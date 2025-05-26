package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.lang.ClassUtils;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Service
public class LlmServiceImpl extends ApplicationContextSupport implements LlmService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmService.class);

    @Autowired
    private ApplicationContext applicationContext;

    private volatile LlmCache cache = new LlmCache(this);
    private final LlmPersistence llmPersistence = new LlmPersistence();
    private final Collection<LlmListener> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Provider.Factory> providerFactories = new CopyOnWriteArrayList<>();
    private final Collection<net.microfalx.heimdall.llm.api.Chat> activeChats = new CopyOnWriteArrayList<>();
    private final Collection<net.microfalx.heimdall.llm.api.Chat> closedChats = new CopyOnWriteArrayList<>();

    @Autowired(required = false)
    private ThreadPool chatPool;

    @Override
    public net.microfalx.heimdall.llm.api.Chat createChat(String modelId) {
        Model model = getModel(modelId);
        return createChat(model);
    }

    @Override
    public net.microfalx.heimdall.llm.api.Chat createChat(Model model) {
        requireNonNull(model);
        net.microfalx.heimdall.llm.api.Chat chat = model.getProvider().getChatFactory().createChat(model);
        activeChats.add(chat);
        if (chat instanceof AbstractChat abstractChat) abstractChat.service = this;
        return chat;
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
        this.cache = cache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initListeners();
        initProviderFactories();
        initializeApplicationContext();
        registerProviders();
    }

    ThreadPool getChatPool() {
        return chatPool != null ? chatPool : ThreadPool.get();
    }

    void closeChat(Chat chat) {
        requireNonNull(chat);
        activeChats.remove(chat);
        closedChats.add(chat);
        llmPersistence.execute(chat);
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

    private void initializeApplicationContext() {
        llmPersistence.setApplicationContext(getApplicationContext());
        cache.setApplicationContext(getApplicationContext());
    }

    private void registerProviders() {
        for (Provider.Factory providerFactory : providerFactories) {
            try {
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
}
