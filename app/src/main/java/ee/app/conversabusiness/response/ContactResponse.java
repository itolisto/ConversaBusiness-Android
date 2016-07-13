package ee.app.conversabusiness.response;

import java.util.List;

import ee.app.conversabusiness.model.Database.dCustomer;

/**
 * Created by edgargomez on 7/8/16.
 */
public class ContactResponse {

    public int getActionCode() {
        return actionCode;
    }

    public dCustomer getCustomer() {
        return customer;
    }

    public List<dCustomer> getCustomers() {
        return customers;
    }

    private int actionCode;
    private dCustomer customer;
    private List<dCustomer> customers;

    public ContactResponse(int actionCode) {
        this.actionCode = actionCode;
        this.customer = null;
        this.customers = null;
    }

    public ContactResponse(int actionCode, dCustomer customer, List<dCustomer> customers) {
        this.actionCode = actionCode;
        this.customer = customer;
        this.customers = customers;
    }

}
