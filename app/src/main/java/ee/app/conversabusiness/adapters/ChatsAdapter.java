package ee.app.conversabusiness.adapters;

import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.R;
import ee.app.conversabusiness.model.database.dbCustomer;
import ee.app.conversabusiness.model.database.dbMessage;
import ee.app.conversabusiness.model.nChatItem;
import ee.app.conversabusiness.utils.Const;
import ee.app.conversabusiness.utils.Utils;
import ee.app.conversabusiness.view.MediumTextView;
import ee.app.conversabusiness.view.RegularTextView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private List<dbCustomer> mUsers;
    private OnItemClickListener listener;
    private OnLongClickListener longlistener;
    private SparseBooleanArray mSelectedPositions;
    private final WeakReference<AppCompatActivity> mActivity;

    public interface OnItemClickListener {
        void onItemClick(dbCustomer contact, int position);
    }

    public interface OnLongClickListener {
        void onItemLongClick(dbCustomer contact, int position);
    }

    public ChatsAdapter(AppCompatActivity activity, OnItemClickListener listener, OnLongClickListener longlistener) {
        this.mUsers = new ArrayList<>();
        this.mActivity = new WeakReference<>(activity);
        this.listener = listener;
        this.longlistener = longlistener;
        this.mSelectedPositions = new SparseBooleanArray(1);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(v, this.mActivity);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            if (payloads.size() > 0) {
                if (payloads.get(0) instanceof String) {
                    switch ((String)payloads.get(0)) {
                        case "toggleActivate": {
                            holder.toggleActivate();
                            break;
                        }
                        case "updateLastMessage": {
                            holder.updateLastMessage(mUsers.get(position));
                            break;
                        }
                        case "updateView": {
                            holder.updateView();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setContact(mUsers.get(position), position);
    }

    // get the number of currently selected items
    public int getSelectedItemCount() {
        return mSelectedPositions.size();
    }

    // Currently selected items.
    public ArrayList<String> getSelectedItems() {
        ArrayList<String> items = new ArrayList<>(mSelectedPositions.size());
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            items.add(Long.toString(mUsers.get(mSelectedPositions.keyAt(i)).getId()));
        }
        return items;
    }

    // Item changes its selection state
    public void toggleSelection(int position) {
        if (mSelectedPositions.get(position, false)) {
            mSelectedPositions.delete(position);
        } else {
            mSelectedPositions.put(position, true);
        }
        notifyItemChanged(position, "toggleActivate");
    }

    // Clear all selections
    public void clearSelections() {
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            notifyItemChanged(mSelectedPositions.keyAt(i), "toggleActivate");
        }
        mSelectedPositions.clear();
    }

    public void clearItems() {
        mUsers.clear();
    }

    public void setItems(List<dbCustomer> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public void addContact(dbCustomer user) {
        mUsers.add(0, user);
        notifyItemInserted(0);
    }

    public void updateContactPosition(String businessId) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getCustomerId().equals(businessId)) {
                if (i > 0) {
                    mUsers.add(0, mUsers.remove(i));
                    notifyItemMoved(i, 0);
                }
                notifyItemChanged(0, "updateLastMessage");
                break;
            }
        }
    }

    public void updateContactView(String businessId) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getCustomerId().equals(businessId)) {
                notifyItemChanged(i, "updateView");
                break;
            }
        }
    }

    public void updateContactRead(String businessId) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getCustomerId().equals(businessId)) {
                notifyItemChanged(i, "updateUnread");
                break;
            }
        }
    }

    public void removeContacts() {
        List<String> positionsById = getSelectedItems();
        int size = mSelectedPositions.size();
        int p = 0;

        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getId() == Long.parseLong(positionsById.get(p))) {
                mUsers.remove(i);
                notifyItemRemoved(i);
                size--;
                p++;
                i = 0;

                if (size == 0) {
                    break;
                }
            }
        }

        mSelectedPositions.clear();
    }

    private String setDate(AppCompatActivity activity, long timeOfCreation) {
        if (activity == null) {
            return "";
        }

        long now = System.currentTimeMillis();

        // Compute start of the day for the timestamp
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (timeOfCreation > cal.getTimeInMillis()) {
            return Utils.getTimeOrDay(activity, timeOfCreation, false);
        } else {
            long diff = now - timeOfCreation;
            long diffd = diff / (1000 * 60 * 60 * 24);

            if (diffd > 7) {
                return Utils.getDate(activity, timeOfCreation, true);
            } else if (diffd > 0 && diffd <= 7){
                return Utils.getTimeOrDay(activity, timeOfCreation, true);
            } else {
                return activity.getString(R.string.chat_day_yesterday);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private SimpleDraweeView ivUserImage;
        private MediumTextView tvUser;
        private RegularTextView tvDate;
        private RegularTextView tvLastMessage;
        private ImageView ivUnread;
        protected WeakReference<AppCompatActivity> activity;

        ViewHolder(View itemView, WeakReference<AppCompatActivity> activity) {
            super(itemView);

            this.activity = activity;

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

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void setContact(dbCustomer user, int position) {
            this.tvUser.setText(user.getDisplayName());

            Uri uri;

            if (position % 6 == 0) {
                uri = Uri.parse("android.resource://ee.app.conversabusiness/"
                        + R.drawable.ic_user_chat_1);
            } else if (position % 5 == 0) {
                uri = Uri.parse("android.resource://ee.app.conversabusiness/"
                        + R.drawable.ic_user_chat_2);
            } else if (position % 4 == 0) {
                uri = Uri.parse("android.resource://ee.app.conversabusiness/"
                        + R.drawable.ic_user_chat_3);
            } else if (position % 3 == 0) {
                uri = Uri.parse("android.resource://ee.app.conversabusiness/"
                        + R.drawable.ic_user_chat_4);
            } else if (position % 2 == 0) {
                uri = Uri.parse("android.resource://ee.app.conversabusiness/"
                        + R.drawable.ic_user_chat_5);
            } else {
                uri = Uri.parse("android.resource://ee.app.conversabusiness/"
                        + R.drawable.ic_user_chat_6);
            }

            this.ivUserImage.setImageURI(uri);
            updateLastMessage(user);

            if (mSelectedPositions.get(position, false)) {
                this.itemView.setActivated(true);
            } else {
                this.itemView.setActivated(false);
            }
        }

        void toggleActivate() {
            if (this.itemView.isActivated()) {
                this.itemView.setActivated(false);
            } else {
                this.itemView.setActivated(true);
            }
        }

        void updateView() {
            this.ivUnread.setVisibility(View.GONE);
        }

        void updateLastMessage(dbCustomer user) {
            if (this.activity.get() == null) {
                return;
            }

            nChatItem info = ConversaApp.getInstance(this.activity.get()).getDB()
                    .getLastMessageAndUnredCount(user.getCustomerId());
            dbMessage lastMessage = info.getMessage();

            if(lastMessage == null) {
                this.tvLastMessage.setText("");
                this.tvDate.setVisibility(View.GONE);
            } else {
                this.tvDate.setVisibility(View.VISIBLE);
                this.tvDate.setText(setDate(this.activity.get(), lastMessage.getCreated()));

                String from;

                if (lastMessage.getFromUserId().equals(
                        ConversaApp.getInstance(this.activity.get())
                                .getPreferences()
                                .getBusinessId()))
                {
                    from = this.activity.get().getString(R.string.me);
                } else {
                    from = user.getDisplayName();
                }

                switch(lastMessage.getMessageType()) {
                    case Const.kMessageTypeImage:
                        this.tvLastMessage.setText(this.activity.get()
                                .getString(R.string.contacts_last_message_image, from));
                        break;
                    case Const.kMessageTypeLocation:
                        this.tvLastMessage.setText(this.activity.get()
                                .getString(R.string.contacts_last_message_location, from));
                        break;
                    case Const.kMessageTypeText:
                        this.tvLastMessage.setText(this.activity.get()
                                .getString(R.string.contacts_last_message_text, from,
                                        lastMessage.getBody().replaceAll("\\n", " ")));
                        break;
                    default:
                        this.tvLastMessage.setText(this.activity.get()
                                .getString(R.string.contacts_last_message_default, from));
                        break;
                }
            }

            if (info.hasUnreadMessages()) {
                this.ivUnread.setVisibility(View.VISIBLE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.ivUnread.setBackground(this.activity.get().getResources()
                            .getDrawable(R.drawable.notification, null));
                } else {
                    this.ivUnread.setBackground(this.activity.get().getResources()
                            .getDrawable(R.drawable.notification));
                }
            } else {
                this.ivUnread.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                listener.onItemClick(mUsers.get(position), position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (longlistener != null) {
                int position = getAdapterPosition();
                longlistener.onItemLongClick(mUsers.get(position), position);
            }
            return true;
        }
    }

}