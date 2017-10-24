package ee.app.conversamanager.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.delivery.DeliveryStatus;
import ee.app.conversamanager.dialog.PushNotification;
import ee.app.conversamanager.events.contact.ContactSaveEvent;
import ee.app.conversamanager.events.message.MessageIncomingEvent;
import ee.app.conversamanager.management.PubnubConnection;
import ee.app.conversamanager.model.database.NotificationInformation;
import ee.app.conversamanager.model.database.dbCustomer;
import ee.app.conversamanager.model.database.dbMessage;
import ee.app.conversamanager.model.parse.Customer;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.Foreground;
import ee.app.conversamanager.utils.Logger;

/**
 * Created by edgargomez on 9/6/16.
 */
public class ReceiveMessageJob extends Job {

    private final String TAG = ReceiveMessageJob.class.getSimpleName();
    private final String additionalDataString;

    public ReceiveMessageJob(String additionalDataString, String group) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(group).addTags(group));
        // We have to set variables so they get serialized into job
        this.additionalDataString = additionalDataString;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        JSONObject additionalData = new JSONObject(additionalDataString);

        // Check if this message came from this connectionId
        String connectionId = additionalData.optString("connectionId", "");
        String currentConnectionId = PubnubConnection.getInstance().getPublicConnectionId();

        if (currentConnectionId != null && connectionId.equals(currentConnectionId)) {
            return;
        }

        String messageId = additionalData.optString("messageId", null);
        String contactId = additionalData.optString("contactId", null);
        String messageType = additionalData.optString("messageType", null);

        if (messageId == null || contactId == null || messageType == null) {
            return;
        }

        dbCustomer dbcustomer = ConversaApp.getInstance(getApplicationContext())
                .getDB().isContact(contactId);

        boolean newContact = false;

        // 1. Find if user is already a contact
        if (dbcustomer == null) {
            newContact = true;
            // 2. Call Parse for User information
            ParseQuery<Customer> query = ParseQuery.getQuery(Customer.class);
            query.whereEqualTo(Const.kCustomerActiveKey, true);

            Collection<String> collection = new ArrayList<>();
            collection.add(Const.kCustomerDisplayNameKey);
            query.selectKeys(collection);

            Customer customer;

            try {
                customer = query.get(contactId);
            } catch (ParseException e) {
                if (AppActions.validateParseException(e)) {
                    AppActions.appLogout(getApplicationContext(), true);
                }

                return;
            }

            // 3. If Customer was found, save to Local Database
            dbcustomer = new dbCustomer();
            dbcustomer.setCustomerId(contactId);
            dbcustomer.setDisplayName(customer.getDisplayName());

            ConversaApp.getInstance(getApplicationContext()).getDB().saveContact(dbcustomer);

            if (dbcustomer.getId() == -1) {
                Logger.error(TAG, "Error guardando Business");
                return;
            }
        }

        // 2. Save to Local Database
        dbMessage dbmessage = new dbMessage();
        dbmessage.setFromUserId(contactId);
        dbmessage.setToUserId(ConversaApp.getInstance(getApplicationContext()).getPreferences().getAccountBusinessId());
        dbmessage.setMessageType(messageType);
        dbmessage.setDeliveryStatus(DeliveryStatus.statusReceived);
        dbmessage.setMessageId(messageId);

        switch (messageType) {
            case Const.kMessageTypeText:
                dbmessage.setBody(additionalData.optString("message", ""));
                break;
            case Const.kMessageTypeLocation:
                dbmessage.setLatitude((float) additionalData.optDouble("latitude", 0));
                dbmessage.setLongitude((float) additionalData.optDouble("longitude", 0));
                break;
            case Const.kMessageTypeAudio:
            case Const.kMessageTypeVideo:
                dbmessage.setDeliveryStatus(DeliveryStatus.statusDownloading);
                dbmessage.setBytes(additionalData.optInt("size", 0));
                dbmessage.setDuration(additionalData.optInt("duration", 0));
                dbmessage.setRemoteUrl(additionalData.optString("file", ""));
                break;
            case Const.kMessageTypeImage:
                dbmessage.setDeliveryStatus(DeliveryStatus.statusDownloading);
                dbmessage.setBytes(additionalData.optInt("size", 0));
                dbmessage.setWidth(additionalData.optInt("width", 0));
                dbmessage.setHeight(additionalData.optInt("height", 0));
                dbmessage.setRemoteUrl(additionalData.optString("file", ""));
                break;
        }

        ConversaApp.getInstance(getApplicationContext()).getDB().saveMessage(dbmessage);

        if (dbmessage.getId() == -1) {
            Logger.error(TAG, "Error guardando Message");
            return;
        }

        if (Foreground.get().isBackground()) {
            if (ConversaApp.getInstance(getApplicationContext())
                    .getPreferences().getPushNotificationPreview()) {

                // Autoincrement count
                NotificationInformation summary = ConversaApp.getInstance(getApplicationContext())
                        .getDB().getGroupInformation(contactId);

                if (summary.getNotificationId() == -1) {
                    ConversaApp.getInstance(getApplicationContext()).getDB()
                            .incrementGroupCount(summary, true);
                } else {
                    ConversaApp.getInstance(getApplicationContext()).getDB()
                            .incrementGroupCount(summary, false);
                }

                PushNotification.showMessageNotification(
                        getApplicationContext(),
                        dbcustomer.getDisplayName(),
                        additionalData.toString(),
                        dbmessage,
                        summary
                );
            }
        }

        // 4. Broadcast results as from IntentService ain't possible to access ui thread
        if (newContact) {
            EventBus.getDefault().post(new ContactSaveEvent(dbcustomer));
        }

        EventBus.getDefault().post(new MessageIncomingEvent(dbmessage));

        if (dbmessage.getMessageType().equals(Const.kMessageTypeAudio) ||
                dbmessage.getMessageType().equals(Const.kMessageTypeVideo) ||
                dbmessage.getMessageType().equals(Const.kMessageTypeImage))
        {
            ConversaApp.getInstance(getApplicationContext())
                    .getJobManager()
                    .addJob(new DownloadFileJob(dbmessage.getFromUserId(), dbmessage.getId()));
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}