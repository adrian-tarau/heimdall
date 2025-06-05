package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.bootstrap.test.answer.RepositoryAnswer;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextConfiguration(classes = {LlmServiceImpl.class, MetadataService.class})
class LlmServiceImplTest extends AbstractBootstrapServiceTestCase {

    @TestBean private ModelRepository modelRepository;
    @TestBean private ProviderRepository providerRepository;

    @MockitoBean private IndexService indexService;
    @MockitoBean private SearchService searchService;

    @Autowired private LlmService llmService;

    @Test
    void initialize() {
        assertThat(llmService.getModels().size()).isGreaterThan(1);
        assertNotNull(llmService.getModel("onnx_all_minilm_l6_v2_q").getTemperature());
        assertNotNull(llmService.getModel("onnx_all_minilm_l6_v2_q").getTopK());
        assertNotNull(llmService.getModel("onnx_all_minilm_l6_v2_q").getTopP());
    }

    @Test
    void embed() {
        assertEquals(384, llmService.embed("This is a test embedding").getDimension());
    }

    private static ModelRepository modelRepository() {
        return RepositoryAnswer.mock(ModelRepository.class);
    }

    private static ProviderRepository providerRepository() {
        return RepositoryAnswer.mock(ProviderRepository.class);
    }

    public static class TestChat extends AbstractChat {

        public TestChat(Model model) {
            super(model);
        }
    }

    public static class TestChatFactory implements net.microfalx.heimdall.llm.api.Chat.Factory {

        @Override
        public Chat createChat(Model model) {
            return new TestChat(model);
        }
    }

    @net.microfalx.lang.annotation.Provider
    public static class TestProviderFactory implements net.microfalx.heimdall.llm.api.Provider.Factory {

        @Override
        public Provider createProvider() {
            Provider.Builder builder = new Provider.Builder("test").chatFactory(new TestChatFactory());
            builder.model(Model.create("m1", "Model 1"));
            builder.model(Model.create("m2", "Model 2"));
            return builder.build();
        }
    }

}