package ee.app.conversamanager.events.message;

import ee.app.conversamanager.model.database.dbMessage;

/**
 * Created by edgargomez on 10/12/16.
 */

public class MessageOutgoingEvent {

    private final dbMessage message;

    public MessageOutgoingEvent(dbMessage message) {
        this.message = message;
    }

    public dbMessage getMessage() {
        return message;
    }

}