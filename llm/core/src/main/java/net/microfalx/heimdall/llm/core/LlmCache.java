package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.jpa.ModelRepository;
import net.microfalx.heimdall.llm.core.jpa.PromptRepository;
import net.microfalx.lang.CollectionUtils;
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
import static net.microfalx.lang.UriUtils.parseUri;

public class LlmCache extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmCache.class);

    private final LlmServiceImpl llmService;
    private volatile LlmCache oldCache;

    private final Map<String, net.microfalx.heimdall.llm.api.Model> models = new HashMap<>();
    private final Map<String, Provider> providers = new HashMap<>();
    private final Map<String, Prompt> prompts = new HashMap<>();

    LlmCache(LlmCache oldCache, LlmServiceImpl llmService) {
        this.oldCache = oldCache;
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

    Map<String, Prompt> getPrompts() {
        return prompts;
    }

    void registerPrompt(Prompt prompt) {
        prompts.put(toIdentifier(prompt.getId()), prompt);
    }

    Prompt getPrompt(String id) {
        requireNonNull(id);
        Prompt prompt = prompts.get(toIdentifier(id));
        if (prompt == null) {
            throw new LlmNotFoundException("A prompt with identifier '" + id + "' is not registered");
        }
        return prompt;
    }

    void load() {
        LOGGER.info("Load models and providers");
        try {
            loadModels();
        } catch (Exception e) {
            LOGGER.error("Failed to load models", e);
        } finally {
            oldCache = null;
        }
        try {
            loadPrompts();
        } catch (Exception e) {
            LOGGER.error("Failed to load prompts", e);
        }finally {
            oldCache = null;
        }
        LOGGER.info("Loaded completed, providers: {}, models: {}, prompts: {}", providers.size(), models.size(), prompts.size());
    }

    private void loadModels() {
        LlmProperties properties = llmService.getProperties();
        Map<Integer, Provider.Builder> providerBuilders = new HashMap<>();
        List<net.microfalx.heimdall.llm.core.jpa.Model> modelJpas = getBean(ModelRepository.class).findAll();
        for (net.microfalx.heimdall.llm.core.jpa.Model modelJpa : modelJpas) {
            Model.Builder modelBuild = loadModel(modelJpa, properties);
            Provider.Builder providerBuild = providerBuilders.get(modelJpa.getProvider().getId());
            if (providerBuild == null) {
                providerBuild = loadProvider(modelJpa.getProvider());
                if (providerBuild != null) {
                    providerBuilders.put(modelJpa.getProvider().getId(), providerBuild);
                }
            }
            if (providerBuild != null) {
                providerBuild.model(modelBuild);
            }
        }
        providerBuilders.values().forEach(builder -> registerProvider(builder.build()));
    }

    private Model.Builder loadModel(net.microfalx.heimdall.llm.core.jpa.Model modelJpa, LlmProperties properties) {
        Model.Builder builder = new Model.Builder(modelJpa.getNaturalId())
                .addStopSequences(new ArrayList<>(CollectionUtils.setFromString(modelJpa.getStopSequences()))).frequencyPenalty(modelJpa.getFrequencyPenalty())
                .modelName(modelJpa.getModelName()).maximumOutputTokens(modelJpa.getMaximumOutputTokens())
                .presencePenalty(modelJpa.getPresencePenalty())
                .temperature(defaultIfNull(modelJpa.getTemperature(), properties.getDefaultTemperature()));
        builder.uri(parseUri(modelJpa.getUri()))
                .apyKey(modelJpa.getApiKey());
        builder.topK(defaultIfNull(modelJpa.getTopK(), properties.getDefaultTopK()))
                .topP(defaultIfNull(modelJpa.getTopP(), properties.getDefaultTopP()))
                .responseFormat(modelJpa.getResponseFormat()).setDefault(modelJpa.isDefault())
                .enabled(modelJpa.isEnabled()).embedding(modelJpa.isEmbedding());
        builder.tags(setFromString(modelJpa.getTags())).name(modelJpa.getName())
                .description(modelJpa.getDescription());
        return builder;
    }

    private Provider.Builder loadProvider(net.microfalx.heimdall.llm.core.jpa.Provider providerJpa) {
        Provider oldProvider = null;
        try {
            if (oldCache != null) oldProvider = oldCache.getProvider(providerJpa.getNaturalId());
        } catch (LlmNotFoundException e) {
            return null;
        }
        if (oldProvider == null) {
            LOGGER.warn("No previous provider found for '{}', skipping", providerJpa.getNaturalId());
            return null;
        }
        Provider.Builder builder = new Provider.Builder(providerJpa.getNaturalId())
                .author(providerJpa.getAuthor());
        builder.uri(parseUri(providerJpa.getUri()))
                .apyKey(providerJpa.getApiKey())
                .license(providerJpa.getLicense()).version(providerJpa.getVersion());
        builder.tags(CollectionUtils.setFromString(providerJpa.getTags()))
                .name(providerJpa.getName()).description(providerJpa.getDescription());
        builder.chatFactory(oldProvider.getChatFactory());
        try {
            builder.embeddingFactory(oldProvider.getEmbeddingFactory());
        } catch (IllegalStateException e) {
            // ignore if the embedding factory is not available
        }
        return builder;
    }

    private void loadPrompts() {
        List<net.microfalx.heimdall.llm.core.jpa.Prompt> modelJpas = getBean(PromptRepository.class).findAll();
        modelJpas.forEach(promptJpa -> {
            Prompt.Builder builder = new Prompt.Builder()
                    .chainOfThought(promptJpa.isChainOfThought())
                    .context(promptJpa.getContext())
                    .examples(promptJpa.getExamples())
                    .maximumInputEvents(promptJpa.getMaximumInputEvents())
                    .maximumOutputTokens(promptJpa.getMaximumOutputTokens())
                    .question(promptJpa.getQuestion())
                    .role(promptJpa.getRole())
                    .useOnlyContext(promptJpa.isUseOnlyContext());
            builder.tags(CollectionUtils.setFromString(promptJpa.getTags()))
                    .name(promptJpa.getName());
            registerPrompt(builder.build());
        });
    }
}
