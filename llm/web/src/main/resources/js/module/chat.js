/*
* The LLM Chat Global Variables
 */
window.Chat = window.Chat || {};

const CHAT_MODAL_ID = "chat-modal";

/**
 * Loads a chat dialog
 *
 * @param {String} path the path
 * @param {Object} [params] the query parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @see DataSet.get
 */
Chat.loadModal = function (path, params, options) {
    let me = CodeEditor;
    Logger.info("Load dialog, path '" + path + "'");
    me.source = {
        path: path,
        params: params,
        options: options || {},
    };
    Application.get(path, params, function (data) {
        me.modal = Application.loadModal(CHAT_MODAL_ID, data);
    }, options);
}

/**
 * Returns the current chat ID.
 *
 * @param {String} chatId the chat identifier
 * @return {String} the identifier
 */
Chat.getCurrent = function (chatId) {
    if (Utils.isEmpty(chatId)) {
        chatId = $(".llm-chat").attr('id');
    }
    if (Utils.isEmpty(chatId)) throw new Error("No chat is currently selected");
    return chatId;
}

/**
 * Sends the current chat message.
 *
 * @param {String} chatId the chat identifier
 */
Chat.send = function (chatId) {
    let messageBox = $("#chat-message");
    let message = messageBox.text();
    messageBox.text("");
    if (Utils.isEmpty(message)) return;
    chatId = Chat.getCurrent(chatId);
    Application.post("question/" + chatId, {}, function (data) {
        $(".llm-chat-messages").append(data);
        let target = $('.llm-chat-messages .llm-chat-msg:last-child')
        Chat.receive(chatId, target);
    }, {
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
    chatId = Chat.getCurrent(chatId);
    target = $(target);
    let textElement = target.find('.llm-chat-text');
    let markdown = "";
    Application.Sse.start("tokens/" + chatId, function (data, event) {
        let json = JSON.parse(data);
        markdown += json.token;
        let html = marked.parse(markdown);
        textElement.html(html);
    });
}