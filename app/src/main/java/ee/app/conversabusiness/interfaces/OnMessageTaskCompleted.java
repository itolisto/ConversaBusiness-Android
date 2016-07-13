package ee.app.conversabusiness.interfaces;

import ee.app.conversabusiness.response.MessageResponse;


public interface OnMessageTaskCompleted {
    void MessagesGetAll(MessageResponse response);
    void MessageSent(MessageResponse response);
    void MessageDeleted(MessageResponse response);
    void MessageUpdated(MessageResponse response);
}