package ee.app.conversamanager.interfaces;

import android.support.annotation.UiThread;

import java.util.List;

import ee.app.conversamanager.messaging.MessageUpdateReason;
import ee.app.conversamanager.model.database.dbMessage;

@UiThread
public interface OnMessageTaskCompleted {
    void MessagesGetAll(List<dbMessage> response);
    void MessageSent(dbMessage response);
    void MessageReceived(dbMessage response);
    void MessageDeleted(List<String> response);
    void MessageUpdated(dbMessage response, MessageUpdateReason reason);
    void onTypingMessage(String from, boolean isTyping);
}