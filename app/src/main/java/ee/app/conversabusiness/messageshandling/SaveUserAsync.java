package ee.app.conversabusiness.messageshandling;

import ee.app.conversabusiness.model.Database.dCustomer;

/**
 * Created by edgargomez on 7/4/16.
 */
public class SaveUserAsync {

    public static void saveUserAsContact(dCustomer business) {
        // 1. Save locally on background
        business.saveToLocalDatabase();
    }

}
