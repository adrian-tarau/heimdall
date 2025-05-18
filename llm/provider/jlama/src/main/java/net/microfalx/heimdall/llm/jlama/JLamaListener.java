package net.microfalx.heimdall.llm.jlama;

import net.microfalx.heimdall.llm.api.AiListener;
import net.microfalx.heimdall.llm.api.AiService;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.lang.annotation.Provider;

@Provider
public class JLamaListener implements AiListener  {

    @Override
    public void registerProviders(AiService service) {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("jlama");
        builder.name("JLama").description("A LLM inference engine for Java");
        builder.version("0.1.0").author("Jake Luciani").license("Apache-2.0")
                .chatFactory(new JLamaChatFactory());
        registerModels(builder);
        service.registerProvider(builder.build());
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        // this model gets into a loop
        //builder.model(Model.create("tinyllama", "TinyLlama", "tjake/TinyLlama-1.1B-Chat-v1.0-Jlama-Q4"));
        builder.model((Model.Builder) Model.create("llama3.2-1b", "Llama 3.2 (1B)", "tjake/Llama-3.2-1B-Instruct-JQ4")
                .tag("meta").tag("llama").tag("1b").tag("llama3"));
        builder.model((Model.Builder) Model.create("llama3.2-3b", "Llama 3.2 (3B)", "tjake/Llama-3.2-3B-Instruct-JQ4")
                .tag("meta").tag("llama").tag("3b").tag("llama3"));
        builder.model((Model.Builder) Model.create("gemma2-2b", "Gemma 3 (2B)", "tjake/gemma-2-2b-it-JQ4")
                .tag("google").tag("gemma").tag("2b").tag("gemma2"));



    }
}
