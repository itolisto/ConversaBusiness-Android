package ee.app.conversamanager.management;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.push.PNPushAddChannelResult;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.events.TypingEvent;
import ee.app.conversamanager.messaging.CustomMessageService;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;

/**
 * Created by edgargomez on 8/17/16.
 */
public class AblyConnection extends SubscribeCallback {

    private final String TAG = AblyConnection.class.getSimpleName();
    private static AblyConnection instance;
    private final Context context;
    private PubNub ablyRealtime;

    public static void initAblyManager(@NonNull Context context) {
        instance = new AblyConnection(context);
    }

    @Nullable
    public static AblyConnection getInstance() {
        if (instance == null) {
            return null;
        }

        return instance;
    }

    private AblyConnection(Context context) {
        this.context = context;
    }

    public PubNub getAblyRealtime() {
        return ablyRealtime;
    }

    public void initAbly()  {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-af90faac-3851-11e7-887b-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-6200baf9-6b96-4196-854d-110c764a8e63");
        PubNub pubnub = new PubNub(pnConfiguration);
        pubnub.addListener(this);
        this.ablyRealtime = pubnub;
    }

    public void subscribeToChannels() {
        this.ablyRealtime.subscribe().channels(getChannels()).withPresence().execute();
    }

    public void subscribeToPushChannels() {
        if (ablyRealtime == null || ConversaApp.getInstance(context).getPreferences().getPushKey().isEmpty())
            return;

        this.ablyRealtime.addPushNotificationsOnChannels()
                .pushType(PNPushType.GCM)
                .channels(getChannels())
                .deviceId(ConversaApp.getInstance(context).getPreferences().getPushKey())
                .async(new PNCallback<PNPushAddChannelResult>() {
            @Override
            public void onResponse(PNPushAddChannelResult result, PNStatus status) {
                Log.e("onResponse", "Result: " + result + ". Status: " + status);
            }
        });
    }

    public void disconnectAbly() {
        if (ablyRealtime != null) {
            this.ablyRealtime.unsubscribeAll();
            this.ablyRealtime.removeAllPushNotificationsFromDeviceWithPushToken()
                    .deviceId(ConversaApp.getInstance(context).getPreferences().getPushKey())
                    .pushType(PNPushType.GCM);
        }
    }

    private List<String> getChannels() {
        String channelname = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
        List<String> channels = new ArrayList<>(2);
        channels.add("bpbc_" + channelname);
        channels.add("bpvt_" + channelname);
        return channels;
    }

    /**
     *
     * HELP METHODS
     *
     */
    public void userHasStartedTyping(String channelName) {
        final HashMap<String, Object> params = new HashMap<>(3);
        params.put("userId", ConversaApp.getInstance(context).getPreferences().getAccountBusinessId());
        params.put("channelName", channelName);
        params.put("isTyping", true);

        ParseCloud.callFunctionInBackground("sendPresenceMessage", params, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer object, ParseException e) {
                if (e != null) {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(context, true);
                    }
                }
            }
        });
    }

    public void userHasEndedTyping(String channelName) {
        final HashMap<String, Object> params = new HashMap<>(2);
        params.put("userId", ConversaApp.getInstance(context).getPreferences().getAccountBusinessId());
        params.put("channelName", channelName);

        ParseCloud.callFunctionInBackground("sendPresenceMessage", params, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer object, ParseException e) {
                if (e != null) {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(context, true);
                    }
                }
            }
        });
    }

    public final String getPublicConnectionId() {
        if (ablyRealtime != null) {
            return ablyRealtime.getInstanceId();
        }

        return null;
    }

    @Override
    public void status(PubNub pubnub, PNStatus status) {
        if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
            // This event happens when radio / connectivity is lost
        } else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
            // Connect event. You can do stuff like publish, and know you'll get it.
            // Or just use the connected event to confirm you are subscribed for
            // UI / internal notifications, etc
        } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
            // Happens as part of our regular operation. This event happens when
            // radio / connectivity is lost, then regained.
        } else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
            // Handle messsage decryption error. Probably client configured to
            // encrypt messages and on live data feed it received plain text.
        }
    }

    @Override
    public void message(PubNub pubnub, PNMessageResult message) {
        JSONObject additionalData;

        try {
            JsonObject mMessage = message.getMessage().getAsJsonObject();
            JsonObject mmMessage;

            if (mMessage.get("message").isJsonObject()) {
                mmMessage = mMessage.getAsJsonObject("message");
                additionalData = new JSONObject(mmMessage.getAsString());
            } else {
                JsonElement jeMessage = mMessage.get("message");
                additionalData = new JSONObject(jeMessage.getAsString());
            }
        } catch (Exception e) {
            Logger.error(TAG, "onMessageReceived additionalData fail to parse-> " + e.getMessage());
            return;
        }

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                Intent msgIntent = new Intent(context, CustomMessageService.class);
                msgIntent.putExtra("data", additionalData.toString());
                context.startService(msgIntent);
                break;
            case 2:
                Logger.error("onPresenceMessage", additionalData.toString());

                String jeFrom = additionalData.optString("from", "");
                boolean isUserTyping = additionalData.optBoolean("isTyping", false);
                if (!jeFrom.isEmpty())
                    EventBus.getDefault().post(new TypingEvent(jeFrom, isUserTyping));
                break;
        }
    }

    @Override
    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
//        Logger.error("onPresenceMessage", "Member " + presenceMessage.clientId + " : " + presenceMessage.action.toString());
//
//        if (presenceMessage.data != null) {
//            JsonElement jeFrom = ((JsonObject) presenceMessage.data).get("from");
//            if (jeFrom != null) {
//                boolean isUserTyping = ((JsonObject) presenceMessage.data).get("isTyping").getAsBoolean();
//                EventBus.getDefault().post(new TypingEvent(jeFrom.getAsString(), isUserTyping));
//            }
//        }
    }

}