package ee.app.conversabusiness.extendables;

import android.content.Intent;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.R;
import ee.app.conversabusiness.contact.ContactUpdateReason;
import ee.app.conversabusiness.dialog.InAppNotification;
import ee.app.conversabusiness.events.TypingEvent;
import ee.app.conversabusiness.events.contact.ContactDeleteEvent;
import ee.app.conversabusiness.events.contact.ContactRetrieveEvent;
import ee.app.conversabusiness.events.contact.ContactSaveEvent;
import ee.app.conversabusiness.events.contact.ContactUpdateEvent;
import ee.app.conversabusiness.events.message.MessageDeleteEvent;
import ee.app.conversabusiness.events.message.MessageIncomingEvent;
import ee.app.conversabusiness.events.message.MessageOutgoingEvent;
import ee.app.conversabusiness.events.message.MessageRetrieveEvent;
import ee.app.conversabusiness.events.message.MessageUpdateEvent;
import ee.app.conversabusiness.interfaces.OnContactTaskCompleted;
import ee.app.conversabusiness.interfaces.OnMessageTaskCompleted;
import ee.app.conversabusiness.messaging.MessageUpdateReason;
import ee.app.conversabusiness.model.database.dbCustomer;
import ee.app.conversabusiness.model.database.dbMessage;
import ee.app.conversabusiness.utils.Logger;

public class ConversaActivity extends BaseActivity implements OnMessageTaskCompleted,
        OnContactTaskCompleted {

    protected boolean unregisterListener = true;
    protected RelativeLayout mRlPushNotification;

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.error("onNewIntent", "\nIntent: " + intent);
        super.onNewIntent(intent);
        if (intent != null) {
            openFromNotification(intent);
        }
    }

    @Override
    protected void initialization() {
        super.initialization();
        if (mRlPushNotification == null) {
            mRlPushNotification = (RelativeLayout) findViewById(R.id.rlPushNotification);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (unregisterListener) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTypingEvent(TypingEvent event) {
        onTypingMessage(event.getFrom(), event.isTyping());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageOutgoingEvent(MessageOutgoingEvent event) {
        MessageSent(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageIncomingEvent(MessageIncomingEvent event) {
        MessageReceived(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageUpdateEvent(MessageUpdateEvent event) {
        MessageUpdated(event.getMessage(), event.getReason());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageDeleteEvent(MessageDeleteEvent event) {
        MessageDeleted(event.getMessageList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageRetrieveEvent(MessageRetrieveEvent event) {
        MessagesGetAll(event.getMessageList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactSaveEvent(ContactSaveEvent event) {
        ContactAdded(event.getContact());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactUpdateEvent(ContactUpdateEvent event) {
        ContactUpdated(event.getContact(), event.getReason());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactDeleteEvent(ContactDeleteEvent event) {
        ContactDeleted(event.getContactList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactRetrieveEvent(ContactRetrieveEvent event) {
        ContactGetAll(event.getListResponse());
    }

    /* ********************************************************************** */
    /* ********************************************************************** */

    protected void openFromNotification(Intent intent) {
        /* Child activities override this method */
    }

    @Override
    public void onTypingMessage(String from, boolean isTyping) {
        /* Child activities override this method */
    }

    @Override
    public void MessagesGetAll(final List<dbMessage> response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageSent(final dbMessage response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageReceived(dbMessage response) {
        // Show in-app notification
        if (mRlPushNotification != null) {
            if (ConversaApp.getInstance(this).getPreferences().getInAppNotificationPreview())
                InAppNotification.make(this, mRlPushNotification)
                        .show(response);
        }
    }

    @Override
    public void MessageDeleted(final List<String> response) {
        /* Child activities override this method */
    }

    @Override
    public void MessageUpdated(final dbMessage response, MessageUpdateReason reason) {
        /* Child activities override this method */
    }

    @Override
    public void ContactGetAll(List<dbCustomer> response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactAdded(dbCustomer response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactDeleted(List<String> response) {
        /* Child activities override this method */
    }

    @Override
    public void ContactUpdated(dbCustomer response, ContactUpdateReason reason) {
        /* Child activities override this method */
    }

}