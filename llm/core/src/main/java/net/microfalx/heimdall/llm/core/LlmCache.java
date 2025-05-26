package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.setFromString;
import static net.microfalx.lang.StringUtils.toIdentifier;

public class LlmCache extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmCache.class);

    private final LlmServiceImpl aiService;

    private final Map<String, net.microfalx.heimdall.llm.api.Model> models = new HashMap<>();
    private final Map<String, Provider> providers = new HashMap<>();

    LlmCache(LlmServiceImpl aiService) {
        this.aiService = aiService;
    }


    Map<String, Model> getModels(){
        return models;
    }

    void registerModel(net.microfalx.heimdall.llm.api.Model model) {
        models.put(toIdentifier(model.getId()), model);
    }

    Model getModel(String id) {
        requireNonNull(id);
        Model model = models.get(toIdentifier(id));
        if (model == null) {
            throw new LlmNotFoundException("A model with identifier '" + id + "' is not registered");
        }
        return model;
    }

    void registerProvider(net.microfalx.heimdall.llm.api.Provider provider) {
        providers.put(toIdentifier(provider.getId()), provider);
        for (net.microfalx.heimdall.llm.api.Model model : provider.getModels()) {
            registerModel(model);
        }
    }

    Map<String, Provider> getProviders() {
        return providers;
    }

    Provider getProvider(String id) {
        requireNonNull(id);
        Provider cluster = providers.get(toIdentifier(id));
        if (cluster == null) {
            throw new LlmNotFoundException("A provider with identifier '" + id + "' is not registered");
        }
        return cluster;
    }

    void load() {
        LOGGER.info("Load AI");
        try {
           loadModels();
        } catch (Exception e) {
            LOGGER.error("Failed to load models", e);
        }
        try {
            loadProviders();
        } catch (Exception e) {
            LOGGER.error("Failed to load providers", e);
        }
        LOGGER.info("AI loaded, providers: {}, models: {}", providers.size(), models.size());
    }

    private void loadModels() {
        List<net.microfalx.heimdall.llm.core.Model> serviceJpas = getBean(ModelRepository.class).findAll();
        for (net.microfalx.heimdall.llm.core.Model modelJpa : serviceJpas) {
            Model.Builder builder = new Model.Builder(modelJpa.getNaturalId())
                    .addStopSequences(modelJpa.getStopSequences()).frequencyPenalty(modelJpa.getFrequencyPenalty())
                    .modelName(modelJpa.getModelName()).maximumOutputTokens(modelJpa.getMaximumOutputTokens())
                    .presencePenalty(modelJpa.getPresencePenalty()).temperature(modelJpa.getTemperature()).
                    uri(URI.create(modelJpa.getUri()),modelJpa.getApiKey()).topK(modelJpa.getTopK()).topP(modelJpa.getTopP())
                    .responseFormat(modelJpa.getResponseFormat()).setDefault(modelJpa.is_default()).setEnabled(modelJpa.isEnabled());
            builder.tags(setFromString(modelJpa.getTags())).name(modelJpa.getName()).description(modelJpa.getDescription());
            registerModel(builder.build());
        }
    }

    private void loadProviders() {
        List<net.microfalx.heimdall.llm.core.Provider> providersJpas = getBean(ProviderRepository.class).findAll();
        for (net.microfalx.heimdall.llm.core.Provider providerJpa : providersJpas) {
            Provider.Builder builder = new Provider.Builder(providerJpa.getNaturalId()).
                    uri(URI.create(providerJpa.getUri()),providerJpa.getApiKey()).
                    author(providerJpa.getAuthor()).version(providerJpa.getVersion()).license(providerJpa.getLicense());
            builder.tags(setFromString(providerJpa.getTags())).name(providerJpa.getName()).description(providerJpa.getDescription());
            for (net.microfalx.heimdall.llm.api.Model model:models.values()){
                Model.Builder modelBuilder = new Model.Builder(model.getId())
                        .addStopSequences(model.getStopSequences().stream().toList()).frequencyPenalty(model.getFrequencyPenalty())
                        .modelName(model.getModelName()).maximumOutputTokens(model.getMaximumOutputTokens())
                        .presencePenalty(model.getPresencePenalty()).temperature(model.getTemperature()).
                        uri(model.getUri(),model.getApyKey()).topK(model.getTopK()).topP(model.getTopP())
                        .responseFormat(model.getResponseFormat());
                builder.tags(model.getTags()).name(model.getName()).description(model.getDescription());
                builder.model(modelBuilder);
            }
            registerProvider(builder.build());
        }
    }

}
