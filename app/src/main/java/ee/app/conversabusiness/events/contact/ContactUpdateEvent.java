package ee.app.conversabusiness.events.contact;

import ee.app.conversabusiness.model.database.dbCustomer;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactUpdateEvent {

    private final dbCustomer contact;

    public ContactUpdateEvent(dbCustomer contact) {
        this.contact = contact;
    }

    public dbCustomer getContact() {
        return contact;
    }

}