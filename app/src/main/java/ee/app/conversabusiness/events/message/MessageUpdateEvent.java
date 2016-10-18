package ee.app.conversabusiness.events.message;

import ee.app.conversabusiness.messaging.MessageUpdateReason;
import ee.app.conversabusiness.model.database.dbMessage;

/**
 * Created by edgargomez on 10/12/16.
 */

public class MessageUpdateEvent {

    private final MessageUpdateReason action;
    private final dbMessage message;

    public MessageUpdateEvent(dbMessage message, MessageUpdateReason action) {
        this.message = message;
        this.action = action;
    }

    public MessageUpdateReason getReason() {
        return action;
    }

    public dbMessage getMessage() {
        return message;
    }

}