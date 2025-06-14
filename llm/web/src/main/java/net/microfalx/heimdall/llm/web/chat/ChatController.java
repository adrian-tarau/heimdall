package net.microfalx.heimdall.llm.web.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.security.SecurityUtils;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.controller.PageController;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.heimdall.llm.core.MessageImpl;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.annotation.Name;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.heimdall.llm.core.LlmUtils.getChatThreadPool;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.ThreadUtils.sleepMillis;

@Controller()
@RequestMapping("/ai/chat")
@Help("llm/chat")
@Name("Chat")
public class ChatController extends PageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    private static final String END_OF_DATA = "$END_OF_DATA$";
    private static final Map<String, TokenStream> chatAnswer = new ConcurrentHashMap<>();

    @Autowired private HelpService helpService;
    @Autowired private LlmService llmService;

    @GetMapping("")
    public String start(Model model) {
        return doStart(model, Prompt.empty(), Mode.DASHBOARD);
    }

    @GetMapping("{id}")
    public String start(Model model, @PathVariable("id") String promptId) {
        Prompt prompt = llmService.getPrompt(promptId);
        return doStart(model, prompt, Mode.DIALOG);
    }

    @GetMapping("info/model/{id}")
    public String showModel(Model model, @PathVariable("id") String chatId) {
        net.microfalx.heimdall.llm.api.Chat chat = llmService.getChat(chatId);
        updateModel(model, chat);
        model.addAttribute("title", "Model");
        model.addAttribute("content", renderMarkdown(chat.getDescription()));
        return "llm/chat :: dialog";
    }

    @GetMapping("info/prompt/{id}")
    public String showPrompth(Model model, @PathVariable("id") String chatId) {
        net.microfalx.heimdall.llm.api.Chat chat = llmService.getChat(chatId);
        updateModel(model, chat);
        model.addAttribute("title", "Model");
        model.addAttribute("content", renderMarkdown(chat.getSystemMessage().getText()));
        model.addAttribute("modalClasses", "modal-lg");
        return "llm/chat :: dialog";
    }

    @PostMapping(value = "question/{id}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public String question(Model model, @PathVariable("id") String chatId, @RequestBody String message) {
        net.microfalx.heimdall.llm.api.Chat chat = llmService.getChat(chatId);
        TokenStream stream = chat.chat(message);
        chatAnswer.put(chatId, stream);
        updateModel(model, chat);
        List<Message> messages = new ArrayList<>();
        model.addAttribute("messages", messages);
        messages.add(MessageImpl.create(Message.Type.USER, message));
        messages.add(MessageImpl.create(Message.Type.MODEL, "Thinking..."));
        return "llm/chat :: question";
    }

    @GetMapping("tokens/{id}")
    public SseEmitter tokens(@PathVariable("id") String chatId) {
        net.microfalx.heimdall.llm.api.Chat chat = llmService.getChat(chatId);
        TokenStream stream = chatAnswer.get(chatId);
        SseEmitter emitter = new SseEmitter();
        ThreadPool threadPool = getChatThreadPool(llmService);
        threadPool.execute(new ChatMessageTask(emitter, chat, stream));
        return emitter;
    }

    private String doStart(Model model, Prompt prompt, Mode mode) {
        net.microfalx.heimdall.llm.api.Chat chat = llmService.createChat(prompt, llmService.getDefaultModel());
        updateModel(model, chat);
        updateIntro(model);
        model.addAttribute("mode", mode);
        if (mode == Mode.DASHBOARD) {
            return "llm/dashboard";
        } else {
            return "llm/dialog";
        }
    }

    private String renderMarkdown(String text) {
        return renderMarkdown(Resource.text(text));
    }

    private String renderMarkdown(Resource resource) {
        try {
            return helpService.render(resource);
        } catch (IOException e) {
            LOGGER.error("Failed to render markdown: " + resource.toURI(), e);
            return "#Error: content not available";
        }
    }

    private void updateModel(Model model, net.microfalx.heimdall.llm.api.Chat chat) {
        requireNonNull(chat);
        updateHelp(model);
        model.addAttribute("chat", chat);
        model.addAttribute("chatTools", new ChatTools(chat));
        updateMenu(model);
    }

    private void updateIntro(Model model) {
        StringWriter writer = new StringWriter();
        try {
            helpService.render("llm/chat-intro", writer);
            model.addAttribute("chatIntro", writer.toString());
        } catch (IOException e) {
            LOGGER.error("Failed to render chat intro", e);
        }
    }

    private void updateMenu(Model model) {
        Menu menu = new Menu();
        menu.add(new Item().setAction("chat.model").setText("Model").setIcon("fa-solid fa-square-binary")
                .setDescription("Displays information about the model"));
        menu.add(new Item().setAction("chat.prompt").setText("Prompt").setIcon("fa-solid fa-terminal")
                .setDescription("Displays information about the prompt"));
        model.addAttribute("chatInfoMenu", menu);
    }

    public enum Mode {
        DASHBOARD, DIALOG
    }

    public class ChatTools {

        private final Chat chat;

        public ChatTools(Chat chat) {
            this.chat = chat;
        }

        public String getMessageCssClass(Message message) {
            if (message.getType() == Message.Type.USER) {
                return EMPTY_STRING;
            } else {
                return "end";
            }
        }

        public String getMessageImageCssClass(Message message) {
            return switch (message.getType()) {
                case USER -> "fa-solid fa-user-tie";
                case MODEL -> "fa-solid fa-robot";
                case SYSTEM -> "fa-solid fa-terminal";
                case CUSTOM -> "fa-solid fa-comment";
                default -> EMPTY_STRING;
            };
        }

        public Collection<Message> getMessages(Chat chat) {
            return chat.getMessages().stream()
                    .filter(message -> message.getType() != Message.Type.SYSTEM)
                    .toList();
        }

        public String getUser(Message message) {
            if (message.getType() == Message.Type.USER) {
                return SecurityUtils.getDisplayName(chat.getUser());
            } else {
                return "Heimdall";
            }
        }

        public String renderMessageText(Message message) {
            try {
                return helpService.render(Resource.text(message.getText()));
            } catch (IOException e) {
                LOGGER.error("Failed to render message text for chat: {}", chat.getId(), e);
                return "#Error: failed to render message text";
            }
        }
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Token {
        private final int index;
        private final String token;
    }

    private static class ChatMessageTask implements Runnable {

        private final SseEmitter emitter;
        private final net.microfalx.heimdall.llm.api.Chat chat;
        private final TokenStream stream;
        private final AtomicInteger index = new AtomicInteger(1);
        private final ObjectMapper objectMapper;

        public ChatMessageTask(SseEmitter emitter, net.microfalx.heimdall.llm.api.Chat chat, TokenStream stream) {
            this.emitter = emitter;
            this.chat = chat;
            this.stream = stream;
            this.objectMapper = new ObjectMapper();
        }

        private void sendToken(String token, boolean asText) {
            SseEmitter.SseEventBuilder builder = SseEmitter.event().id(chat.getId());
            try {
                String data = token;
                if (!asText) {
                    data = objectMapper.writeValueAsString(new Token(index.getAndIncrement(), token));
                }
                builder.data(data);
                emitter.send(builder);
            } catch (IllegalStateException e) {
                ExceptionUtils.throwException(e);
            } catch (Exception e) {
                LOGGER.error("Failed to send token for chat: {}", chat.getId(), e);
            }
        }

        @Override
        public void run() {
            if (stream == null) {
                emitter.completeWithError(new IllegalStateException("No message stream available for chat: " + chat.getId()));
            } else {
                try {
                    while (!stream.isComplete()) {
                        while (stream.hasNext()) {
                            String token = stream.next();
                            sendToken(token, false);
                        }
                        sleepMillis(20);
                    }
                    sendToken(END_OF_DATA, true);
                } catch (IllegalStateException e) {
                    LOGGER.warn("Communication error with client for chat: {}", chat.getId(), e);
                } catch (Exception e) {
                    LOGGER.error("Failed to process tokens for chat: {}", chat.getId(), e);
                    emitter.completeWithError(e);
                } finally {
                    emitter.complete();
                }
            }
        }
    }
}
