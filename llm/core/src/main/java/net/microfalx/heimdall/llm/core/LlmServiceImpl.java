package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetExport;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.heimdall.llm.lucene.LuceneEmbeddingStore;
import net.microfalx.lang.*;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.rocksdb.RocksDbResource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.TimeUtils.millisSince;

@Service
public class LlmServiceImpl extends ApplicationContextSupport implements LlmService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmService.class);

    @Autowired
    private IndexService indexService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private LlmPersistence persistence;

    @Autowired(required = false)
    @Getter(AccessLevel.PROTECTED)
    private LlmProperties properties = new LlmProperties();

    private File variableDirectory;
    private LuceneEmbeddingStore embeddingStore;
    private ContentRetriever contentRetriever;
    private ChatMemoryStore chatStore;
    private volatile LlmCache cache = new LlmCache(this, null);
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    private final Collection<LlmListener> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Provider.Factory> providerFactories = new CopyOnWriteArrayList<>();
    private final Map<String, net.microfalx.heimdall.llm.api.Chat> activeChats = new ConcurrentHashMap<>();
    private final Collection<net.microfalx.heimdall.llm.api.Chat> closedChats = new CopyOnWriteArrayList<>();

    private volatile Model defaultModel;
    private volatile Model defaultEmbeddingModel;

    private ThreadPool chatPool;
    private ThreadPool embeddingPool;

    private Resource chatsResource;
    private static final Map<String, Long> lastAutoSave = new ConcurrentHashMap<>();

    public LlmServiceImpl() {
        // Workaround for classes not being loaded by Spring
        warmClassesWorkaround();
    }

    ChatMemoryStore getChatStore() {
        return chatStore;
    }

    ContentRetriever getContentRetriever() {
        return contentRetriever;
    }

    @Override
    public Chat createChat() {
        return createChat(Prompt.empty());
    }

    @Override
    public Chat createChat(Prompt prompt) {
        requireNonNull(prompt);
        Model model = getDefaultModel();
        if (prompt.getModel() != null) model = prompt.getModel();
        return createChat(prompt, model);
    }

    @Override
    public Chat createChat(Model model) {
        return createChat(Prompt.empty(), model);
    }

    public net.microfalx.heimdall.llm.api.Chat createChat(Prompt prompt, Model model) {
        requireNonNull(prompt);
        requireNonNull(model);
        if (model.isEmbedding()) throw new LlmException("Model '" + model.getId() + "' does not support chatting");
        Chat.Factory chatFactory = model.getProvider().getChatFactory();
        net.microfalx.heimdall.llm.api.Chat chat = chatFactory.createChat(prompt, model);
        activeChats.put(toIdentifier(chat.getId()), chat);
        if (chat instanceof AbstractChat abstractChat) abstractChat.initialize(this);
        ThreadPool.get().execute(() -> persistence.execute(chat));
        return chat;
    }

    @Override
    public Collection<Chat> getChats(Principal principal) {
        requireNonNull(principal);
        Collection<Chat> chats = new ArrayList<>();
        activeChats.values().forEach(chat -> {
            if (principal.equals(chat.getUser())) chats.add(chat);
        });
        return chats;
    }

    @Override
    public Chat getChat(String id) {
        requireNonNull(id);
        Chat chat = activeChats.get(toIdentifier(id));
        if (chat == null) {
            throw new LlmException("Chat '" + id + "' not found");
        }
        return chat;
    }

    @Override
    public Embedding embed(String text) {
        return createEmbedding(getDefaultEmbeddingModel(), text);
    }

    @Override
    public Embedding embed(String modelId, String text) {
        return createEmbedding(getModel(modelId), text);
    }

    public Embedding createEmbedding(Model model, String text) {
        requireNonNull(model);
        if (!model.isEmbedding()) throw new LlmException("Model '" + model.getId() + "' does not support embedding");
        return model.getProvider().getEmbeddingFactory().createEmbedding(model, text);
    }

    @Override
    public Model getDefaultModel() {
        if (defaultModel != null) return defaultModel;
        if (isNotEmpty(properties.getDefaultModel()) && isNotEmpty(properties.getDefaultProvider())) {
            String defaultModelId = properties.getDefaultProvider() + "." + properties.getDefaultModel();
            defaultModel = cache.findModel(defaultModelId);
            if (defaultModel == null) {
                LOGGER.warn("A model with identifier '{}' not found, falling back to configured model", defaultModelId);
            }
        }
        if (defaultModel == null) {
            defaultModel = getModels().stream().filter(model -> model.isDefault() && !model.isEmbedding())
                    .findFirst().orElseThrow(() -> new LlmNotFoundException("No default model found"));
        }
        return defaultModel;
    }

    @Override
    public Model getDefaultEmbeddingModel() {
        if (defaultEmbeddingModel != null) return defaultEmbeddingModel;
        defaultEmbeddingModel = getModels().stream().filter(model -> model.isDefault() && model.isEmbedding()).findFirst().orElseThrow(
                () -> new LlmNotFoundException("No default embedding model found"));
        return defaultEmbeddingModel;
    }

    @Override
    public Collection<Model> getModels() {
        return unmodifiableCollection(cache.getModels().values());
    }

    @Override
    public Model getModel(String id) {
        return cache.getModel(id);
    }

    @Override
    public Collection<net.microfalx.heimdall.llm.api.Chat> getActiveChats() {
        return List.of();
    }

    @Override
    public Iterable<net.microfalx.heimdall.llm.api.Chat> getHistoricalChats() {
        return null;
    }

    @Override
    public Collection<Provider> getProviders() {
        return unmodifiableCollection(cache.getProviders().values());
    }

    @Override
    public void registerProvider(Provider provider) {
        requireNonNull(provider);
        cache.registerProvider(provider);
        persistProvider(provider);
    }

    @Override
    public Collection<Tool> getTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    @Override
    public Tool getTool(String id) {
        requireNonNull(id);
        Tool tool = tools.get(toIdentifier(id));
        if (tool == null) throw new LlmNotFoundException("Tool '" + id + "' not found");
        return tool;
    }

    @Override
    public void registerTool(Tool tool) {
        requireNonNull(tool);
        LOGGER.info("Registering tool {}", tool.getId());
        tools.put(toIdentifier(tool.getId()), tool);
    }

    /**
     * Returns the final prompt text for the given model and prompt.
     *
     * @param chat the chat to use
     * @return a non-null string
     */
    public String getSystemMessage(Chat chat) {
        SystemMessageBuilder builder = new SystemMessageBuilder(this, chat.getModel(), chat.getPrompt());
        Map<String, Object> variables = new HashMap<>();
        getDataSetAsJson(chat, variables);
        return PromptTemplate.from(builder.build()).apply(variables).text();
    }

    @Override
    public void reload() {
        if (!properties.isPersistenceEnabled()) return;
        LlmCache cache = new LlmCache(this, this.cache);
        cache.setApplicationContext(getApplicationContext());
        cache.load();
        this.defaultModel = null;
        this.defaultEmbeddingModel = null;
        this.cache = cache;
    }

    @Override
    public Collection<Prompt> getPrompts() {
        return unmodifiableCollection(cache.getPrompts().values());
    }

    @Override
    public Collection<Prompt> getPrompts(Collection<String> tags) {
        Collection<Prompt> filteredPrompts = new ArrayList<>();
        for (Prompt prompt : cache.getPrompts().values()) {
            for (String tag : tags) {
                if (prompt.getTags().contains(tag)) filteredPrompts.add(prompt);
            }
        }
        return filteredPrompts;
    }

    @Override
    public Collection<Prompt> getPrompts(String... tags) {
        return getPrompts(Arrays.asList(tags));
    }

    @Override
    public Prompt getPrompt(String id) {
        return cache.getPrompt(id);
    }

    @Override
    public void registerPrompt(Prompt prompt) {
        requireNonNull(prompt);
        cache.registerPrompt(prompt);
        persistence.execute(prompt);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeApplicationContext();
        registerLibraryPaths();
        initThreadPools();
        initDirectories();
        initListeners();
        initProviderFactories();
        registerProviders();
        initializeChatStore();
        initializeEmbeddingStore();
        initResources();
        initTask();
    }

    @PreDestroy
    protected void destroy() {
        if (embeddingStore != null) embeddingStore.close();
    }

    public ThreadPool getChatPool() {
        return ObjectUtils.defaultIfNull(chatPool, ThreadPool.get());
    }

    public ThreadPool getEmbeddingPool() {
        return ObjectUtils.defaultIfNull(embeddingPool, ThreadPool.get());
    }

    /**
     * Closes the given chat and persists it.
     *
     * @param chat the chat to close
     */
    void closeChat(Chat chat) {
        requireNonNull(chat);
        activeChats.remove(chat.getId());
        closedChats.add(chat);
        persistence.execute(chat);
    }

    /**
     * Returns any data set associated with the chat as a JSON string.
     *
     * @param chat the chat to get the data set from
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getDataSetAsJson(Chat chat, Map<String, Object> variables) {
        requireNonNull(chat);

        DataSetRequest request = chat.getFeature(DataSetRequest.class);
        TransactionTemplate transactionTemplate = getTransactionTemplate(request.getDataSet());
        if (transactionTemplate != null) {
            transactionTemplate.execute(status -> doGetDataSetAsJsonUnder(chat, variables));
        } else {
            doGetDataSetAsJsonUnder(chat, variables);
        }
    }

    Object doGetDataSetAsJsonUnder(Chat chat, Map<String, Object> variables) {
        DataSetRequest<?, ?, ?> request = chat.getFeature(DataSetRequest.class);
        Page<?> page = null;
        if (request != null) {
            page = fireGetDataSet(chat, request);
        }
        update(chat, request, "SCHEMA", true, page, variables);
        update(chat, request, "DATASET", false, page, variables);
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void update(Chat chat, DataSetRequest dataSetRequest, String name, boolean schema,
                        Page page, Map<String, Object> variables) {
        Resource resource;
        if (page != null) {
            DataSetExport<Object, Field<Object>, Object> export = DataSetExport.create(DataSetExport.Format.JSON)
                    .setIncludeMetadata(schema).setIncludeData(!schema);
            export.initialize(getApplicationContext());
            resource = export.export(dataSetRequest.getDataSet(), page);
        } else {
            LOGGER.warn("No data set found for chat {}", chat.getId());
            resource = Resource.text(JSON_ERROR);
        }
        String json = JSON_ERROR;
        try {
            json = resource.loadAsString();
        } catch (IOException e) {
            LOGGER.warn("Failed to load data set for chat {}", chat.getId());
        }
        variables.put(name, json);
    }

    /**
     * Returns a fragment of the prompt text.
     *
     * @param model    the model to use
     * @param prompt   the prompt to use
     * @param fragment the fragment to return
     * @return the text of the fragment, never null
     */
    String getPromptFragment(Model model, Prompt prompt, Prompt.Fragment fragment, String text) {
        requireNonNull(model);
        requireNonNull(prompt);
        requireNonNull(fragment);
        String originalText = text;
        for (LlmListener listener : listeners) {
            text = listener.augment(model, prompt, fragment, text);
            if (isNotEmpty(text)) break;
        }
        return isEmpty(text) ? originalText : text;
    }

    /**
     * Stores the chat messages serialized in an external resource.
     *
     * @param chat the snapshot
     * @return the resource where the snapshot was stored
     * @throws IOException I/O exception if snapshot cannot be stored
     */
    Resource writeChatMessages(Chat chat) throws IOException {
        Resource directory = chatsResource.resolve(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY);
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()));
        if (!directory.exists()) directory.create();
        List<ChatMessage> messages = chatStore.getMessages(chat.getId());
        String json = ChatMessageSerializer.messagesToJson(messages);
        IOUtils.appendStream(target.getWriter(), new StringReader(json));
        return target;
    }

    private void initDirectories() {
        variableDirectory = JvmUtils.getVariableDirectory("llm");
    }

    private void initListeners() {
        Collection<LlmListener> loadedListeners = ClassUtils.resolveProviderInstances(LlmListener.class);
        LOGGER.info("Register {} listeners", loadedListeners.size());
        for (LlmListener listener : loadedListeners) {
            LOGGER.debug(" - {}", ClassUtils.getName(loadedListeners));
            if (listener instanceof ApplicationContextAware applicationContextAware) {
                applicationContextAware.setApplicationContext(getApplicationContext());
            }
            this.listeners.add(listener);
        }
    }

    private void initProviderFactories() {
        Collection<Provider.Factory> loadedProviderFactories = ClassUtils.resolveProviderInstances(Provider.Factory.class);
        LOGGER.info("Register {} provider factories", loadedProviderFactories.size());
        for (Provider.Factory providerFactory : loadedProviderFactories) {
            LOGGER.debug(" - {}", ClassUtils.getName(providerFactory));
            this.providerFactories.add(providerFactory);
        }
    }

    private void initializeEmbeddingStore() {
        this.embeddingStore = new LuceneEmbeddingStore(this, indexService, searchService)
                .setThreadPool(getEmbeddingPool()).setEnabled(properties.isEmbeddingEnabled());
        this.indexService.registerListener(this.embeddingStore);
        this.contentRetriever = this.embeddingStore.getContentRetriever();
    }

    private void initializeChatStore() {
        this.chatStore = new InMemoryChatMemoryStore();
    }

    private void initializeApplicationContext() {
        persistence.llmService = this;
        persistence.setApplicationContext(getApplicationContext());
        cache.setApplicationContext(getApplicationContext());
    }

    private <M, F extends Field<M>, ID> TransactionTemplate getTransactionTemplate(DataSet<M, F, ID> dataSet) {
        Metadata<M, F, ID> metadata = dataSet.getMetadata();
        PlatformTransactionManager transactionManager = getBean(PlatformTransactionManager.class);
        if (transactionManager != null && metadata.hasAnnotation(Entity.class)) {
            return new TransactionTemplate(transactionManager);
        } else {
            return null;
        }
    }

    private void initResources() {
        chatsResource = getSharedResource().resolve("chats", Resource.Type.DIRECTORY);
        if (chatsResource.isLocal()) {
            LOGGER.info("Chat messages are stored in a RocksDB database: {}", chatsResource);
            Resource dbStatementsResource = RocksDbResource.create(chatsResource);
            try {
                dbStatementsResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize statement store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("db/statements", dbStatementsResource);
        } else {
            LOGGER.info("Database statements are stored in a remote storage: {}", chatsResource);
        }
    }

    private void initThreadPools() {
        chatPool = ThreadPoolFactory.create("LLM").create();
        embeddingPool = ThreadPoolFactory.create("Embedding").create();
    }

    private void persistProvider(Provider provider) {
        if (!properties.isPersistenceEnabled()) return;
        for (Model model : provider.getModels()) {
            persistence.execute(model);
        }
    }

    private void initTask() {
        ThreadPool threadPool = ThreadPool.get();
        threadPool.execute(this::reload);
        threadPool.execute(this::fireStartEvent);
        threadPool.scheduleAtFixedRate(new MaintenanceTask(), Duration.ofSeconds(60));
    }

    private void fireStartEvent() {
        for (LlmListener listener : listeners) {
            try {
                listener.onStart(this);
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to notify listener {}", ClassUtils.getName(listener));
            }
        }
    }

    private <M, F extends Field<M>, ID> Page<M> fireGetDataSet(Chat chat, DataSetRequest<M, F, ID> request) {
        for (LlmListener listener : listeners) {
            try {
                Page<M> page = listener.getPage(chat, request);
                if (page != null) return page;
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to build data set result for {} with listener {}", chat.getName(), ClassUtils.getName(listener));
            }
        }
        return null;
    }

    private Resource getSharedResource() {
        return resourceService.getShared("llm");
    }

    private void registerProviders() {
        for (Provider.Factory providerFactory : providerFactories) {
            try {
                if (providerFactory instanceof AbstractProviderFactory abstractProviderFactory) {
                    abstractProviderFactory.setProperties(properties);
                }
                Provider provider = providerFactory.createProvider();
                if (provider == null) {
                    LOGGER.error("Provider factory {} returned NULL", ClassUtils.getName(providerFactory));
                } else {
                    registerProvider(provider);
                }
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to create provider with factory {}", ClassUtils.getName(providerFactory));
            }
        }
    }

    private void warmClassesWorkaround() {
        try {
            Class.forName("net.microfalx.heimdall.llm.lucene.LuceneContentRetriever");
            Class.forName("net.microfalx.heimdall.llm.lucene.LuceneEmbeddingStore");
        } catch (Exception e) {
            rethrowException(e);
        }
    }

    private void registerLibraryPaths() {
        File djlCache = JvmUtils.getCacheDirectory("djl");
        System.setProperty("ENGINE_CACHE_DIR", validateDirectoryExists(new File(djlCache, "engine")).getAbsolutePath());
        System.setProperty("DJL_CACHE_DIR", validateDirectoryExists(new File(djlCache, "cache")).getAbsolutePath());
        System.setProperty("DJL_OFFLINE", Boolean.toString(properties.isOffline()));
    }

    private void processPendingChats() {
        for (Chat chat : activeChats.values()) {
            long lastActivity = ((AbstractChat) chat).lastActivity.get();
            boolean changed = ((AbstractChat) chat).changed.get();
            long lastAutoSaveForChat = lastAutoSave.computeIfAbsent(chat.getId(), s -> TimeUtils.oneHourAgo());
            if (changed && millisSince(lastAutoSaveForChat) > properties.getChatAutoSaveInterval().toMillis()) {
                persistence.execute(chat);
                lastAutoSave.put(chat.getId(), currentTimeMillis());
                ((AbstractChat) chat).changed.set(false);
            }
            if (millisSince(lastActivity) > properties.getChatTimeout().toMillis()) {
                LOGGER.info("Closing chat {} due to inactivity", chat.getId());
                chat.close();
            }
        }
    }

    private int getNextSequence() {
        return resourceSequences.computeIfAbsent(LocalDate.now(), localDate -> {
            int start = 1;
            if (STARTUP.toLocalDate().equals(localDate)) {
                start = STARTUP.toLocalTime().toSecondOfDay();
            }
            return new AtomicInteger(start);
        }).getAndIncrement();
    }

    class MaintenanceTask implements Runnable {

        @Override
        public void run() {
            try {
                processPendingChats();
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to process pending chats");
            }
        }
    }

    private static final String JSON_ERROR = "{message = 'No data set found'}";
    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String FILE_NAME_FORMAT = "%09d";
    private static final LocalDateTime STARTUP = LocalDateTime.now();
    private static final Map<LocalDate, AtomicInteger> resourceSequences = new ConcurrentHashMap<>();
}
