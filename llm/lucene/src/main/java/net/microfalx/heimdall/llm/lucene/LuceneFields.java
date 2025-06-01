package net.microfalx.heimdall.llm.lucene;

class LuceneFields {

    static final String INDEX_NAME = "embedding";
    static final String ID_FIELD_NAME = "id";
    static final String CONTENT_FIELD_NAME = "content";
    static final String CONTENT_URI_FIELD_NAME = "content-uri";
    static final String TOKEN_COUNT_FIELD_NAME = "estimated-token-count";
    static final String EMBEDDING_FIELD_NAME = "embedding";

    static final String[] FIELDS = {
            CONTENT_FIELD_NAME,
            CONTENT_URI_FIELD_NAME,
            TOKEN_COUNT_FIELD_NAME,
            EMBEDDING_FIELD_NAME
    };
}
