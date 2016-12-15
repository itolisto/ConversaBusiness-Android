package ee.app.conversabusiness.jobs;

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

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.delivery.DeliveryStatus;
import ee.app.conversabusiness.dialog.PushNotification;
import ee.app.conversabusiness.events.contact.ContactSaveEvent;
import ee.app.conversabusiness.events.message.MessageIncomingEvent;
import ee.app.conversabusiness.model.database.NotificationInformation;
import ee.app.conversabusiness.model.database.dbCustomer;
import ee.app.conversabusiness.model.database.dbMessage;
import ee.app.conversabusiness.model.parse.Customer;
import ee.app.conversabusiness.utils.AppActions;
import ee.app.conversabusiness.utils.Const;
import ee.app.conversabusiness.utils.Foreground;
import ee.app.conversabusiness.utils.Logger;

import static com.parse.ParseException.CONNECTION_FAILED;
import static com.parse.ParseException.INTERNAL_SERVER_ERROR;
import static com.parse.ParseException.INVALID_SESSION_TOKEN;

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
            collection.add(Const.kCustomerNameKey);
            collection.add(Const.kCustomerDisplayNameKey);
            query.selectKeys(collection);

            Customer customer = query.get(contactId);

            // 3. If Customer was found, save to Local Database
            dbcustomer = new dbCustomer();
            dbcustomer.setCustomerId(contactId);
            dbcustomer.setName(customer.getName());
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
        dbmessage.setDeliveryStatus(DeliveryStatus.statusAllDelivered);
        dbmessage.setMessageId(messageId);

        switch (messageType) {
            case Const.kMessageTypeText:
                dbmessage.setBody(additionalData.optString("message", ""));
                break;
            case Const.kMessageTypeAudio:
            case Const.kMessageTypeVideo:
                dbmessage.setBytes(additionalData.optInt("size", 0));
                dbmessage.setDuration(additionalData.optInt("duration", 0));
                dbmessage.setRemoteUrl(additionalData.optString("file", ""));
                break;
            case Const.kMessageTypeImage:
                dbmessage.setBytes(additionalData.optInt("size", 0));
                dbmessage.setWidth(additionalData.optInt("width", 0));
                dbmessage.setHeight(additionalData.optInt("height", 0));
                dbmessage.setRemoteUrl(additionalData.optString("file", ""));
                break;
            case Const.kMessageTypeLocation:
                dbmessage.setLatitude((float) additionalData.optDouble("latitude", 0));
                dbmessage.setLongitude((float) additionalData.optDouble("longitude", 0));
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
        if (throwable instanceof ParseException) {
            ParseException exception = (ParseException) throwable;
            Logger.error(TAG, exception.getMessage());
            AppActions.validateParseException(getApplicationContext(), exception);

            if (exception.getCode() == INTERNAL_SERVER_ERROR ||
                    exception.getCode() == CONNECTION_FAILED ||
                    exception.getCode() == INVALID_SESSION_TOKEN )
            {
                return RetryConstraint.CANCEL;
            }
        }

        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specify a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        RetryConstraint rtn = RetryConstraint.createExponentialBackoff(runCount, 1000);
        rtn.setNewPriority(Priority.MID);
        return rtn;
    }

}