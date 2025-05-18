package net.microfalx.heimdall.llm.core;

import net.microfalx.heimdall.llm.api.*;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AiServiceImpl implements AiService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiService.class);

    private final Collection<Provider> providers = new CopyOnWriteArrayList<>();
    private final Collection<AiListener> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Chat> activeChats = new CopyOnWriteArrayList<>();
    private final Collection<Chat> closedChats = new CopyOnWriteArrayList<>();
    private volatile Map<String, Model> models;
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
        return unmodifiableCollection(models.values());
    }

    @Override
    public Model getModel(String id) {
        updateModels();
        Model model = models.get(id.toLowerCase());
        if (model == null) throw new AiNotFoundException("A model with identifier '" + id + "' cannot be found");
        return model;
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
        return unmodifiableCollection(providers);
    }

    @Override
    public void registerProvider(Provider provider) {
        requireNonNull(provider);
        providers.add(provider);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initListeners();
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

    private void updateModels() {
        if (millisSince(lastModelUpdates) < ONE_MINUTE) return;
        Map<String, Model> newModels = new HashMap<>();
        for (Provider provider : providers) {
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
