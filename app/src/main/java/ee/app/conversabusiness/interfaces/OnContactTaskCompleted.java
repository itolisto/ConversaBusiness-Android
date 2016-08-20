package ee.app.conversabusiness.interfaces;

import android.support.annotation.UiThread;

import java.util.List;

import ee.app.conversabusiness.model.Database.dCustomer;

/**
 * Created by edgargomez on 7/4/16.
 */

@UiThread
public interface OnContactTaskCompleted {
    void ContactGetAll(List<dCustomer> response);
    void ContactAdded(dCustomer response);
    void ContactDeleted(dCustomer response);
    void ContactUpdated(dCustomer response);
}