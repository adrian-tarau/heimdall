package net.microfalx.heimdall.llm.web;

import jakarta.websocket.server.PathParam;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.StringUtils;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Iterator;

import static net.microfalx.heimdall.llm.core.LlmUtils.getChatThreadPool;

@Controller("SystemChatController")
@RequestMapping("/system/ai/chat")
@DataSet(model = Chat.class, timeFilter = false)
public class ChatController extends SystemDataSetController<Chat,Integer> {

    @Autowired
    private LlmService llmService;

    @PostMapping("start/{id}")
    public JsonResponse<?> start(@PathParam("id") String id) {
        Prompt prompt = Prompt.empty();
        net.microfalx.heimdall.llm.api.Chat chat = llmService.createChat(prompt, llmService.getDefaultModel());
        return JsonResponse.success().setPayload(chat.getId());
    }

    @PostMapping("message/{id}")
    public SseEmitter message(@PathParam("id") String id, @RequestBody String message) {
        net.microfalx.heimdall.llm.api.Chat chat = llmService.getChat(id);
        Iterator<String> stream = chat.chat(message);
        SseEmitter emitter = new SseEmitter();
        ThreadPool threadPool = getChatThreadPool(llmService);
        threadPool.execute(new ChatMessageTask(emitter, chat, stream));
        return emitter;
    }

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Chat, Field<Chat>, Integer> dataSet, Chat model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Chat, Field<Chat>, Integer> dataSet, Chat model, State state) {
        super.afterPersist(dataSet, model, state);
        llmService.reload();
    }

    private static class ChatMessageTask implements Runnable {

        private final SseEmitter emitter;
        private final net.microfalx.heimdall.llm.api.Chat chat;
        private final Iterator<String> stream;

        public ChatMessageTask(SseEmitter emitter, net.microfalx.heimdall.llm.api.Chat chat,
                               Iterator<String> stream) {
            this.emitter = emitter;
            this.chat = chat;
            this.stream = stream;
        }

        private void sendToken(String token) {
            SseEmitter.SseEventBuilder builder = SseEmitter.event().id(chat.getId());
            builder.data(token);
            try {
                emitter.send(builder);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }

        @Override
        public void run() {
            try {
                while (stream.hasNext()) {
                    String token = stream.next();
                    sendToken(token);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }
    }
}
