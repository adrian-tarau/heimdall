package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.TimeUtils.ONE_MINUTE;
import static net.microfalx.lang.TimeUtils.millisSince;

@Service
public class AiServiceImpl extends ApplicationContextSupport implements AiService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiService.class);

    @Autowired
    private ApplicationContext applicationContext;

    private volatile AICache cache = new AICache(this);
    private final AiPersistence aiPersistence = new AiPersistence();
    private final Collection<AiListener> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Chat> activeChats = new CopyOnWriteArrayList<>();
    private final Collection<Chat> closedChats = new CopyOnWriteArrayList<>();
    private volatile Map<String, net.microfalx.heimdall.llm.api.Model> models;
    private volatile long lastModelUpdates = TimeUtils.oneDayAgo();

    @Autowired(required = false)
    private ThreadPool chatPool;

    @Override
    public Chat createChat(String modelId) {
        Model model = getModel(modelId);
        return createChat(model);
    }

    @Override
    public Chat createChat(Model model) {
        requireNonNull(model);
        Chat chat = model.getProvider().getChatFactory().createChat(model);
        activeChats.add(chat);
        if (chat instanceof AbstractChat abstractChat) abstractChat.service = this;
        return chat;
    }

    @Override
    public Collection<Model> getModels() {
        updateModels();
        return unmodifiableCollection(cache.getModels().values());
    }

    @Override
    public Model getModel(String id) {
        updateModels();
        return cache.getModel(id);
    }

    @Override
    public Collection<Chat> getActiveChats() {
        return List.of();
    }

    @Override
    public Iterable<Chat> getHistoricalChats() {
        return null;
    }

    @Override
    public Collection<Provider> getProviders() {
        updateModels();
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

    private void updateModels() {
        if (millisSince(lastModelUpdates) < ONE_MINUTE) return;
        Map<String, Model> newModels = new HashMap<>();
        for (Provider provider : cache.getProviders().values()) {
            for (Model model : provider.getModels()) {
                newModels.put(model.getId().toLowerCase(), model);
            }
        }
        lastModelUpdates = currentTimeMillis();
        this.models = newModels;
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
