package net.microfalx.heimdall.llm.core;

import jakarta.annotation.PreDestroy;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.heimdall.llm.lucene.LuceneEmbeddingStore;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Service
public class LlmServiceImpl extends ApplicationContextSupport implements LlmService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmService.class);

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    @Autowired(required = false)
    private LlmProperties llmProperties = new LlmProperties();

    private File variableDirectory;
    private LuceneEmbeddingStore embeddingStore;
    private volatile LlmCache cache = new LlmCache(this);
    private final LlmPersistence llmPersistence = new LlmPersistence();
    private final Collection<LlmListener> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Provider.Factory> providerFactories = new CopyOnWriteArrayList<>();
    private final Collection<net.microfalx.heimdall.llm.api.Chat> activeChats = new CopyOnWriteArrayList<>();
    private final Collection<net.microfalx.heimdall.llm.api.Chat> closedChats = new CopyOnWriteArrayList<>();

    private volatile Model defaultModel;
    private volatile Model defaultEmbeddingModel;

    private ThreadPool chatPool;
    private ThreadPool embeddingPool;

    public LlmServiceImpl() {
        // Workaround for classes not being loaded by Spring
        warmClassesWorkaround();
    }

    @Override
    public Chat createChat() {
        return createChat(getDefaultModel());
    }

    @Override
    public net.microfalx.heimdall.llm.api.Chat createChat(String modelId) {
        Model model = getModel(modelId);
        return createChat(model);
    }

    @Override
    public net.microfalx.heimdall.llm.api.Chat createChat(Model model) {
        requireNonNull(model);
        if (model.isEmbedding()) throw new LlmException("Model '" + model.getId() + "' does not support chating");
        net.microfalx.heimdall.llm.api.Chat chat = model.getProvider().getChatFactory().createChat(model);
        activeChats.add(chat);
        if (chat instanceof AbstractChat abstractChat) abstractChat.service = this;
        return chat;
    }

    @Override
    public Embedding embed(String text) {
        return createEmbedding(getDefaultEmbeddingModel(), text);
    }

    @Override
    public Embedding embed(String modelId, String text) {
        return createEmbedding(getModel(modelId), text);
    }

    public Embedding createEmbedding(Model model, String text) {
        requireNonNull(model);
        if (!model.isEmbedding()) throw new LlmException("Model '" + model.getId() + "' does not support embedding");
        return model.getProvider().getEmbeddingFactory().createEmbedding(model, text);
    }

    @Override
    public Model getDefaultModel() {
        if (defaultModel != null) return defaultModel;
        defaultModel = getModels().stream().filter(model -> model.isDefault() && !model.isEmbedding()).findFirst().orElseThrow(
                () -> new LlmNotFoundException("No default model found"));
        return defaultModel;
    }

    @Override
    public Model getDefaultEmbeddingModel() {
        if (defaultEmbeddingModel != null) return defaultEmbeddingModel;
        defaultEmbeddingModel = getModels().stream().filter(model -> model.isDefault() && model.isEmbedding()).findFirst().orElseThrow(
                () -> new LlmNotFoundException("No default embedding model found"));
        return defaultEmbeddingModel;
    }

    @Override
    public Collection<Model> getModels() {
        return unmodifiableCollection(cache.getModels().values());
    }

    @Override
    public Model getModel(String id) {
        return cache.getModel(id);
    }

    @Override
    public Collection<net.microfalx.heimdall.llm.api.Chat> getActiveChats() {
        return List.of();
    }

    @Override
    public Iterable<net.microfalx.heimdall.llm.api.Chat> getHistoricalChats() {
        return null;
    }

    @Override
    public Collection<Provider> getProviders() {
        return unmodifiableCollection(cache.getProviders().values());
    }

    @Override
    public void registerProvider(Provider provider) {
        requireNonNull(provider);
        cache.registerProvider(provider);
        for (Model model : provider.getModels()) {
            llmPersistence.execute(model);
        }
    }

    @Override
    public void reload() {
        LlmCache cache = new LlmCache(this);
        cache.setApplicationContext(getApplicationContext());
        cache.load();
        this.defaultModel = null;
        this.defaultEmbeddingModel = null;
        this.cache = cache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initThreadPools();
        initDirectories();
        initListeners();
        initProviderFactories();
        initializeApplicationContext();
        registerProviders();
        initializeEmbeddingStore();
    }

    @PreDestroy
    protected void destroy() {
        if (embeddingStore != null) embeddingStore.close();
    }

    public ThreadPool getChatPool() {
        return ObjectUtils.defaultIfNull(chatPool, ThreadPool.get());
    }

    public ThreadPool getEmbeddingPool() {
        return ObjectUtils.defaultIfNull(embeddingPool, ThreadPool.get());
    }

    void closeChat(Chat chat) {
        requireNonNull(chat);
        activeChats.remove(chat);
        closedChats.add(chat);
        llmPersistence.execute(chat);
    }

    private void initDirectories() {
        variableDirectory = JvmUtils.getVariableDirectory("llm");
    }

    private void initListeners() {
        Collection<LlmListener> loadedListeners = ClassUtils.resolveProviderInstances(LlmListener.class);
        LOGGER.info("Register {} listeners", loadedListeners.size());
        for (LlmListener listener : loadedListeners) {
            LOGGER.debug(" - {}", ClassUtils.getName(loadedListeners));
            this.listeners.add(listener);
        }
    }

    private void initProviderFactories() {
        Collection<Provider.Factory> loadedProviderFactories = ClassUtils.resolveProviderInstances(Provider.Factory.class);
        LOGGER.info("Register {} provider factories", loadedProviderFactories.size());
        for (Provider.Factory providerFactory : loadedProviderFactories) {
            LOGGER.debug(" - {}", ClassUtils.getName(providerFactory));
            this.providerFactories.add(providerFactory);
        }
    }

    private void initializeEmbeddingStore() {
        this.embeddingStore = new LuceneEmbeddingStore(this, indexService, searchService, new File(variableDirectory, "embedding"))
                .setThreadPool(getEmbeddingPool());
        this.indexService.registerListener(this.embeddingStore);
    }

    private void initializeApplicationContext() {
        llmPersistence.setApplicationContext(getApplicationContext());
        cache.setApplicationContext(getApplicationContext());
    }

    private void initThreadPools() {
        chatPool = ThreadPoolFactory.create("LLM").create();
        embeddingPool = ThreadPoolFactory.create("Embedding").create();
    }

    private void registerProviders() {
        for (Provider.Factory providerFactory : providerFactories) {
            try {
                Method method = ReflectionUtils.findMethod(providerFactory.getClass(), "setLlmProperties");
                if (method != null) ReflectionUtils.invokeMethod(method, providerFactory, llmProperties);
                Provider provider = providerFactory.createProvider();
                if (provider == null) {
                    LOGGER.error("Provider factory {} returned NULL", ClassUtils.getName(providerFactory));
                } else {
                    registerProvider(provider);
                }
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to create provider with factory {}", ClassUtils.getName(providerFactory));
            }
        }
    }

    private void warmClassesWorkaround() {
        try {
            Class.forName("net.microfalx.heimdall.llm.lucene.LuceneContentRetriever");
            Class.forName("net.microfalx.heimdall.llm.lucene.LuceneEmbeddingStore");
        } catch (ClassNotFoundException e) {
            ExceptionUtils.throwException(e);
        }
    }
}
