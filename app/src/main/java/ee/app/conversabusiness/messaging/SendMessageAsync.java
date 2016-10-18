package ee.app.conversabusiness.messaging;

import android.content.Context;
import android.content.Intent;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.actions.MessageAction;
import ee.app.conversabusiness.contact.SaveContactAsync;
import ee.app.conversabusiness.delivery.DeliveryStatus;
import ee.app.conversabusiness.model.database.dbCustomer;
import ee.app.conversabusiness.model.database.dbMessage;
import ee.app.conversabusiness.utils.Const;

/**
 * SendMessageAsync
 * 
 * AsyncTask for sending all sorts of messages
 */

public class SendMessageAsync {

	public static void sendTextMessage(Context context, String text, boolean addContact, dbCustomer business) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(ConversaApp.getInstance(context).getPreferences().getBusinessId());
		message.setToUserId(business.getCustomerId());
		message.setMessageType(Const.kMessageTypeText);
		message.setDeliveryStatus(DeliveryStatus.statusUploading);
		message.setBody(text);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_OUTGOING);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveContactAsync.saveCustomerAsContact(context, business);
		}
	}

	public static void sendLocationMessage(Context context, double lat, double lon, boolean addContact, dbCustomer business) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(ConversaApp.getInstance(context).getPreferences().getBusinessId());
		message.setToUserId(business.getCustomerId());
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(DeliveryStatus.statusUploading);
		message.setLatitude((float)lat);
		message.setLongitude((float)lon);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_OUTGOING);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveContactAsync.saveCustomerAsContact(context, business);
		}
	}

	public static void sendImageMessage(Context context, String path, int width, int height, int size, boolean addContact, dbCustomer business) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(ConversaApp.getInstance(context).getPreferences().getBusinessId());
		message.setToUserId(business.getCustomerId());
		message.setMessageType(Const.kMessageTypeImage);
		message.setDeliveryStatus(DeliveryStatus.statusUploading);
		message.setLocalUrl(path);
		message.setWidth(width);
		message.setHeight(height);
		message.setBytes(size);

		// 2. Save locally on background
		Intent intent = new Intent(context, MessageIntentService.class);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_OUTGOING);
		intent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, message);
		context.startService(intent);

		if (addContact) {
			SaveContactAsync.saveCustomerAsContact(context, business);
		}
	}

}