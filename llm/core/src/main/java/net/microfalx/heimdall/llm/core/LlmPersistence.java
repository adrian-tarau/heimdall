package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.heimdall.llm.api.LlmException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.jpa.*;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.resource.Resource;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.setToString;

class LlmPersistence extends ApplicationContextSupport {

    private LlmServiceImpl llmService;

    public LlmPersistence(LlmServiceImpl llmService) {
        requireNonNull(llmService);
        this.llmService = llmService;
    }

    net.microfalx.heimdall.llm.core.jpa.Provider execute(Provider provider) {
        NaturalIdEntityUpdater<net.microfalx.heimdall.llm.core.jpa.Provider, Integer> updater = getUpdater(ProviderRepository.class);
        net.microfalx.heimdall.llm.core.jpa.Provider jpaProvider = new net.microfalx.heimdall.llm.core.jpa.Provider();
        jpaProvider.setLicense(provider.getLicense());
        jpaProvider.setAuthor(provider.getAuthor());
        jpaProvider.setNaturalId(provider.getId());
        jpaProvider.setVersion(provider.getVersion());
        if (provider.getUri() == null) {
            jpaProvider.setUri(null);
        } else {
            jpaProvider.setUri(provider.getUri().toASCIIString());
        }
        jpaProvider.setTags(setToString(provider.getTags()));
        jpaProvider.setName(provider.getName());
        jpaProvider.setDescription(provider.getDescription());
        jpaProvider.setApiKey(provider.getApyKey());
        jpaProvider.setTags(CollectionUtils.setToString(provider.getTags()));
        return updater.findByNaturalIdAndUpdate(jpaProvider);
    }

    net.microfalx.heimdall.llm.core.jpa.Model execute(Model model) {
        NaturalIdEntityUpdater<net.microfalx.heimdall.llm.core.jpa.Model, Integer> updater = getUpdater(ModelRepository.class);
        net.microfalx.heimdall.llm.core.jpa.Model jpaModel = new net.microfalx.heimdall.llm.core.jpa.Model();
        jpaModel.setNaturalId(model.getId());
        jpaModel.setModelName(model.getModelName());
        jpaModel.setApiKey(model.getApyKey(false));
        jpaModel.setFrequencyPenalty(model.getFrequencyPenalty());
        jpaModel.setPresencePenalty(model.getPresencePenalty());
        jpaModel.setMaximumOutputTokens(model.getMaximumOutputTokens());
        if (jpaModel.getUri() == null) {
            jpaModel.setUri(null);
        } else {
            jpaModel.setUri(model.getUri(false).toASCIIString());
        }
        jpaModel.setTemperature(model.getTemperature());
        jpaModel.setTopK(model.getTopK());
        jpaModel.setTopP(model.getTopP());
        jpaModel.setResponseFormat(model.getResponseFormat());
        jpaModel.setStopSequences(setToString(model.getStopSequences()));
        jpaModel.setName(model.getName());
        jpaModel.setTags(setToString(model.getTags()));
        jpaModel.setDescription(model.getDescription());
        jpaModel.setName(model.getName());
        jpaModel.setDefault(model.isDefault());
        jpaModel.setEnabled(model.isEnabled());
        jpaModel.setEmbedding(model.isEmbedding());
        jpaModel.setProvider(execute(model.getProvider()));
        jpaModel.setDescription(model.getDescription());
        jpaModel.setTags(CollectionUtils.setToString(model.getTags()));
        jpaModel.setMaximumContextLength(model.getMaximumContextLength());
        return updater.findByNaturalIdAndUpdate(jpaModel);
    }

    void execute(net.microfalx.heimdall.llm.api.Chat chat) {
        if (chat.getMessageCount() == 0) return;
        Resource resource;
        try {
            resource = llmService.writeChatMessages(chat);
        } catch (IOException e) {
            throw new LlmException("Failed to write chat messages for " + chat.getId(), e);
        }
        ChatRepository repository = getBean(ChatRepository.class);
        Chat jpaChat = new Chat();
        jpaChat.setId(chat.getId());
        jpaChat.setModel(execute(chat.getModel()));
        jpaChat.setName(chat.getName());
        jpaChat.setResource(resource.toURI().toASCIIString());
        jpaChat.setDuration(chat.getDuration());
        jpaChat.setUser(chat.getUser().getName());
        jpaChat.setFinishAt(chat.getFinishAt());
        jpaChat.setStartAt(chat.getStartAt());
        jpaChat.setTokenCount(chat.getTokenCount());
        repository.saveAndFlush(jpaChat);
    }

    void execute(net.microfalx.heimdall.llm.api.Prompt prompt) {
        NaturalIdEntityUpdater<Prompt, Integer> updater = getUpdater(PromptRepository.class);
        Prompt jpaPrompt = new Prompt();
        jpaPrompt.setNaturalId(prompt.getId());
        jpaPrompt.setName(prompt.getName());
        jpaPrompt.setRole(prompt.getRole());
        jpaPrompt.setMaximumInputEvents(prompt.getMaximumInputEvents());
        jpaPrompt.setMaximumOutputTokens(prompt.getMaximumOutputTokens());
        jpaPrompt.setChainOfThought(prompt.isChainOfThought());
        jpaPrompt.setUseOnlyContext(prompt.isUseOnlyContext());
        jpaPrompt.setExamples(prompt.getExamples());
        jpaPrompt.setContext(prompt.getContext());
        jpaPrompt.setQuestion(prompt.getQuestion());
        jpaPrompt.setTags(setToString(prompt.getTags()));
        jpaPrompt.setDescription(prompt.getDescription());
        updater.findByNaturalIdAndUpdate(jpaPrompt);
    }


    private <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }
}
