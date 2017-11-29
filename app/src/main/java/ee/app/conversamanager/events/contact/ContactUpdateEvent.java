package ee.app.conversamanager.events.contact;

import ee.app.conversamanager.contact.ContactUpdateReason;
import ee.app.conversamanager.model.database.dbCustomer;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactUpdateEvent {

    private final dbCustomer contact;
    private final ContactUpdateReason reason;

    public ContactUpdateEvent(dbCustomer contact, ContactUpdateReason reason) {
        this.contact = contact;
        this.reason = reason;
    }

    public dbCustomer getContact() {
        return contact;
    }

    public ContactUpdateReason getReason() {
        return reason;
    }

}