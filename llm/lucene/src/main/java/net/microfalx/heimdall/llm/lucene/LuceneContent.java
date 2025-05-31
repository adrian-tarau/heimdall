package net.microfalx.heimdall.llm.lucene;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.DefaultContent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class LuceneContent extends DefaultContent {

    protected static final String DUMMY_TEXT = "Dummy";

    @Setter(AccessLevel.PROTECTED)
    private Embedding embedding;

    LuceneContent(TextSegment textSegment, Map<ContentMetadata, Object> metadata) {
        super(textSegment, metadata);
    }


}
