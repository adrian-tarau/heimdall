package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

import static net.microfalx.lang.CollectionUtils.setToString;

public class LlmPersistence extends ApplicationContextSupport {

    net.microfalx.heimdall.llm.core.Provider execute(Provider provider) {
        NaturalIdEntityUpdater<net.microfalx.heimdall.llm.core.Provider, Integer> updater = getUpdater(ProviderRepository.class);
        net.microfalx.heimdall.llm.core.Provider jpaProvider = new net.microfalx.heimdall.llm.core.Provider();
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
        return updater.findByNaturalIdAndUpdate(jpaProvider);
    }

    net.microfalx.heimdall.llm.core.Model execute(Model model) {
        NaturalIdEntityUpdater<net.microfalx.heimdall.llm.core.Model, Integer> updater = getUpdater(ModelRepository.class);
        net.microfalx.heimdall.llm.core.Model jpaModel = new net.microfalx.heimdall.llm.core.Model();
        jpaModel.setNaturalId(model.getId());
        jpaModel.setModelName(model.getModelName());
        jpaModel.setApiKey(model.getApyKey());
        jpaModel.setFrequencyPenalty(model.getFrequencyPenalty());
        jpaModel.setPresencePenalty(model.getPresencePenalty());
        jpaModel.setMaximumOutputTokens(model.getMaximumOutputTokens());
        if (jpaModel.getUri() == null) {
            jpaModel.setUri(null);
        } else {
            jpaModel.setUri(model.getUri().toASCIIString());
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
        return updater.findByNaturalIdAndUpdate(jpaModel);
    }

    void execute(net.microfalx.heimdall.llm.api.Chat chat) {
        NaturalIdEntityUpdater<Chat, Integer> updater = getUpdater(ChatRepository.class);
        Chat jpaChat = new Chat();
        jpaChat.setModel(execute(chat.getModel()));
        jpaChat.setNaturalId(chat.getId());
        jpaChat.setName(chat.getName());
        jpaChat.setContent("");
        jpaChat.setDuration(chat.getDuration());
        jpaChat.setUser(chat.getUser().getName());
        jpaChat.setFinishAt(chat.getFinishAt());
        jpaChat.setStartAt(chat.getStartAt());
        jpaChat.setTokenCount(chat.getTokenCount());
        updater.findByNaturalIdAndUpdate(jpaChat);
    }


    private <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }
}
