package ee.app.conversamanager.management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

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
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.ChannelStateListener;
import io.ably.lib.realtime.CompletionListener;
import io.ably.lib.realtime.ConnectionState;
import io.ably.lib.realtime.ConnectionStateListener;
import io.ably.lib.realtime.Presence;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import io.ably.lib.types.PresenceMessage;
import io.ably.lib.util.IntentUtils;

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
        try {
            ClientOptions clientOptions = new ClientOptions();
            clientOptions.key = "T6z9Ew.9a7FmQ:NYh49uPgi78dbMYH";
            clientOptions.logLevel = io.ably.lib.util.Log.ERROR;
            if (this.clientId != null) {
                clientOptions.clientId = clientId;
            }
            // Receive messages that they themselves publish
            clientOptions.echoMessages = false;
            // Ably Realtime library will open and maintain a connection to the Ably realtime servers
            // as soon as it is instanced
            ablyRealtime = new AblyRealtime(clientOptions);
            // Register listener for state changes
            ablyRealtime.connection.on(this);

            LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ErrorInfo error = IntentUtils.getErrorInfo(intent);
                    if (error != null) {
                        // Handle error
                        Logger.error("onReceive", "Push failed: " + error.message);
                        return;
                    }
                    // Subscribe to channels / listen for push etc.
                    String channelname = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
                    ablyRealtime.channels.get("bpbc:" + channelname).push.subscribeClientAsync(context, new CompletionListener() {
                        @Override
                        public void onSuccess() {
                            Logger.error("onSuccess", "Public channel subscribed for push");
                        }

                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            Logger.error("onError", "Public channel error for push: " + errorInfo.message);
                        }
                    });

                    ablyRealtime.channels.get("bpvt:" + channelname).push.subscribeClientAsync(context, new CompletionListener() {
                        @Override
                        public void onSuccess() {
                            Logger.error("onSuccess", "Private channel subscribed for push");
                        }

                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            Logger.error("onError", "Private channel error for push: " + errorInfo.message);
                        }
                    });
                }
            }, new IntentFilter("io.ably.broadcast.PUSH_ACTIVATE"));

            ablyRealtime.push.activate(context);
        } catch (AblyException e) {
            Logger.error(TAG, "InitAbly method exception: " + e.getMessage());
        }
    }

    public void subscribeToPushNotifications(Intent intent) {
        ErrorInfo error = IntentUtils.getErrorInfo(intent);
        if (error != null) {
            // Handle error
            Logger.error("onReceive", "Push failed: " + error.message);
            return;
        }
        // Subscribe to channels / listen for push etc.
        String channelname = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
        ablyRealtime.channels.get("bpbc:" + channelname).push.subscribeClientAsync(context, new CompletionListener() {
            @Override
            public void onSuccess() {
                Logger.error("onSuccess", "Public channel subscribed for push");
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Logger.error("onError", "Public channel error for push: " + errorInfo.message);
            }
        });

        ablyRealtime.channels.get("bpvt:" + channelname).push.subscribeClientAsync(context, new CompletionListener() {
            @Override
            public void onSuccess() {
                Logger.error("onSuccess", "Private channel subscribed for push");
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Logger.error("onError", "Private channel error for push: " + errorInfo.message);
            }
        });
    }

    public void connectAbly() {
        if (ablyRealtime != null) {
            switch (ablyConnectionStatus()) {
                case initialized:
                case connecting:
                case connected: {
                    Logger.error(TAG, "Ably is already connected or is connecting");
                }
                default: {
                    ablyRealtime.connection.connect();
                }
            }
        });
    }

    public void disconnectAbly() {
        if (ablyRealtime != null) {
            ablyRealtime.connection.close();
            ablyRealtime.push.deactivate(context);
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

    /**
     *
     * HELP METHODS
     *
     */
    public void userHasStartedTyping(String channelName) {
//        if(this.ablyRealtime.connection.state != ConnectionState.connected) {
//            return;
//        }
//
//        try {
//            JsonObject payload = new JsonObject();
//            payload.addProperty("isTyping", true);
//            payload.addProperty("from", ConversaApp.getInstance(context).getPreferences().getAccountBusinessId());
//
//            if (!ablyRealtime.channels.isEmpty()) {
//                Channel channel = ablyRealtime.channels.get("upbc:" + channelName);
//                // Not interested in callback
//                channel.presence.update(payload, null);
//            }
//        } catch (AblyException e) {
//            Logger.error(TAG, e.getMessage());
//        }
    }

    public void userHasEndedTyping(String channelName) {
//        if(this.ablyRealtime.connection.state != ConnectionState.connected) {
//            return;
//        }
//
//        try {
//            JsonObject payload = new JsonObject();
//            payload.addProperty("isTyping", false);
//            payload.addProperty("from", ConversaApp.getInstance(context).getPreferences().getAccountBusinessId());
//
//            if (!ablyRealtime.channels.isEmpty()) {
//                Channel channel = ablyRealtime.channels.get("upbc:" + channelName);
//                // Not interested in callback
//                channel.presence.update(payload, null);
//            }
//        } catch (AblyException e) {
//            Logger.error(TAG, e.getMessage());
//        }
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