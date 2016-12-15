package ee.app.conversabusiness.interfaces;

import android.view.View;

import ee.app.conversabusiness.model.database.dbCustomer;

/**
 * Created by edgargomez on 7/4/16.
 */
public interface OnContactLongClickListener {
    void onContactLongClick(dbCustomer contact, View v, int position);
}