package ee.app.conversamanager.interfaces;

import android.support.annotation.UiThread;

import java.util.List;

import ee.app.conversamanager.contact.ContactUpdateReason;
import ee.app.conversamanager.model.database.dbCustomer;

/**
 * Created by edgargomez on 7/4/16.
 */
@UiThread
public interface OnContactTaskCompleted {
    void ContactGetAll(List<dbCustomer> response);
    void ContactAdded(dbCustomer response);
    void ContactDeleted(List<String> response);
    void ContactUpdated(dbCustomer response, ContactUpdateReason reason);
}