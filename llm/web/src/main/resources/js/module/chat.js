/*
* The LLM Chat Global Variables
 */
window.Chat = window.Chat || {};

const CHAT_MODAL_ID = "chat-modal";
const CHAT_PATH = "ai/chat/";

/**
 * Loads a chat dialog
 *
 * @param {String} path the path
 * @param {Object} [params] the query parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @see DataSet.get
 */
Chat.loadModal = function (path, params, options) {
    let me = Chat;
    path = CHAT_PATH + path;
    Logger.info("Load dialog, path '" + path + "'");
    options = options || {};
    options.self = false;
    Application.get(path, params, function (data) {
        me.modal = Application.loadModal(CHAT_MODAL_ID, data);
    }, options);
}

/**
 * Starts a chat by asking a chat for a given prompt and data set.
 *
 * @param {String} prompt the prompt identifier
 * @param {String} dataSet the data set request identifier
 */
Chat.prompt = function (prompt, dataSet) {
    let me = Chat;
    if (Utils.isEmpty(prompt)) throw new Error("Prompt is required");
    if (Utils.isEmpty(dataSet)) throw new Error("DataSet is required");
    me.loadModal(prompt, {dataSet: dataSet});
}

/**
 * Returns the current chat ID.
 *
 * If the chatId is not provided, it will try to find the last chat element in the DOM.
 *
 * @param {String} [chatId] the chat identifier
 * @param {Boolean} [required=true] true if the identifier is required, false otherwise
 * @return {String} the identifier
 */
Chat.getCurrent = function (chatId, required) {
    if (Utils.isUndefined(required)) required = true;
    if (Utils.isEmpty(chatId)) {
        chatId = $(".llm-chat").attr('id');
    }
    if (Utils.isEmpty(chatId) && required) throw new Error("No chat is currently selected");
    return chatId;
}

/**
 * Displays information about the model.
 */
Chat.showModel = function () {
    Application.get(CHAT_PATH + "info/model/" + Chat.getCurrent(), {}, function (data) {
        Application.loadModal(CHAT_MODAL_ID, data);
    }, {self: false});
}

/**
 * Displays information about the prompt.
 */
Chat.showPrompt = function () {
    Application.get("info/prompt/" + Chat.getCurrent(), {}, function (data) {
        Application.loadModal(CHAT_MODAL_ID, data);
    }, {self: false});
}

/**
 * Returns the message input element.
 */
Chat.getMessageInput = function () {
    let messageInput = $("#chat-message");
    if (messageInput.length === 0) {
        throw new Error("Chat message input not found");
    }
    return messageInput;
}

/**
 * Sends the current chat message.
 *
 * @param {String} [chatId] the chat identifier
 */
Chat.send = function (chatId) {
    let me = Chat;
    let messageInput = me.getMessageInput();
    let message = messageInput.text();
    messageInput.text("");
    me._chatBody = null;
    if (Utils.isEmpty(message)) return;
    chatId = me.getCurrent(chatId);
    Application.post("question/" + chatId, {}, function (data) {
        $(".llm-chat-messages").append(data);
        me.focusMessage();
        let target = $('.llm-chat-messages .llm-chat-msg:last-child')
        me.receive(chatId, target);
    }, {
        self: false,
        data: message,
        contentType: "text/plain"
    });

}

/**
 * Receives tokens from the server for a given chat.
 *
 * @param {String} chatId the chat identifier
 * @package {String|jQuery} target the target element to display the tokens
 */
Chat.receive = function (chatId, target) {
    let me = Chat;
    chatId = me.getCurrent(chatId);
    target = $(target);
    let textElement = target.find('.llm-chat-text');
    let markdown = "";
    Application.Sse.start("tokens/" + chatId, function (data, event) {
        let json = JSON.parse(data);
        markdown += json.token;
        let html = marked.parse(markdown);
        textElement.html(html);
        me.focusMessage();
    }, {}, {self: false});
}

/**
 * Focuses the chat body, scrolling to the bottom.
 */
Chat.focusMessage = function () {
    let me = Chat;
    if (!me._chatBody) {
        me._chatBody = $(".llm-chat-body");
    }
    me._chatBody.scrollTop(me._chatBody[0].scrollHeight);
}

/**
 * Focuses the chat input field.
 */
Chat.focusInput = function () {
    let me = Chat;
    me.getMessageInput().focus();
}

/**
 * Initializes the chat module.
 */
Chat.start = function () {
    let me = Chat;
    if (me.getCurrent(null, false)) {
        me.getMessageInput().keydown(function (e) {
            if (e.keyCode === 13 && !e.shiftKey) {
                e.preventDefault();
                me.send();
            }
        });
        me.focusInput();
    }
}

Application.bind("start", Chat.start);
Application.bind("chat.prompt", Chat.prompt);
Application.bind("chat.info.model", Chat.showModel);
Application.bind("chat.info.prompt", Chat.showPrompt);