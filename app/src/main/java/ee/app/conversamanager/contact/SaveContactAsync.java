package ee.app.conversamanager.contact;

import android.content.Context;
import android.content.Intent;

import ee.app.conversamanager.actions.ContactAction;
import ee.app.conversamanager.model.database.dbCustomer;

/**
 * Created by edgargomez on 7/4/16.
 */
public class SaveContactAsync {

    public static void saveCustomerAsContact(Context context, dbCustomer business) {
        // 1. Save locally on background
        Intent intent = new Intent(context, ContactIntentService.class);
        intent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactAction.ACTION_CONTACT_SAVE);
        intent.putExtra(ContactIntentService.INTENT_EXTRA_CUSTOMER, business);
        context.startService(intent);
    }

}