package ee.app.conversamanager.interfaces;

import java.util.List;

import androidx.annotation.UiThread;
import ee.app.conversamanager.messaging.MessageDeleteReason;
import ee.app.conversamanager.messaging.MessageUpdateReason;
import ee.app.conversamanager.model.database.dbMessage;

@UiThread
public interface OnMessageTaskCompleted {
    void MessagesGetAll(List<dbMessage> response);
    void MessageSent(dbMessage response);
    void MessageReceived(dbMessage response);
    void MessageDeleted(List<String> response, MessageDeleteReason reason);
    void MessageUpdated(dbMessage response, MessageUpdateReason reason);
    void onTypingMessage(String from, boolean isTyping);
}