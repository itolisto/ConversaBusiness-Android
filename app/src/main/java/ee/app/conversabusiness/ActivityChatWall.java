package ee.app.conversabusiness;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.afollestad.materialcamera.MaterialCamera;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ee.app.conversabusiness.adapters.MessagesAdapter;
import ee.app.conversabusiness.extendables.ConversaActivity;
import ee.app.conversabusiness.interfaces.OnMessageClickListener;
import ee.app.conversabusiness.management.AblyConnection;
import ee.app.conversabusiness.messaging.MessageUpdateReason;
import ee.app.conversabusiness.messaging.SendMessageAsync;
import ee.app.conversabusiness.model.database.dbCustomer;
import ee.app.conversabusiness.model.database.dbMessage;
import ee.app.conversabusiness.utils.Const;
import ee.app.conversabusiness.utils.Logger;
import ee.app.conversabusiness.utils.Utils;
import ee.app.conversabusiness.view.LightTextView;
import ee.app.conversabusiness.view.MediumTextView;
import ee.app.conversabusiness.view.MyBottomSheetDialogFragment;

public class ActivityChatWall extends ConversaActivity implements View.OnClickListener,
		View.OnTouchListener, OnMessageClickListener {

	private dbCustomer businessObject;
	private MessagesAdapter gMessagesAdapter;

	private boolean typingFlag = false;
	private boolean addAsContact;
	private boolean loading;
	private boolean loadMore;
	private boolean newMessagesFromNewIntent;
	private int itemPosition;

	private Handler isUserTypingHandler = new Handler();
	private LightTextView mSubTitleTextView;
	private RecyclerView mRvWallMessages;
	private EditText mEtMessageText;
	private BottomSheetDialogFragment myBottomSheet;
	private ImageButton mBtnWallSend;
	private MediumTextView mTitleTextView;

	private Timer timer;
	private Timer typingTimer;

	public final static int CAMERA_RQ = 6969;
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

			if (business.getCustomerId().equals(businessObject.getCustomerId())) {
				// Call for new messages
				int count = intent.getIntExtra(Const.kAppVersionKey, 1);
				newMessagesFromNewIntent = true;
				dbMessage.getAllMessageForChat(this, businessObject.getCustomerId(), count, 0);
			} else {
				// Change name and avatar
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

	@Override
	protected void onResume() {
		super.onResume();
		startTimer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stoptimertask();
	}

	@SuppressWarnings("ConstantConditions")
	private Runnable isUserTypingRunnable = new Runnable() {
		@Override
		public void run() {
			Logger.error("isUserTypingRunnable", "Try to send typing ended update");
			AblyConnection.getInstance().userHasEndedTyping(businessObject.getCustomerId());
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
					AblyConnection.getInstance().userHasStartedTyping(businessObject.getCustomerId());
					typingFlag = true;
				}

				isUserTypingHandler.postDelayed(isUserTypingRunnable, 3500);
			}
		}
	};

	public void startTimer() {
		if (loading || timer != null) {
			return;
		}

		//set a new Timer
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (gMessagesAdapter != null && mRvWallMessages != null) {
					final int firstVisibleItem = ((LinearLayoutManager)mRvWallMessages.
							getLayoutManager()).findFirstCompletelyVisibleItemPosition();
					final int lastVisibleItem = ((LinearLayoutManager)mRvWallMessages.
							getLayoutManager()).findLastCompletelyVisibleItemPosition();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							gMessagesAdapter.updateTime(firstVisibleItem,
									(lastVisibleItem - firstVisibleItem) + 1);
						}
					});
				}
			}
		}, 0, 60000);
	}

	public void restartTimer() {
		stoptimertask();
		startTimer();
	}

	public void stoptimertask() {
		//stop the timer, if it's not already null
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	protected void initialization() {
		super.initialization();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		mTitleTextView = (MediumTextView) toolbar.findViewById(R.id.tvChatName);
		FrameLayout mBackButton = (FrameLayout) toolbar.findViewById(R.id.flBack);
		ImageButton mIbBackButton = (ImageButton) toolbar.findViewById(R.id.ibBack);
		mSubTitleTextView = (LightTextView) toolbar.findViewById(R.id.tvChatStatus);
		mTitleTextView.setText(businessObject.getDisplayName());

		setSupportActionBar(toolbar);

		mRvWallMessages = (RecyclerView) findViewById(R.id.rvWallMessages);
		mEtMessageText = (EditText) findViewById(R.id.etWallMessage);
		mBtnWallSend = (ImageButton) findViewById(R.id.btnWallSend);

		myBottomSheet = MyBottomSheetDialogFragment.newInstance(businessObject.getCustomerId(), this);

		ImageButton mBtnOpenSlidingDrawer = (ImageButton) findViewById(R.id.btnSlideButton);

		mEtMessageText.setTypeface(ConversaApp.getInstance(this).getTfRalewayRegular());
		mEtMessageText.addTextChangedListener(isUserTypingTextWatcher);

		gMessagesAdapter = new MessagesAdapter(this,
				ConversaApp.getInstance(this).getPreferences().getAccountBusinessId(),
				this);
		LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		manager.setReverseLayout(true);
		mRvWallMessages.setLayoutManager(manager);
		mRvWallMessages.setOnTouchListener(this);
		mRvWallMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				final int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager())
						.findLastCompletelyVisibleItemPosition();
				final int totalItemCount = recyclerView.getLayoutManager().getItemCount();

				// 1. Check if app isn't checking for new messages and last visible item is on the top
				if (!loading && lastVisibleItem == (totalItemCount - 1)) {
					// 2. If load more is true retrieve more messages otherwise skip
					if (loadMore) {
						gMessagesAdapter.addLoad(true);
						mRvWallMessages.scrollToPosition(totalItemCount);
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

		mRvWallMessages.setAdapter(gMessagesAdapter);

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
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
			case R.id.rvWallMessages:
				Utils.hideKeyboard(this);
				return false;
		}

		return true;
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
		// Make sure the request was successful
		if (resultCode == RESULT_OK) {
			// Check which request we're responding to
			switch (requestCode) {
				case ActivityCameraCrop.PICK_CAMERA_REQUEST:
				case ActivityCameraCrop.PICK_GALLERY_REQUEST: {
					SendMessageAsync.sendImageMessage(
							this,
							data.getStringExtra("result"),
							data.getIntExtra("width", 0),
							data.getIntExtra("height", 0),
							data.getLongExtra("bytes", 0),
							addAsContact,
							businessObject);
					break;
				}
				case ActivityLocation.PICK_LOCATION_REQUEST: {
					SendMessageAsync.sendLocationMessage(
							this,
							data.getDoubleExtra("lat", 0),
							data.getDoubleExtra("lon", 0),
							addAsContact,
							businessObject);
					break;
				}
				case CAMERA_RQ: {
					if (data.getType().equals("image/jpeg")) {
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(data.getData().getPath(), options);
						SendMessageAsync.sendImageMessage(
								this,
								data.getData().toString(),
								options.outWidth,
								options.outHeight,
								data.getLongExtra(MaterialCamera.SIZE_EXTRA, 0),
								addAsContact,
								businessObject);
					}
//					else {
//						MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//						retriever.setDataSource(data.getData().getPath());
//						int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
//						int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
//						int duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//						retriever.release();
//					}
					break;
				}
			}
		} else if (requestCode == CAMERA_RQ) {
			if (data != null && data.getSerializableExtra(MaterialCamera.ERROR_EXTRA) != null) {
				Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
				if (e != null) {
					Logger.error("onActivityResult", e.getMessage());
				}
			}
		}
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
			}

			// Check if we need to load more messages
			if (messages.size() < 20) {
				loadMore = false;
			}
		} else {
			if (newMessagesFromNewIntent) {
				newMessagesFromNewIntent = false;
				gMessagesAdapter.addMessages(messages, 0);
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
		startTimer();
	}

	@Override
	public void MessageSent(dbMessage response) {
		// 1. Check visibility
		if (mRvWallMessages.getVisibility() == View.GONE) {
			mRvWallMessages.setVisibility(View.VISIBLE);
		}

		// 2. Check if user needs to be added
		if (addAsContact) {
			addAsContact = false;
		}

		// 3. Add message to adapter
		gMessagesAdapter.addMessage(response);
		mRvWallMessages.scrollToPosition(0);
	}

	@Override
	public void MessageDeleted(List<String> message) {

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

			// 3. Add to adapter
			gMessagesAdapter.addMessage(message);
			mRvWallMessages.scrollToPosition(0);

			// 4. Update is typing
			onTypingMessage(businessObject.getCustomerId(), false);
		} else {
			super.MessageReceived(message);
		}
	}

	@Override
	public void onTypingMessage(String from, boolean isTyping) {
		if (from.equals(businessObject.getCustomerId())) {
			if (isTyping) {
				showIsTyping(true);

				if (typingTimer != null) {
					typingTimer.cancel();
					typingTimer = null;
				}

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
			} else {
				if (typingTimer != null) {
					typingTimer.cancel();
					typingTimer = null;
				}

				showIsTyping(false);
			}
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

}