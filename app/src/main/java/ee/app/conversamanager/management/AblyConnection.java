package ee.app.conversamanager.management;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.events.TypingEvent;
import ee.app.conversamanager.messaging.CustomMessageService;
import ee.app.conversamanager.utils.Logger;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.ChannelState;
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

/**
 * Created by edgargomez on 8/17/16.
 */
public class AblyConnection implements Channel.MessageListener, Presence.PresenceListener,
        CompletionListener, ConnectionStateListener, ChannelStateListener {

    private final String TAG = AblyConnection.class.getSimpleName();
    private static AblyConnection instance;
    private final Context context;
    private final String clientId;
    private boolean firstLoad;
    private AblyRealtime ablyRealtime;

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
        this.firstLoad = true;
        this.clientId = generateDeviceUUID();
    }

    public void initAbly()  {
        try {
            ClientOptions clientOptions = new ClientOptions();
            clientOptions.key = "zmxQkA.HfI9Xg:0UC2UioXcnDarSak";
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
        } catch (AblyException e) {
            Logger.error(TAG, "InitAbly method exception: " + e.getMessage());
        }
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
        }
    }

    public void disconnectAbly() {
        if (ablyRealtime != null) {
            ablyRealtime.connection.close();
        }
    }

    /**
     *
     * MESSAGE LISTENER METHOD
     *
     */
    @Override
    public void onMessage(Message messages) {
        JSONObject additionalData;

        try {
            additionalData = new JSONObject(messages.data.toString());
        } catch (JSONException e) {
            Logger.error(TAG, "onMessageReceived additionalData fail to parse-> " + e.getMessage());
            return;
        }

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                Intent msgIntent = new Intent(context, CustomMessageService.class);
                msgIntent.putExtra("data", additionalData.toString());
                context.startService(msgIntent);
                break;
        }
    }

    /**
     *
     * PRESENCE LISTENER METHOD
     *
     */
    @Override
    public void onPresenceMessage(PresenceMessage presenceMessage) {
        Logger.error("onPresenceMessage", "Member " + presenceMessage.clientId + " : " + presenceMessage.action.toString());

        switch (presenceMessage.action) {
            case enter:
                break;
            case leave:
                break;
            case update:
                String from = ((JsonObject) presenceMessage.data).get("from").getAsString();
                boolean isUserTyping = ((JsonObject) presenceMessage.data).get("isTyping").getAsBoolean();
                EventBus.getDefault().post(new TypingEvent(from, isUserTyping));
                break;
        }
    }

    /**
     *
     * COMPLETION LISTENER METHOD
     *
     */
    @Override
    public void onSuccess() {
        Logger.error("PresenceRegistration", "\nsuccess success success\nsuccess");
    }

    @Override
    public void onError(ErrorInfo reason) {
        Logger.error("PresenceRegistration", reason.message);
    }

    /**
     *
     * CONNECTION STATE LISTENER METHOD
     *
     */
    @Override
    public void onConnectionStateChanged(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.current) {
            case initialized:
                Logger.error("onConnectionStateChgd", "Initialized");
                break;
            case connecting:
                Logger.error("onConnectionStateChgd", "Connecting");
                break;
            case connected:
                Logger.error("onConnectionStateChgd", "Connected");
                if (firstLoad) {
                    // Subscribe to all Channels
                    subscribeToChannels();
                    // Change first load
                    firstLoad = false;
                } else {
                    if (ablyRealtime.channels.values().size() == 0) {
                        subscribeToChannels();
                    } else {
                        for (Channel channel : ablyRealtime.channels.values()) {
                            reattach(channel);
                        }
                    }
                }
                break;
            case disconnected:
                Logger.error("onConnectionStateChgd", "Disconnected");
                break;
            case suspended:
                Logger.error("onConnectionStateChgd", "Suspended");
                break;
            case closing:
                Logger.error("onConnectionStateChgd", "Closing");
                for (Channel channel : ablyRealtime.channels.values()) {
                    channel.unsubscribe();
                    channel.presence.unsubscribe();
                }
                break;
            case closed:
                Logger.error("onConnectionStateChgd", "Closed");
                break;
            case failed:
                Logger.error("onConnectionStateChgd", "Failed");
                break;
        }
    }

    public void subscribeToChannels() {
        String channelname = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
        if (!channelname.isEmpty()) {
            for (int i = 0; i < 2; i++) {
                Channel channel;
                if (i == 0) {
                    channel = ablyRealtime.channels.get("bpbc:" + channelname);
                } else {
                    channel = ablyRealtime.channels.get("bpvt:" + channelname);
                }

                reattach(channel);
            }
        }
    }

    private void reattach(Channel channel) {
        try {
            channel.subscribe(this);
            channel.presence.subscribe(this);
            channel.presence.enter(PresenceMessage.Action.present, this);
        } catch (AblyException e) {
            Logger.error("reattach", "Error while trying to subscribe to channel or presence");
        }
    }

    /**
     *
     * CHANNEL STATE LISTENER METHOD
     *
     */
    @Override
    public void onChannelStateChanged(ChannelState state, ErrorInfo reason) {
        if (reason != null) {
            Logger.error("fasdf", reason.message);
            return;
        }

        switch (state) {
            case initialized:
                break;
            case attaching:
                break;
            case attached:
                break;
            case detaching:
                break;
            case detached:
                break;
            case failed:
                break;
        }
    }

    /**
     *
     * HELP METHODS
     *
     */
    public void userHasStartedTyping(String channelName) {
        if(this.ablyRealtime.connection.state != ConnectionState.connected) {
            return;
        }

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("isTyping", true);
            payload.addProperty("from", ConversaApp.getInstance(context).getPreferences().getAccountBusinessId());

            if (!ablyRealtime.channels.isEmpty()) {
                Channel channel = ablyRealtime.channels.get("upbc:" + channelName);
                // Not interested in callback
                channel.presence.update(payload, null);
            }
        } catch (AblyException e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    public void userHasEndedTyping(String channelName) {
        if(this.ablyRealtime.connection.state != ConnectionState.connected) {
            return;
        }

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("isTyping", false);
            payload.addProperty("from", ConversaApp.getInstance(context).getPreferences().getAccountBusinessId());

            if (!ablyRealtime.channels.isEmpty()) {
                Channel channel = ablyRealtime.channels.get("upbc:" + channelName);
                // Not interested in callback
                channel.presence.update(payload, null);
            }
        } catch (AblyException e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    public PresenceMessage[] getPresentUsers(String channel) {
        return ablyRealtime.channels.get("upbc:".concat(channel)).presence.get();
    }

    public ConnectionState ablyConnectionStatus() {
        if (ablyRealtime == null) {
            return ConnectionState.disconnected;
        } else {
            return ablyRealtime.connection.state;
        }
    }

    public final String getPublicConnectionId() {
        if (ablyRealtime != null) {
            return ablyRealtime.connection.key;
        }

        return null;
    }

    private static String generateDeviceUUID() {
        String serial = android.os.Build.SERIAL;
        String androidID = Settings.Secure.ANDROID_ID;
        String deviceUUID = serial + androidID;

        MessageDigest digest;
        byte[] result;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            result = digest.digest(deviceUUID.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}