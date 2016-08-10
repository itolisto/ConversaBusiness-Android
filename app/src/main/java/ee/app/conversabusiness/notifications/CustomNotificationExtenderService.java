package ee.app.conversabusiness.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.FragmentUsersChat;
import ee.app.conversabusiness.extendables.ConversaActivity;
import ee.app.conversabusiness.model.Database.dCustomer;
import ee.app.conversabusiness.model.Database.dbMessage;
import ee.app.conversabusiness.model.Parse.Account;
import ee.app.conversabusiness.model.Parse.Customer;
import ee.app.conversabusiness.model.Parse.pMessage;
import ee.app.conversabusiness.utils.Const;
import ee.app.conversabusiness.utils.Foreground;
import ee.app.conversabusiness.utils.Logger;

/**
 * Created by edgargomez on 7/21/16.
 */
public class CustomNotificationExtenderService extends IntentService {

    private final String TAG = getClass().getSimpleName();
    public static final String PARAM_OUT_MSG = "omsg";

    public CustomNotificationExtenderService() {
        super("CustomNotificationExtenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            return;
        }

        String json = intent.getExtras().getString("data", "");

        if (json.isEmpty()) {
            return;
        }

        JSONObject additionalData;

        try {
            additionalData = new JSONObject(json);
        } catch (JSONException e) {
            Logger.error(TAG, "onMessageReceived payload fail to parse-> " + e.getMessage());
            return;
        }

        Log.e("NotifOpenedHandler", "Full additionalData:\n" + additionalData.toString());

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                String messageId = additionalData.optString("messageId", null);
                String contactId = additionalData.optString("contactId", null);
                String messageType = additionalData.optString("messageType", null);

                if (messageId == null || contactId == null || messageType == null) {
                    return;
                }

                dCustomer dbcustomer = null;

                // 1. Find if user is already a contact
                if(ConversaApp.getDB().isContact(contactId) == null) {
                    // 2. Call Parse for User information
                    ParseQuery<Customer> query = ParseQuery.getQuery(Customer.class);
                    query.whereEqualTo(Const.kCustomerUserInfoKey, ParseObject.createWithoutData(Account.class, contactId));
                    query.whereEqualTo(Const.kCustomerActiveKey, true);

                    Collection<String> collection = new ArrayList<>();
                    collection.add(Const.kCustomerNameKey);
                    collection.add(Const.kCustomerDisplayNameKey);
                    collection.add(Const.kCustomerStatusKey);
                    collection.add(Const.kCustomerAvatarKey);
                    query.selectKeys(collection);
                    Customer customer;

                    try {
                        customer = query.getFirst();
                    } catch (ParseException e) {
                        Log.e(TAG, "Error consiguiendo informacion de Customer " + e.getMessage());
                        return;
                    }

                    // 3. If Customer was found, save to Local Database
                    dbcustomer = new dCustomer();
                    dbcustomer.setBusinessId(contactId);
                    dbcustomer.setName(customer.getName());
                    dbcustomer.setDisplayName(customer.getDisplayName());
                    dbcustomer.setStatusMessage(customer.getStatus());
                    try {
                        if (customer.getAvatar() != null) {
                            dbcustomer.setAvatarThumbFileId(customer.getAvatar().getUrl());
                        } else {
                            dbcustomer.setAvatarThumbFileId("");
                        }
                    } catch (IllegalStateException e) {
                        dbcustomer.setAvatarThumbFileId("");
                    }

                    dbcustomer = ConversaApp.getDB().saveContact(dbcustomer);

                    if (dbcustomer.getId() == -1) {
                        Log.e(TAG, "Error guardando Contacto");
                        return;
                    }
                }

                // 2. Get message information
                pMessage parseMessage = null;

                if (additionalData.optBoolean("callParse", false)) {
                    ParseQuery<pMessage> query = ParseQuery.getQuery(pMessage.class);
                    Collection<String> collection = new ArrayList<>();

                    switch (messageType) {
                        case Const.kMessageTypeText:
                            collection.add(Const.kMessageTextKey);
                            break;
                        case Const.kMessageTypeAudio:
                        case Const.kMessageTypeVideo:
                        case Const.kMessageTypeImage:
                            collection.add(Const.kMessageFileKey);
                            break;
                        case Const.kMessageTypeLocation:
                            collection.add(Const.kMessageLocationKey);
                            break;
                    }

                    query.selectKeys(collection);

                    try {
                        parseMessage = query.get(messageId);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error consiguiendo informacion de Message " + e.getMessage());
                        return;
                    }
                }

                // 3. If message was found, save to Local Database
                dbMessage dbmessage = new dbMessage();
                dbmessage.setFromUserId(contactId);
                dbmessage.setToUserId(ConversaApp.getPreferences().getBusinessId());
                dbmessage.setMessageType(messageType);
                dbmessage.setDeliveryStatus(dbMessage.statusAllDelivered);
                dbmessage.setMessageId(messageId);

                switch (messageType) {
                    case Const.kMessageTypeText:
                        if (parseMessage == null) {
                            dbmessage.setBody(additionalData.optString("message", ""));
                        } else {
                            dbmessage.setBody(parseMessage.getText());
                        }
                        break;
                    case Const.kMessageTypeAudio:
                    case Const.kMessageTypeVideo:
                        dbmessage.setBytes(additionalData.optInt("size", 0));
                        dbmessage.setDuration(additionalData.optInt("duration", 0));
                        if (parseMessage == null) {
                            dbmessage.setFileId(additionalData.optString("file", ""));
                        } else {
                            try {
                                if (parseMessage.getFile() != null) {
                                    dbmessage.setFileId(parseMessage.getFile().getUrl());
                                } else {
                                    dbmessage.setFileId("");
                                }
                            } catch (IllegalStateException e) {
                                dbmessage.setFileId("");
                            }
                        }
                        break;
                    case Const.kMessageTypeImage:
                        dbmessage.setBytes(additionalData.optInt("size", 0));
                        dbmessage.setWidth(additionalData.optInt("width", 0));
                        dbmessage.setHeight(additionalData.optInt("height", 0));
                        if (parseMessage == null) {
                            dbmessage.setFileId(additionalData.optString("file", ""));
                        } else {
                            try {
                                if (parseMessage.getFile() != null) {
                                    dbmessage.setFileId(parseMessage.getFile().getUrl());
                                } else {
                                    dbmessage.setFileId("");
                                }
                            } catch (IllegalStateException e) {
                                dbmessage.setFileId("");
                            }
                        }
                        break;
                    case Const.kMessageTypeLocation:
                        dbmessage.setLatitude((float)additionalData.optDouble("latitude", 0));
                        dbmessage.setLongitude((float)additionalData.optDouble("longitude", 0));
                        break;
                }

                dbmessage = ConversaApp.getDB().saveMessage(dbmessage);

                if (dbmessage.getId() == -1) {
                    Log.e(TAG, "Error guardando Message");
                    return;
                }

                // 4. Broadcast results as from IntentService ain't possible to access ui thread
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ConversaActivity.MessageReceiver.ACTION_RESP);
                broadcastIntent.putExtra(PARAM_OUT_MSG, dbmessage);
                ConversaApp.getLocalBroadcastManager().sendBroadcast(broadcastIntent);

                if (dbcustomer != null) {
                    broadcastIntent = new Intent();
                    broadcastIntent.setAction(FragmentUsersChat.UsersReceiver.ACTION_RESP);
                    broadcastIntent.putExtra(PARAM_OUT_MSG, dbcustomer);
                    ConversaApp.getLocalBroadcastManager().sendBroadcast(broadcastIntent);
                }
                break;
            default:
                return;
        }

        if (Foreground.get().isBackground()) {
            // Show notification
            Log.e("OneSignalExample", "Notification displayed with id: " + 1);
        } else {
            // Show in-app notification
        }
    }

}