package ee.app.conversabusiness.interfaces;

import ee.app.conversabusiness.response.ContactResponse;

/**
 * Created by edgargomez on 7/4/16.
 */

public interface OnContactTaskCompleted {
    void ContactGetAll(ContactResponse response);
    void ContactAdded(ContactResponse response);
    void ContactDeleted(ContactResponse response);
    void ContactUpdated(ContactResponse response);
}