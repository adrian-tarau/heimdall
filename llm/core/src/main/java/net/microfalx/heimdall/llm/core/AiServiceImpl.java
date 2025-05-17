package net.microfalx.heimdall.llm.core;

import net.microfalx.heimdall.llm.api.*;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class AiServiceImpl implements AiService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiService.class);

    private final Collection<Provider> providers = new CopyOnWriteArrayList<>();
    private final Collection<AiListener> listeners = new CopyOnWriteArrayList<>();
    private volatile Map<String, Model> models;
    private volatile long lastModelUpdates;

    @Override
    public Chat createChat(Model model) {
        requireNonNull(model);
        return model.getProvider().getChatFactory().createChat(model);
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
        if (TimeUtils.millisSince(lastModelUpdates) < TimeUtils.ONE_MINUTE) return;
        Map<String, Model> newModels = new HashMap<>();
        for (Provider provider : providers) {
            for (Model model : provider.getModels()) {
                newModels.put(model.getId().toLowerCase(), model);
            }
        }
        lastModelUpdates = currentTimeMillis();
        this.models = newModels;
    }
}
