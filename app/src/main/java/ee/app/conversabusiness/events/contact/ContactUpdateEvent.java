package ee.app.conversabusiness.events.contact;

import ee.app.conversabusiness.contact.ContactUpdateReason;
import ee.app.conversabusiness.model.database.dbCustomer;

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