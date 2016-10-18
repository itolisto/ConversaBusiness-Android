package ee.app.conversabusiness.contact;

import android.app.IntentService;
import android.content.Intent;
import android.database.SQLException;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.actions.ContactAction;
import ee.app.conversabusiness.events.contact.ContactDeleteEvent;
import ee.app.conversabusiness.events.contact.ContactRetrieveEvent;
import ee.app.conversabusiness.events.contact.ContactSaveEvent;
import ee.app.conversabusiness.model.database.dbCustomer;

/**
 * Created by edgargomez on 8/17/16.
 */
public class ContactIntentService extends IntentService {

    public static final String TAG = "ContactIntentService";

    // Intent constants
    public static final String INTENT_EXTRA_ACTION_CODE = "action_code";
    public static final String INTENT_EXTRA_CUSTOMER = "customer_single";
    public static final String INTENT_EXTRA_CUSTOMER_LIST = "customer_list";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ContactIntentService() {
        super("ContactIntentService");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            return;
        }

        ContactAction actionCode = (ContactAction) intent.getExtras().getSerializable(INTENT_EXTRA_ACTION_CODE);
        dbCustomer user = intent.getExtras().getParcelable(INTENT_EXTRA_CUSTOMER);
        List<String> list = intent.getExtras().getStringArrayList(INTENT_EXTRA_CUSTOMER_LIST);

        try {
            switch (actionCode) {
                case ACTION_CONTACT_SAVE:
                    ConversaApp.getInstance(this).getDB().saveContact(user);
                    if (user.getId() != -1) {
                        EventBus.getDefault().post(new ContactSaveEvent(user));
                    }
                    break;
                case ACTION_CONTACT_UPDATE:
                    break;
                case ACTION_CONTACT_DELETE: {
                    // Delete from database
                    ConversaApp.getInstance(this).getDB().deleteContactsById(list);
                    EventBus.getDefault().post(new ContactDeleteEvent(list));
                    break;
                }
                case ACTION_CONTACT_RETRIEVE_ALL:
                    List<dbCustomer> users = ConversaApp.getInstance(this).getDB().getAllContacts();
                    EventBus.getDefault().post(new ContactRetrieveEvent(users));
                    break;
            }
        } catch (SQLException e) {
            Log.e("ContactAsyncTaskRunner", "No se pudo guardar contacto porque ocurrio el siguiente error: " + e.getMessage());
        }
    }

}