package net.microfalx.heimdall.llm.core.provider.onnx;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

@net.microfalx.lang.annotation.Provider
public class OnnxProviderFactory implements Provider.Factory {

    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("onnx");
        builder.name("ONNX").description("A cross-platform runtime for machine-learning model accelerator");
        builder.version("1.22").author("Microsoft").license("MIT")
                .chatFactory(new OnnxChatFactory()).embeddingFactory(new OnnxEmbeddingFactory())
                .tag("onnx").tag("microsoft");
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model((Model.Builder) Model.create("all-minilm-l6-v2-q", "MinLM L6", "all-minilm-l6-v2-q")
                .forEmbedding().tag("onnx").tag("huggingface").tag("quantized").tag("embedding"));
        builder.model((Model.Builder) Model.create("e5-small-v2-q", "E5 Small", "e5-small-v2-q")
                .forEmbedding().asDefault().tag("onnx").tag("microsoft").tag("quantized").tag("embedding"));
    }
}
