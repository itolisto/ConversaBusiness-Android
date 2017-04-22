package ee.app.conversamanager.holders;

import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Calendar;
import java.util.Locale;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.interfaces.OnContactClickListener;
import ee.app.conversamanager.interfaces.OnContactLongClickListener;
import ee.app.conversamanager.model.database.dbCustomer;
import ee.app.conversamanager.model.database.dbMessage;
import ee.app.conversamanager.model.nChatItem;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.MediumTextView;
import ee.app.conversamanager.view.RegularTextView;

/**
 * Created by edgargomez on 10/31/16.
 */

public class ChatsViewHolder extends BaseHolder {

    private SparseBooleanArray mSelectedPositions;
    private SimpleDraweeView ivUserImage;
    private MediumTextView tvUser;
    private RegularTextView tvDate;
    private RegularTextView tvLastMessage;
    private ImageView ivUnread;
    private dbCustomer user;

    private OnContactClickListener listener;
    private OnContactLongClickListener longlistener;

    public ChatsViewHolder(View itemView, AppCompatActivity activity, OnContactClickListener listener,
                           OnContactLongClickListener longlistener) {
        super(itemView, activity);

        this.ivUserImage = (SimpleDraweeView) itemView
                .findViewById(R.id.sdvContactAvatar);
        this.tvUser = (MediumTextView) itemView
                .findViewById(R.id.mtvUser);
        this.tvDate = (RegularTextView) itemView
                .findViewById(R.id.rtvDate);
        this.tvLastMessage = (RegularTextView) itemView
                .findViewById(R.id.rtvLastMessage);
        this.ivUnread = (ImageView) itemView
                .findViewById(R.id.ivUnread);

        this.listener = listener;
        this.longlistener = longlistener;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onContactClick(user, v, getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longlistener != null) {
            longlistener.onContactLongClick(user, v, getAdapterPosition());
        }
        return true;
    }

    public void setContact(dbCustomer user, int position, SparseBooleanArray mSelectedPositions) {
        this.user = user;
        this.mSelectedPositions = mSelectedPositions;

        this.tvUser.setText(user.getDisplayName());

        Uri uri;
        position++;

        if (position % 7 == 0) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_user_one))
                    .build();
        } else if (position % 6 == 0) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_user_two))
                    .build();
        } else if (position % 5 == 0) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_user_three))
                    .build();
        } else if (position % 4 == 0) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_user_four))
                    .build();
        } else if (position % 3 == 0) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_user_five))
                    .build();
        } else if (position % 2 == 0) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_user_six))
                    .build();
        } else {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.ic_user_seven))
                    .build();
        }

        this.ivUserImage.setImageURI(uri);

        updateLastMessage(user.getCustomerId());

        if (mSelectedPositions.get(position, false)) {
            this.itemView.setActivated(true);
        } else {
            this.itemView.setActivated(false);
        }
    }

    public void toggleActivate() {
        if (this.itemView.isActivated()) {
            this.itemView.setActivated(false);
        } else {
            this.itemView.setActivated(true);
        }
    }

    public void updateView() {
        this.ivUnread.setVisibility(View.GONE);
    }

    public void updateLastMessage(String customerId) {
        nChatItem info = ConversaApp.getInstance(activity).getDB()
                .getLastMessageAndUnredCount(customerId);
        dbMessage lastMessage = info.getMessage();

        if (lastMessage == null) {
            this.tvLastMessage.setText(activity
                    .getString(R.string.contacts_item_conversation_empty));
            this.tvDate.setVisibility(View.GONE);
        } else {
            this.tvDate.setVisibility(View.VISIBLE);
            this.tvDate.setText(setDate(activity, lastMessage.getCreated()));

            switch(lastMessage.getMessageType()) {
                case Const.kMessageTypeImage:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_image));
                    break;
                case Const.kMessageTypeLocation:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_location));
                    break;
                case Const.kMessageTypeAudio:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_audio));
                    break;
                case Const.kMessageTypeVideo:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_video));
                    break;
                case Const.kMessageTypeText:
                    this.tvLastMessage.setText(lastMessage.getBody().replaceAll("\\n", " "));
                    break;
                default:
                    this.tvLastMessage.setText(activity
                            .getString(R.string.contacts_last_message_default));
                    break;
            }
        }

        if (info.hasUnreadMessages()) {
            this.ivUnread.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.ivUnread.setBackground(activity.getResources()
                        .getDrawable(R.drawable.ic_unread_message, null));
            } else {
                this.ivUnread.setBackground(activity.getResources()
                        .getDrawable(R.drawable.ic_unread_message));
            }
        } else {
            this.ivUnread.setVisibility(View.INVISIBLE);
        }
    }

    private String setDate(AppCompatActivity activity, long timeOfCreation) {
        // Compute start of the day for the timestamp
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);

        long now = cal.getTimeInMillis();

        if (timeOfCreation > now) {
            return Utils.getTimeOrDay(activity, timeOfCreation, false);
        } else {
            long diff = now - timeOfCreation;
            long diffd = diff / (1000 * 60 * 60 * 24);
            long diffw = diff / (1000 * 60 * 60 * 24 * 7);

            if (diffd > 7) {
                return Utils.getDate(activity, timeOfCreation, (diffw > 52));
            } else if (diffd >= 1 && diffd <= 7) {
                return Utils.getTimeOrDay(activity, timeOfCreation, true);
            } else if (diffd == 0) {
                return activity.getString(R.string.chat_day_yesterday);
            } else {
                return Utils.getDate(activity, timeOfCreation, true);
            }
        }
    }

}