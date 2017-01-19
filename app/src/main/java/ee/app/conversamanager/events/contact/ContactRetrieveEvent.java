package ee.app.conversamanager.events.contact;

import java.util.List;

import ee.app.conversamanager.model.database.dbCustomer;

/**
 * Created by edgargomez on 10/12/16.
 */

public class ContactRetrieveEvent {

    private final List<dbCustomer> list_response;

    public ContactRetrieveEvent(List<dbCustomer> list_response) {
        this.list_response = list_response;
    }

    public List<dbCustomer> getListResponse() {
        return list_response;
    }

}