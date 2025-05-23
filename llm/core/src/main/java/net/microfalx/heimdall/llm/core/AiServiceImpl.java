package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
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
public class AiServiceImpl extends ApplicationContextSupport implements AiService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiService.class);

    @Autowired
    private ApplicationContext applicationContext;

    private volatile AICache cache = new AICache(this);
    private final AiPersistence aiPersistence = new AiPersistence();
    private final Collection<AiListener> listeners = new CopyOnWriteArrayList<>();
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
            aiPersistence.execute(model);
        }
    }

    @Override
    public void reload() {
        AICache cache = new AICache(this);
        cache.setApplicationContext(getApplicationContext());
        cache.load();
        this.cache = cache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initListeners();
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
    }

    private void initListeners() {
        Collection<AiListener> listeners = ClassUtils.resolveProviderInstances(AiListener.class);
        LOGGER.info("Register {} listeners", listeners.size());
        for (AiListener listener : listeners) {
            LOGGER.debug(" - {}", ClassUtils.getName(listeners));
            this.listeners.add(listener);
        }
    }

    private void initializeApplicationContext() {
        aiPersistence.setApplicationContext(getApplicationContext());
        cache.setApplicationContext(getApplicationContext());
    }

    private void registerProviders() {
        for (AiListener listener : listeners) {
            try {
                listener.registerProviders(this);
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to register provider with listener {}", ClassUtils.getName(listener));
            }
        }
    }
}
