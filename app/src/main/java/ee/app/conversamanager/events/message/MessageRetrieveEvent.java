package ee.app.conversamanager.events.message;

import java.util.List;

import ee.app.conversamanager.model.database.dbMessage;

/**
 * Created by edgargomez on 10/12/16.
 */

public class MessageRetrieveEvent {

    private final List<dbMessage> messageList;

    public MessageRetrieveEvent(List<dbMessage> messageList) {
        this.messageList = messageList;
    }

    public List<dbMessage> getMessageList() {
        return messageList;
    }

}