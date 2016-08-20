package ee.app.conversabusiness.notifications.onesignal;

import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OSNotificationReceivedResult;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.math.BigInteger;
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

/**
 * Created by edgargomez on 7/21/16.
 */
public class CustomNotificationExtenderService extends NotificationExtenderService {

    private final String TAG = CustomNotificationExtenderService.class.getSimpleName();
    public static final String PARAM_OUT_MSG = "omsg";

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult result) {
        if (result.restoring || result.isAppInFocus) {
            Log.e(TAG, "Returning as 'restoring' flag is true or app is in focus");
            return true;
        }

        OSNotificationPayload notification = result.payload;

        Log.e(TAG, "\nId:" + notification.notificationId +
                "\nTitle:" + notification.title +
                "\nBody:" + notification.body +
                "\nAdditionalData:" + notification.additionalData.toString() +
                "\nGroup:" + notification.groupKey +
                "\nGroupMessage:" + notification.groupMessage +
                "\nfromProjectNumber:" + notification.fromProjectNumber +
                "\nRestoring:" + result.restoring);

        JSONObject pushData = notification.additionalData;

        switch (pushData.optInt("appAction", 0)) {
            case 1:
                String messageId = pushData.optString("messageId", null);
                String contactId = pushData.optString("contactId", null);
                String messageType = pushData.optString("messageType", null);

                if (messageId == null || contactId == null || messageType == null) {
                    return true;
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
                         return true;
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
                         return true;
                     }
                }

                // 2. Get message information
                pMessage message = null;

                if (pushData.optBoolean("callParse", false)) {
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
                        message = query.get(messageId);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error consiguiendo informacion de Message " + e.getMessage());
                        return true;
                    }
                }

                // 3. If message was found, save to Local Database
                dbMessage dbmessage = new dbMessage();
                dbmessage.setFromUserId(contactId);
                dbmessage.setToUserId(Account.getCurrentUser().getObjectId());
                dbmessage.setMessageType(messageType);
                dbmessage.setDeliveryStatus(dbMessage.statusAllDelivered);
                dbmessage.setMessageId(messageId);

                switch (messageType) {
                    case Const.kMessageTypeText:
                        if (message == null) {
                            dbmessage.setBody(pushData.optString("message", ""));
                        } else {
                            dbmessage.setBody(message.getText());
                        }
                        break;
                    case Const.kMessageTypeAudio:
                    case Const.kMessageTypeVideo:
                        dbmessage.setBytes(pushData.optInt("size", 0));
                        dbmessage.setDuration(pushData.optInt("duration", 0));
                        if (message == null) {
                            dbmessage.setFileId(pushData.optString("file", ""));
                        } else {
                            try {
                                if (message.getFile() != null) {
                                    dbmessage.setFileId(message.getFile().getUrl());
                                } else {
                                    dbmessage.setFileId("");
                                }
                            } catch (IllegalStateException e) {
                                dbmessage.setFileId("");
                            }
                        }
                        break;
                    case Const.kMessageTypeImage:
                        dbmessage.setBytes(pushData.optInt("size", 0));
                        dbmessage.setWidth(pushData.optInt("width", 0));
                        dbmessage.setHeight(pushData.optInt("height", 0));
                        if (message == null) {
                            dbmessage.setFileId(pushData.optString("file", ""));
                        } else {
                            try {
                                if (message.getFile() != null) {
                                    dbmessage.setFileId(message.getFile().getUrl());
                                } else {
                                    dbmessage.setFileId("");
                                }
                            } catch (IllegalStateException e) {
                                dbmessage.setFileId("");
                            }
                        }
                        break;
                    case Const.kMessageTypeLocation:
                        dbmessage.setLatitude((float)pushData.optDouble("latitude", 0));
                        dbmessage.setLongitude((float)pushData.optDouble("longitude", 0));
                        break;
                }

                dbmessage = ConversaApp.getDB().saveMessage(dbmessage);

                if (dbmessage.getId() == -1) {
                    Log.e(TAG, "Error guardando Message");
                    return true;
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
                return true;
        }

        if (result.isAppInFocus) {
            // Return true to stop the notifications from displaying.
            return true;
        } else {
            OverrideSettings overrideSettings = new OverrideSettings();
            overrideSettings.extender = new NotificationCompat.Extender() {
                @Override
                public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                    // Sets the background notification color to Green on Android 5.0+ devices.
                    return builder.setColor(new BigInteger("FF00FF00", 16).intValue());
                }
            };

            OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
            Log.e("OneSignalExample", "Notification displayed with id: " + displayedResult.androidNotificationId);
            return false;
        }
    }

}