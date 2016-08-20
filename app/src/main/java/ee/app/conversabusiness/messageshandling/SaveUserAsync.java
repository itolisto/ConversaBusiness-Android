package ee.app.conversabusiness.messageshandling;

import android.content.Context;
import android.content.Intent;

import ee.app.conversabusiness.management.contact.ContactIntentService;
import ee.app.conversabusiness.model.Database.dCustomer;

/**
 * Created by edgargomez on 7/4/16.
 */
public class SaveUserAsync {

    public static void saveUserAsContact(Context context, dCustomer business) {
        // 1. Save locally on background
        Intent intent = new Intent(context, ContactIntentService.class);
        intent.putExtra(ContactIntentService.INTENT_EXTRA_ACTION_CODE, ContactIntentService.ACTION_MESSAGE_SAVE);
        intent.putExtra(ContactIntentService.INTENT_EXTRA_CUSTOMER, business);
        context.startService(intent);
    }

}
