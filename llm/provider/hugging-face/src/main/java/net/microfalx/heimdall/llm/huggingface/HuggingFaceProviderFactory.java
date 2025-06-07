package net.microfalx.heimdall.llm.huggingface;

import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.AbstractProviderFactory;

import java.net.URI;
@net.microfalx.lang.annotation.Provider
public class HuggingFaceProviderFactory extends AbstractProviderFactory {

    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("hugging_face");
        builder.name("HuggingFace").description("a machine learning (ML) and data science platform and community that helps users build, deploy and train machine learning models");
        builder.version("0.1.0").author("Hugging Face").license("Apache 2.0").uri(URI.create("https://huggingface.co/"))
                .chatFactory(new HuggingFaceChatFactory()).tag("huggingface");
        return builder.build();
    }
}
