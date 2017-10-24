package ee.app.conversamanager;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ee.app.conversamanager.adapters.MessagesAdapter;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.interfaces.OnMessageClickListener;
import ee.app.conversamanager.management.PubnubConnection;
import ee.app.conversamanager.messaging.MessageDeleteReason;
import ee.app.conversamanager.messaging.MessageUpdateReason;
import ee.app.conversamanager.messaging.SendMessageAsync;
import ee.app.conversamanager.model.database.dbCustomer;
import ee.app.conversamanager.model.database.dbMessage;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.ImageFilePath;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.view.MediumTextView;
import ee.app.conversamanager.view.MyBottomSheetDialogFragment;
import ee.app.conversamanager.view.RegularTextView;

public class ActivityChatWall extends ConversaActivity implements View.OnClickListener, OnMessageClickListener {

	private dbCustomer businessObject;
	private MessagesAdapter gMessagesAdapter;

	private boolean typingFlag = false;
	private boolean addAsContact;
	private boolean loading;
	private boolean loadMore;
	private boolean newMessagesFromNewIntent;
	private int itemPosition;

	private Handler isUserTypingHandler = new Handler();
	private RegularTextView mSubTitleTextView;
	private RecyclerView mRvWallMessages;
	private EditText mEtMessageText;
	private BottomSheetDialogFragment myBottomSheet;
	private ImageButton mBtnWallSend;
	private MediumTextView mTitleTextView;
	private SimpleDraweeView ivContactAvatar;

	private Timer typingTimer;

	public final static int PERMISSION_RQ = 84;

	public ActivityChatWall() {
		this.loading = true;
		this.loadMore = true;
		this.newMessagesFromNewIntent = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_wall);

		// Deactivate check internet connection
		checkInternetConnection = false;
		// EventBus should not unregister on onStop
		unregisterListener = false;

		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if(extras == null) {
				finish();
			} else {
				businessObject = extras.getParcelable(Const.iExtraCustomer);
				addAsContact = extras.getBoolean(Const.iExtraAddBusiness);
				itemPosition = extras.getInt(Const.iExtraPosition, -1);
			}
		} else {
			businessObject = savedInstanceState.getParcelable(Const.iExtraCustomer);
			addAsContact = savedInstanceState.getBoolean(Const.iExtraAddBusiness);
			itemPosition = savedInstanceState.getInt(Const.iExtraPosition, -1);
		}

		initialization();
		dbMessage.getAllMessageForChat(this, businessObject.getCustomerId(), 20, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Map<String, String> articleParams = new HashMap<>(1);
		String customer_id = ConversaApp.getInstance(this).getPreferences().getAccountBusinessId();
		articleParams.put("business", (customer_id == null) ? "" : customer_id);
		FlurryAgent.logEvent("manager_chat_duration", articleParams, true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FlurryAgent.endTimedEvent("manager_chat_duration");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(Const.iExtraCustomer, businessObject);
		outState.putBoolean(Const.iExtraAddBusiness, addAsContact);
		outState.putInt(Const.iExtraPosition, itemPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void openFromNotification(Intent intent) {
		Logger.error("openFromNotification", "New intent: " + intent.toString());
		dbCustomer business = intent.getParcelableExtra(Const.iExtraCustomer);

		if (business == null) {
			finish();
		} else {
			addAsContact = intent.getBooleanExtra(Const.iExtraAddBusiness, false);
			itemPosition = intent.getIntExtra(Const.iExtraPosition, -1);
			typingFlag = false;

			if (business.getCustomerId().equals(businessObject.getCustomerId())) {
				// Call for new messages
				int count = intent.getIntExtra(Const.kAppVersionKey, 1);
				newMessagesFromNewIntent = true;
				dbMessage.getAllMessageForChat(this, businessObject.getCustomerId(), count, 0);
			} else {
				// Change name and avatar
				setAvatarImage(-1);
				mTitleTextView.setText(business.getDisplayName());
				// Set new business reference
				businessObject = business;
				// Clean list of current messages and get new messages
				loadMore = true;
				gMessagesAdapter.clearMessages();
				mRvWallMessages.setEnabled(false);
				dbMessage.getAllMessageForChat(this, business.getCustomerId(), 20, 0);
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	private Runnable isUserTypingRunnable = new Runnable() {
		@Override
		public void run() {
			Logger.error("isUserTypingRunnable", "Try to send typing ended update");
			PubnubConnection.getInstance().userHasEndedTyping(businessObject.getCustomerId());
			typingFlag = false;
		}
	};

	private TextWatcher isUserTypingTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@SuppressWarnings("ConstantConditions")
		@Override
		public void afterTextChanged(Editable s) {
			isUserTypingHandler.removeCallbacks(isUserTypingRunnable);

			if (s.toString().isEmpty()) {
				mBtnWallSend.setEnabled(false);
				isUserTypingHandler.postDelayed(isUserTypingRunnable, 0);
			} else {
				mBtnWallSend.setEnabled(true);

				if (!typingFlag) {
					Logger.error("isUserTypingRunnable", "Try to send typing started update");
					PubnubConnection.getInstance().userHasStartedTyping(businessObject.getCustomerId());
					typingFlag = true;
				}

				isUserTypingHandler.postDelayed(isUserTypingRunnable, 3500);
			}
		}
	};

	@Override
	@SuppressWarnings("ConstantConditions")
	protected void initialization() {
		super.initialization();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		mTitleTextView = (MediumTextView) toolbar.findViewById(R.id.tvChatName);
		FrameLayout mBackButton = (FrameLayout) toolbar.findViewById(R.id.flBack);
		ImageButton mIbBackButton = (ImageButton) toolbar.findViewById(R.id.ibBack);
		mSubTitleTextView = (RegularTextView) toolbar.findViewById(R.id.tvChatStatus);
		mTitleTextView.setText(businessObject.getDisplayName());
		ivContactAvatar = (SimpleDraweeView) toolbar.findViewById(R.id.ivAvatarChat);

		setAvatarImage(itemPosition);
		setSupportActionBar(toolbar);

		mRvWallMessages = (RecyclerView) findViewById(R.id.rvWallMessages);
		mEtMessageText = (EditText) findViewById(R.id.etWallMessage);
		mBtnWallSend = (ImageButton) findViewById(R.id.btnWallSend);

		mBtnWallSend.setEnabled(false);

		myBottomSheet = MyBottomSheetDialogFragment.newInstance(businessObject.getCustomerId(), this);

		ImageButton mBtnOpenSlidingDrawer = (ImageButton) findViewById(R.id.btnSlideButton);

		mEtMessageText.setTypeface(ConversaApp.getInstance(this).getTfRalewayRegular());
		mEtMessageText.addTextChangedListener(isUserTypingTextWatcher);

		gMessagesAdapter = new MessagesAdapter(this,
				ConversaApp.getInstance(this).getPreferences().getAccountBusinessId(),
				this);
		LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRvWallMessages.setLayoutManager(manager);
		mRvWallMessages.setAdapter(gMessagesAdapter);
		mRvWallMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				// 1. If load more is true retrieve more messages otherwise skip
				if (loadMore) {
					final int lastVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager())
							.findFirstCompletelyVisibleItemPosition();
					final int totalItemCount = recyclerView.getLayoutManager().getItemCount();

					// 2. Check if app isn't checking for new messages and last visible item is on the top
					if (!loading && lastVisibleItem == 0) {
						gMessagesAdapter.addLoad(true);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								dbMessage.getAllMessageForChat(getApplicationContext(),
										businessObject.getCustomerId(), 20, totalItemCount);
							}
						}, 1900);
						loading = true;
					}
				}
			}
		});
		mRvWallMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v,
									   int left, int top, int right, final int bottom,
									   int oldLeft, int oldTop, int oldRight, final int oldBottom) {
				if (bottom < oldBottom) {
					Logger.error("onLayoutChange", "Bottom:" + bottom + " Old:" + oldBottom);
					mRvWallMessages.postDelayed(new Runnable() {
						@Override
						public void run() {
							mRvWallMessages.smoothScrollToPosition(
									gMessagesAdapter.getItemCount() - 1
							);
						}
					}, 100);
				}
			}
		});

		mBtnWallSend.setOnClickListener(this);
		mBtnOpenSlidingDrawer.setOnClickListener(this);
		mIbBackButton.setOnClickListener(this);
		mBackButton.setOnClickListener(this);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// Request permission to save videos in external storage
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RQ);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				navigateUp();
				return true;
		}

		return false;
	}

	@Override
	public void onBackPressed() {
		navigateUp();
	}

	private void navigateUp() {
		Intent upIntent = NavUtils.getParentActivityIntent(this);
		if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
			// This activity is NOT part of this app's task, so create a new task
			// when navigating up, with a synthesized back stack.
			TaskStackBuilder.create(this)
					// Add all of this activity's parents to the back stack
					.addNextIntentWithParentStack(upIntent)
					// Navigate up to the closest parent
					.startActivities();
		} else {
			// This activity is part of this app's task, so simply
			// navigate up to the logical parent activity.
			NavUtils.navigateUpTo(this, upIntent);
		}
	}

	@Override
	public void onMessageClick(dbMessage message, View view, int position) {
		switch (message.getMessageType()) {
			case Const.kMessageTypeText: {

				break;
			}
			case Const.kMessageTypeLocation: {
				Intent intent = new Intent(this, ActivityLocation.class);
				intent.putExtra(Const.LOCATION, "userLocation");
				intent.putExtra(Const.LATITUDE, message.getLatitude());
				intent.putExtra(Const.LONGITUDE, message.getLongitude());
				startActivity(intent);
				break;
			}
			case Const.kMessageTypeImage: {
				Intent i = new Intent(this, ActivityImageDetail.class);
				i.putExtra(ActivityImageDetail.EXTRA_IMAGE, message.getLocalUrl());
				ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
						view, 0, 0, view.getWidth(), view.getHeight());
				startActivity(i, options.toBundle());
				break;
			}
			case Const.kMessageTypeVideo: {
				break;
			}
			case Const.kMessageTypeAudio: {
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivAvatarChat:
				// Llamar a Servidor por foto y actualizar
				break;
			case R.id.ibBack:
			case R.id.flBack:
				onBackPressed();
				break;
			case R.id.btnSlideButton:
				myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());
				break;
			case R.id.btnWallSend:
				String body = mEtMessageText.getText().toString().trim();

				if (body.length() > 0) {
					mEtMessageText.setText("");
					SendMessageAsync.sendTextMessage(
							this,
							body,
							addAsContact,
							businessObject);
				}
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && data != null) {
			switch (requestCode) {
				case ActivityLocation.PICK_LOCATION_REQUEST: {
					SendMessageAsync.sendLocationMessage(
							this,
							data.getDoubleExtra("lat", 0),
							data.getDoubleExtra("lon", 0),
							addAsContact,
							businessObject);
					break;
				}
				case Const.CAPTURE_MEDIA: {
					String path = ImageFilePath.getPath(this, Uri.parse(data.getStringExtra("imageUri")));
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(path, options);

					SendMessageAsync.sendImageMessage(
							this,
							path,
							options.outWidth,
							options.outHeight,
							new File(path == null ? "" : path).length(),
							addAsContact,
							businessObject);
					break;
				}
				case Const.CAPTURE_VIDEO: {
					MediaMetadataRetriever retriever = new MediaMetadataRetriever();
					retriever.setDataSource(data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));
					int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
					int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
					int duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
					retriever.release();
					break;
				}
			}
		} else {
			Logger.error("onActivityResult", "Error");
		}
	}

	private void setAvatarImage(int position) {
		Uri uri;

		if (position == -1) {
			Random rand = new Random();
			position = rand.nextInt(14) + 1;
		} else {
			position++;
		}

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

		this.ivContactAvatar.setImageURI(uri);
	}

	/* ****************************************************************************************** */

	@Override
	public void noInternetConnection() {
		super.noInternetConnection();
		mBtnWallSend.setEnabled(false);
	}

	@Override
	public void yesInternetConnection() {
		super.yesInternetConnection();
		mBtnWallSend.setEnabled(true);
	}

	@Override
	public void MessagesGetAll(List<dbMessage> messages) {
		// 1. Add messages
		if (mRvWallMessages.getLayoutManager().getItemCount() == 0) {
			mRvWallMessages.setEnabled(true);

			// If messages size is zero there's no need to do anything
			if (messages.size() > 0) {
				// Update unread incoming messages
				dbMessage.updateViewMessages(this, businessObject.getCustomerId());
				// Set messages
				gMessagesAdapter.setMessages(messages);
				// As this is the first time we load messages, change visibility
				mRvWallMessages.setVisibility(View.VISIBLE);
				mRvWallMessages.scrollToPosition(gMessagesAdapter.getItemCount() - 1);
			}

			// Check if we need to load more messages
			if (messages.size() < 20) {
				loadMore = false;
			}

		} else {
			if (newMessagesFromNewIntent) {
				newMessagesFromNewIntent = false;
				gMessagesAdapter.addMessages(messages);
			} else {
				gMessagesAdapter.addLoad(false);

				// No need to check visibility, only add messages to adapter
				gMessagesAdapter.addMessages(messages);
				// Check if we need to load more messages
				if (messages.size() < 20) {
					loadMore = false;
				}
			}
		}

		// 2. Set loading as completed
		loading = false;
	}

	@Override
	public void MessageSent(dbMessage message) {
		// 1. Check visibility
		if (mRvWallMessages.getVisibility() == View.GONE) {
			mRvWallMessages.setVisibility(View.VISIBLE);
		}

		// 2. Check if user needs to be added
		if (addAsContact) {
			addAsContact = false;
		}

		if (ConversaApp.getInstance(this).getPreferences().getPlaySoundWhenSending()) {
			playSound(true);
		}

		// 3. Add message to adapter
		int visibleItemCount = mRvWallMessages.getChildCount();
		int firstVisibleItem = ((LinearLayoutManager) mRvWallMessages.getLayoutManager()).findFirstVisibleItemPosition();
		gMessagesAdapter.addMessage(message, firstVisibleItem, visibleItemCount);
		mRvWallMessages.scrollToPosition(gMessagesAdapter.getItemCount() - 1);
	}

	@Override
	public void MessageDeleted(List<String> response, MessageDeleteReason reason) {

	}

	@Override
	public void MessageUpdated(dbMessage message, MessageUpdateReason reason) {
		// 1. Get visible items and first visible item position
		int visibleItemCount = mRvWallMessages.getChildCount();
		int firstVisibleItem = ((LinearLayoutManager) mRvWallMessages.getLayoutManager()).findFirstVisibleItemPosition();
		// 2. Update message
		switch (reason) {
			case FILE_DOWNLOAD:
				gMessagesAdapter.updateImageView(message, firstVisibleItem, visibleItemCount);
				break;
			case VIEW:
				break;
			case STATUS:
				gMessagesAdapter.updateStatus(message, firstVisibleItem, visibleItemCount);
				break;
		}
	}

	@Override
	public void MessageReceived(dbMessage message) {
		// 1. Check if this message belongs to this conversation
		if (message.getFromUserId().equals(businessObject.getCustomerId())) {
			dbMessage.updateViewMessages(this, businessObject.getCustomerId());

			// 2. Check visibility
			if (mRvWallMessages.getVisibility() == View.GONE) {
				mRvWallMessages.setVisibility(View.VISIBLE);
			}

			if (ConversaApp.getInstance(this).getPreferences().getPlaySoundWhenReceiving()) {
				playSound(false);
			}

			// 3. Add to adapter
			int visibleItemCount = mRvWallMessages.getChildCount();
			int firstVisibleItem = ((LinearLayoutManager) mRvWallMessages.getLayoutManager()).findFirstVisibleItemPosition();
			gMessagesAdapter.addMessage(message, firstVisibleItem, visibleItemCount);
			mRvWallMessages.scrollToPosition(gMessagesAdapter.getItemCount() - 1);

			// 4. Update is typing
			onTypingMessage(businessObject.getCustomerId(), false);
		} else {
			super.MessageReceived(message);
		}
	}

	@Override
	public void onTypingMessage(String from, boolean isTyping) {
		if (from.equals(businessObject.getCustomerId())) {
			if (typingTimer != null) {
				typingTimer.cancel();
				typingTimer = null;
			}

			if (isTyping) {
				typingTimer = new Timer();
				typingTimer.schedule(new TimerTask() {
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showIsTyping(false);
							}
						});
					}
				}, 6000);
			}

			showIsTyping(isTyping);
		}
	}

	public void showIsTyping(boolean show) {
		if (show) {
			mSubTitleTextView.setText(R.string.is_typing);
			mSubTitleTextView.setVisibility(View.VISIBLE);
		} else {
			mSubTitleTextView.setVisibility(View.GONE);
		}
	}

	private void playSound(final boolean sound) {
		final SoundPool sounds;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			AudioAttributes attributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_NOTIFICATION)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build();
			sounds = new SoundPool.Builder()
					.setAudioAttributes(attributes)
					.build();
		} else {
			sounds = new SoundPool(15, AudioManager.STREAM_NOTIFICATION, 0);
		}

		sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
				/** soundId for Later handling of sound pool **/
				// in 2nd param u have to pass your desire ringtone
				sounds.play(soundId, 0.99f, 0.99f, 1, 0, 0.99f);
			}
		});

		if (sound) {
			sounds.load(getApplicationContext(), R.raw.message_sent, 1);
		} else {
			sounds.load(getApplicationContext(), R.raw.message_received, 1);
		}
	}

}