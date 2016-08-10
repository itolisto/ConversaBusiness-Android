/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversabusiness.messageshandling;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.model.Database.dbMessage;
import ee.app.conversabusiness.model.Parse.Account;
import ee.app.conversabusiness.utils.Const;

/**
 * SendMessageAsync
 * 
 * AsyncTask for sending all sorts of messages
 */

public class SendMessageAsync {

	public static void sendTextMessage(String customerId, String text) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(ConversaApp.getPreferences().getBusinessId());
		message.setToUserId(customerId);
		message.setMessageType(Const.kMessageTypeText);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setBody(text);

		// 2. Save locally on background
		message.saveToLocalDatabase(dbMessage.ACTION_MESSAGE_SAVE);
	}

	public static void sendLocationMessage(String customerId, float lat, float lon) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(ConversaApp.getPreferences().getBusinessId());
		message.setToUserId(customerId);
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setLatitude(lat);
		message.setLongitude(lon);

		// 2. Save locally on background
		message.saveToLocalDatabase(dbMessage.ACTION_MESSAGE_SAVE);
	}

	public static void sendImageMessage(String businessId, int width, int height, int size) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(businessId);
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setWidth(width);
		message.setHeight(height);
		message.setBytes(size);
		// In here we must have a way to reference ParseFile associated with this message
		// We probably need to add a variable to Message class to hold a reference

		// 2. Save locally on background
		message.saveToLocalDatabase(dbMessage.ACTION_MESSAGE_SAVE);
	}

	public static void sendVideoAudioMessage(String businessId, int duration, int size) {
		// 1. Create local message
		dbMessage message = new dbMessage();
		message.setFromUserId(Account.getCurrentUser().getObjectId());
		message.setToUserId(businessId);
		message.setMessageType(Const.kMessageTypeLocation);
		message.setDeliveryStatus(dbMessage.statusUploading);
		message.setDuration(duration);
		message.setBytes(size);
		// In here we must have a way to reference ParseFile associated with this message
		// We probably need to add a variable to Message class to hold a reference

		// 2. Save locally on background
		message.saveToLocalDatabase(dbMessage.ACTION_MESSAGE_SAVE);
	}

}