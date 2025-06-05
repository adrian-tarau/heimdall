package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.lang.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.setFromString;
import static net.microfalx.lang.ObjectUtils.defaultIfNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

public class LlmCache extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmCache.class);

    private final LlmServiceImpl llmService;

    private final Map<String, net.microfalx.heimdall.llm.api.Model> models = new HashMap<>();
    private final Map<String, Provider> providers = new HashMap<>();

    LlmCache(LlmServiceImpl llmService) {
        this.llmService = llmService;
    }


    Map<String, Model> getModels() {
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
        LOGGER.info("AI loaded, providers: {}, models: {}", providers.size(), models.size());
    }

    private void loadModels() {
        LlmProperties properties = llmService.getLlmProperties();
        Map<Integer, Provider.Builder> providerBuilders = new HashMap<>();
        List<net.microfalx.heimdall.llm.core.Model> modelJpas = getBean(ModelRepository.class).findAll();
        for (net.microfalx.heimdall.llm.core.Model modelJpa : modelJpas) {
            Model.Builder modelBuild = loadModel(modelJpa, properties);
            Provider.Builder providerBuild = providerBuilders.get(modelJpa.getProvider().getId());
            if (providerBuild == null) {
                providerBuild = loadProvider(modelJpa.getProvider());
                providerBuilders.put(modelJpa.getProvider().getId(), providerBuild);
            }
            providerBuild.model(modelBuild);
        }
        providerBuilders.values().forEach(builder -> registerProvider(builder.build()));
    }

    private Model.Builder loadModel(net.microfalx.heimdall.llm.core.Model modelJpa, LlmProperties properties) {
        Model.Builder builder = new Model.Builder(modelJpa.getNaturalId())
                .addStopSequences(new ArrayList<>(CollectionUtils.setFromString(modelJpa.getStopSequences()))).frequencyPenalty(modelJpa.getFrequencyPenalty())
                .modelName(modelJpa.getModelName()).maximumOutputTokens(modelJpa.getMaximumOutputTokens())
                .presencePenalty(modelJpa.getPresencePenalty())
                .temperature(defaultIfNull(modelJpa.getTemperature(), properties.getDefaultTemperature()));
        builder.uri(UriUtils.parseUri(modelJpa.getUri()))
                .apyKey(modelJpa.getApiKey());
        builder.topK(defaultIfNull(modelJpa.getTopK(), properties.getDefaultTopK()))
                .topP(defaultIfNull(modelJpa.getTopP(), properties.getDefaultTopP()))
                .responseFormat(modelJpa.getResponseFormat()).setDefault(modelJpa.isDefault())
                .enabled(modelJpa.isEnabled()).embedding(modelJpa.isEmbedding());
        builder.tags(setFromString(modelJpa.getTags())).name(modelJpa.getName()).description(modelJpa.getDescription());
        return builder;
    }

    private Provider.Builder loadProvider(net.microfalx.heimdall.llm.core.Provider providerJpa) {
        Provider.Builder builder = new Provider.Builder(providerJpa.getNaturalId())
                .author(providerJpa.getAuthor());
        builder.uri(UriUtils.parseUri(providerJpa.getUri()))
                .apyKey(providerJpa.getApiKey())
                .license(providerJpa.getLicense()).version(providerJpa.getVersion());
        builder.tags(CollectionUtils.setFromString(providerJpa.getTags()))
                .name(providerJpa.getName()).description(providerJpa.getDescription());
        return builder;
    }
}
