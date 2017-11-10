package ee.app.conversamanager.messaging;

import android.app.IntentService;
import android.content.Intent;
import android.database.SQLException;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.actions.MessageAction;
import ee.app.conversamanager.delivery.DeliveryStatus;
import ee.app.conversamanager.events.message.MessageDeleteEvent;
import ee.app.conversamanager.events.message.MessageOutgoingEvent;
import ee.app.conversamanager.events.message.MessageRetrieveEvent;
import ee.app.conversamanager.events.message.MessageUpdateEvent;
import ee.app.conversamanager.jobs.SendMessageJob;
import ee.app.conversamanager.model.database.dbMessage;
import ee.app.conversamanager.utils.Logger;

/**
 * Created by edgargomez on 8/17/16.
 */
public class MessageIntentService extends IntentService {

    public static final String TAG = "MessageIntentService";

    // Intent constants
    public static final String INTENT_EXTRA_ACTION_CODE = "action_code";
    public static final String INTENT_EXTRA_MESSAGE = "message_single";

    // Specific Intent constants
    public static final String INTENT_EXTRA_CONTACT_ID = "contact_id";
    public static final String INTENT_EXTRA_UPDATE_STATUS = "update_status";
    public static final String INTENT_EXTRA_MESSAGE_COUNT = "message_count";
    public static final String INTENT_EXTRA_MESSAGE_SKIP = "message_skip";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public MessageIntentService() {
        super("MessageIntentService");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            return;
        }

        MessageAction actionCode = (MessageAction) intent.getExtras().getSerializable(INTENT_EXTRA_ACTION_CODE);
        dbMessage message = intent.getExtras().getParcelable(INTENT_EXTRA_MESSAGE);

        try {
            switch (actionCode) {
                case ACTION_MESSAGE_OUTGOING: {
                    ConversaApp.getInstance(this).getDB().saveMessage(message);

                    if (message.getId() != -1) {
                        ConversaApp.getInstance(getApplicationContext())
                                .getJobManager()
                                .addJob(new SendMessageJob(message.getId(), message.getToUserId()));

                        EventBus.getDefault().post(new MessageOutgoingEvent(message));
                    }
                    break;
                }
                case ACTION_MESSAGE_INCOMING: {
                    // This case is handled in ReceiveMessageJob
                    break;
                }
                case ACTION_MESSAGE_UPDATE_STATUS: {
                    int status = intent.getExtras().getInt(INTENT_EXTRA_UPDATE_STATUS, DeliveryStatus.statusParseError);
                    ConversaApp.getInstance(this).getDB().updateDeliveryStatus(message.getId(), status);
                    message.setDeliveryStatus(status);
                    EventBus.getDefault().post(
                            new MessageUpdateEvent(message, MessageUpdateReason.STATUS));
                    break;
                }
                case ACTION_MESSAGE_UPDATE_VIEW: {
                    String contact_id = intent.getExtras().getString(INTENT_EXTRA_CONTACT_ID);
                    ConversaApp.getInstance(this).getDB().updateViewMessages(contact_id);
                    message = new dbMessage();
                    message.setFromUserId(contact_id);
                    EventBus.getDefault().post(
                            new MessageUpdateEvent(message, MessageUpdateReason.VIEW));
                    return;
                }
                case ACTION_MESSAGE_RETRIEVE_ALL: {
                    String contact_id = intent.getExtras().getString(INTENT_EXTRA_CONTACT_ID);
                    int count = intent.getExtras().getInt(INTENT_EXTRA_MESSAGE_COUNT, 0);
                    int skip = intent.getExtras().getInt(INTENT_EXTRA_MESSAGE_SKIP, 0);
                    List<dbMessage> list = ConversaApp.getInstance(this)
                            .getDB().getMessagesByContact(contact_id, count, skip);
                    EventBus.getDefault().post(
                            new MessageRetrieveEvent(list));
                    break;
                }
                case ACTION_MESSAGE_DELETE_ALL: {
                    // Delete from database
                    String id = intent.getExtras().getString(INTENT_EXTRA_CONTACT_ID);
                    ConversaApp.getInstance(this).getDB().deleteAllMessagesById(id);
                    List<String> list = new ArrayList<>(1);
                    list.add(id);
                    EventBus.getDefault().post(new MessageDeleteEvent(list, MessageDeleteReason.ALL));
                    break;
                }
            }
        } catch (SQLException e) {
            Logger.error(TAG, "No se pudo guardar mensaje porque ocurrio el siguiente error: " + e.getMessage());
        }
    }

}