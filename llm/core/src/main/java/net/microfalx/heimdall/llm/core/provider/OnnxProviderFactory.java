package net.microfalx.heimdall.llm.core.provider;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

@net.microfalx.lang.annotation.Provider
public class OnnxProviderFactory implements Provider.Factory {

    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("onnx");
        builder.name("ONNX").description("A cross-platform runtime for machine-learning model accelerator");
        builder.version("0.1.0").author("ONNX Team").license("MIT")
                .chatFactory(new OnnxChatFactory()).embeddingFactory(new OnnxEmbeddingFactory());
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model((Model.Builder) Model.create("all-minilm-l6-v2-q", "MinLM L6 Quantized", "all-minilm-l6-v2-q")
                .forEmbedding().tag("onnx").tag("huggingface").tag("quantized"));
        builder.model((Model.Builder) Model.create("e5-small-v2-q", "E5 Small Quantized", "e5-small-v2-q")
                .forEmbedding().asDefault().tag("onnx").tag("microsoft").tag("quantized"));
    }
}
