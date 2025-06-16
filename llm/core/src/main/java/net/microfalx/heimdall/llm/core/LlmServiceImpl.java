package net.microfalx.heimdall.llm.core;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.lucene.LuceneEmbeddingStore;
import net.microfalx.lang.*;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.security.Principal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.StringUtils.*;

@Service
public class LlmServiceImpl extends ApplicationContextSupport implements LlmService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmService.class);

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    @Autowired(required = false)
    @Getter(AccessLevel.PROTECTED)
    private LlmProperties properties = new LlmProperties();

    private File variableDirectory;
    private LuceneEmbeddingStore embeddingStore;
    private ContentRetriever contentRetriever;
    private ChatMemoryStore chatStore;
    private volatile LlmCache cache = new LlmCache(null, this);
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();
    private final LlmPersistence llmPersistence = new LlmPersistence();
    private final Collection<LlmListener> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Provider.Factory> providerFactories = new CopyOnWriteArrayList<>();
    private final Map<String, net.microfalx.heimdall.llm.api.Chat> activeChats = new ConcurrentHashMap<>();
    private final Collection<net.microfalx.heimdall.llm.api.Chat> closedChats = new CopyOnWriteArrayList<>();

    private volatile Model defaultModel;
    private volatile Model defaultEmbeddingModel;

    private ThreadPool chatPool;
    private ThreadPool embeddingPool;

    public LlmServiceImpl() {
        // Workaround for classes not being loaded by Spring
        warmClassesWorkaround();
    }

    ChatMemoryStore getChatStore() {
        return chatStore;
    }

    ContentRetriever getContentRetriever() {
        return contentRetriever;
    }

    @Override
    public Chat createChat() {
        return createChat(Prompt.empty());
    }

    @Override
    public Chat createChat(Prompt prompt) {
        return createChat(prompt, getDefaultModel());
    }

    @Override
    public Chat createChat(Model model) {
        return createChat(Prompt.empty(), model);
    }

    public net.microfalx.heimdall.llm.api.Chat createChat(Prompt prompt, Model model) {
        requireNonNull(prompt);
        requireNonNull(model);
        if (model.isEmbedding()) throw new LlmException("Model '" + model.getId() + "' does not support chatting");
        Chat.Factory chatFactory = model.getProvider().getChatFactory();
        net.microfalx.heimdall.llm.api.Chat chat = chatFactory.createChat(prompt, model);
        activeChats.put(toIdentifier(chat.getId()), chat);
        if (chat instanceof AbstractChat abstractChat) abstractChat.initialize(this);
        return chat;
    }

    @Override
    public Collection<Chat> getChats(Principal principal) {
        requireNonNull(principal);
        Collection<Chat> chats = new ArrayList<>();
        activeChats.values().forEach(chat -> {
            if (principal.equals(chat.getUser())) chats.add(chat);
        });
        return chats;
    }

    @Override
    public Chat getChat(String id) {
        requireNonNull(id);
        Chat chat = activeChats.get(toIdentifier(id));
        if (chat == null) {
            throw new LlmException("Chat '" + id + "' not found");
        }
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
        if (isNotEmpty(properties.getDefaultModel()) && isNotEmpty(properties.getDefaultProvider())) {
            String defaultModelId = properties.getDefaultProvider() + "." + properties.getDefaultModel();
            defaultModel = cache.findModel(defaultModelId);
            if (defaultModel == null) {
                LOGGER.warn("A model with identifier '{}' not found, falling back to configured model", defaultModelId);
            }
        }
        if (defaultModel == null) {
            defaultModel = getModels().stream().filter(model -> model.isDefault() && !model.isEmbedding())
                    .findFirst().orElseThrow(() -> new LlmNotFoundException("No default model found"));
        }
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
        persistProvider(provider);
    }

    @Override
    public Collection<Tool> getTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    @Override
    public Tool getTool(String id) {
        requireNonNull(id);
        Tool tool = tools.get(toIdentifier(id));
        if (tool == null) throw new LlmNotFoundException("Tool '" + id + "' not found");
        return tool;
    }

    @Override
    public void registerTool(Tool tool) {
        requireNonNull(tool);
        LOGGER.info("Registering tool {}", tool.getId());
        tools.put(toIdentifier(tool.getId()), tool);
    }

    /**
     * Returns the final prompt text for the given model and prompt.
     *
     * @param model  the model to use
     * @param prompt the prompt to use
     * @return a non-null string
     */
    public String getSystemMessage(Model model, Prompt prompt) {
        SystemMessageBuilder builder = new SystemMessageBuilder(this, model, prompt);
        return builder.build();
    }

    @Override
    public void reload() {
        if (!properties.isPersistenceEnabled()) return;
        LlmCache cache = new LlmCache(this.cache, this);
        cache.setApplicationContext(getApplicationContext());
        cache.load();
        this.defaultModel = null;
        this.defaultEmbeddingModel = null;
        this.cache = cache;
    }

    @Override
    public Collection<Prompt> getPrompts() {
        return unmodifiableCollection(cache.getPrompts().values());
    }

    @Override
    public Prompt getPrompt(String id) {
        return cache.getPrompt(id);
    }

    @Override
    public void registerPrompt(Prompt prompt) {
        requireNonNull(prompt);
        cache.registerPrompt(prompt);
        llmPersistence.execute(prompt);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registerLibraryPaths();
        initThreadPools();
        initDirectories();
        initListeners();
        initProviderFactories();
        initializeApplicationContext();
        registerProviders();
        initializeChatStore();
        initializeEmbeddingStore();
        initTask();
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

    /**
     * Closes the given chat and persists it.
     *
     * @param chat the chat to close
     */
    void closeChat(Chat chat) {
        requireNonNull(chat);
        activeChats.remove(chat.getId());
        closedChats.add(chat);
        llmPersistence.execute(chat);
    }

    /**
     * Returns a fragment of the prompt text.
     *
     * @param model    the model to use
     * @param prompt   the prompt to use
     * @param fragment the fragment to return
     * @return the text of the fragment, never null
     */
    String getPromptFragment(Model model, Prompt prompt, Prompt.Fragment fragment, String text) {
        requireNonNull(model);
        requireNonNull(prompt);
        requireNonNull(fragment);
        String originalText = text;
        for (LlmListener listener : listeners) {
            text = listener.augment(model, prompt, fragment, text);
            if (isNotEmpty(text)) break;
        }
        return isEmpty(text) ? originalText : text;
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
        this.embeddingStore = new LuceneEmbeddingStore(this, indexService, searchService)
                .setThreadPool(getEmbeddingPool());
        this.indexService.registerListener(this.embeddingStore);
        this.contentRetriever = this.embeddingStore.getContentRetriever();
    }

    private void initializeChatStore() {
        this.chatStore = new InMemoryChatMemoryStore();
    }

    private void initializeApplicationContext() {
        llmPersistence.setApplicationContext(getApplicationContext());
        cache.setApplicationContext(getApplicationContext());
    }

    private void initThreadPools() {
        chatPool = ThreadPoolFactory.create("LLM").create();
        embeddingPool = ThreadPoolFactory.create("Embedding").create();
    }

    private void persistProvider(Provider provider) {
        if (!properties.isPersistenceEnabled()) return;
        for (Model model : provider.getModels()) {
            llmPersistence.execute(model);
        }
    }

    private void initTask() {
        ThreadPool threadPool = ThreadPool.get();
        threadPool.execute(this::reload);
        threadPool.execute(this::fireStartEvent);
        threadPool.scheduleAtFixedRate(new MaintenanceTask(), Duration.ofSeconds(60));
    }

    private void fireStartEvent() {
        for (LlmListener listener : listeners) {
            try {
                listener.onStart(this);
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to notify listener {}", ClassUtils.getName(listener));
            }
        }
    }

    private void registerProviders() {
        for (Provider.Factory providerFactory : providerFactories) {
            try {
                if (providerFactory instanceof AbstractProviderFactory abstractProviderFactory) {
                    abstractProviderFactory.setProperties(properties);
                }
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

    private void registerLibraryPaths() {
        File djlCache = JvmUtils.getCacheDirectory("djl");
        System.setProperty("ENGINE_CACHE_DIR", validateDirectoryExists(new File(djlCache, "engine")).getAbsolutePath());
        System.setProperty("DJL_CACHE_DIR", validateDirectoryExists(new File(djlCache, "cache")).getAbsolutePath());
        System.setProperty("DJL_OFFLINE", Boolean.toString(properties.isOffline()));
    }

    private void processPendingChats() {
        for (Chat chat : activeChats.values()) {
            long lastActivity = ((AbstractChat) chat).lastActivity.get();
            if (TimeUtils.millisSince(lastActivity) > properties.getChatTimeout().toMillis()) {
                LOGGER.info("Closing chat {} due to inactivity", chat.getId());
                chat.close();
            }
        }
    }

    class MaintenanceTask implements Runnable {

        @Override
        public void run() {
            try {
                processPendingChats();
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to process pending chats");
            }
        }
    }
}
