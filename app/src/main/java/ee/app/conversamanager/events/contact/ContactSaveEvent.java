package ee.app.conversamanager.events.contact;

import ee.app.conversamanager.model.database.dbCustomer;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactSaveEvent {

    private final dbCustomer contact;

    public ContactSaveEvent(dbCustomer contact) {
        this.contact = contact;
    }

    public dbCustomer getContact() {
        return contact;
    }

}