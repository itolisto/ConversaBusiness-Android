package ee.app.conversamanager.settings;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.interfaces.FunctionCallback;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.MediumTextView;
import ee.app.conversamanager.view.RegularTextView;

/**
 * Created by edgargomez on 11/24/16.
 */

public class ActivitySettingsProfile extends ConversaActivity implements View.OnClickListener {

    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();
    static float sAnimatorScale = 2;

    // General
    private ColorDrawable mBackground;
    private CardView mCvContainer;
    private View mVwStatus;
    private boolean liked;
    private int followers;
    private final int ANIM_DURATION = 500;
    // Profile views
    private SimpleDraweeView mSdvBusinessHeader;
    private RegularTextView mBtvFollowers;
    // Action buttons
    private Button mBtnFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        checkInternetConnection = false;

        initialization();

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null) {
            ViewTreeObserver observer = mCvContainer.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mCvContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                    runEnterAnimation();
                    return true;
                }
            });
        }
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    public void runEnterAnimation() {
        final long duration = (long) (ANIM_DURATION * sAnimatorScale) / 2;

        // Animate scale and translation to go from thumbnail to full size
        mCvContainer.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sDecelerator);

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();
    }

    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     * when we actually switch activities)
     */
    public void runExitAnimation(final Runnable endAction) {
        final long duration = (long) (ANIM_DURATION * sAnimatorScale) / 8;

        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // First, slide/fade text out of the way
        // Animate image back to thumbnail size/location
        mCvContainer.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sAccelerator).
                withEndAction(endAction);
    }

    @Override
    protected void initialization() {
        super.initialization();

        liked = false;

        mCvContainer = (CardView) findViewById(R.id.cvContainer);
        mVwStatus = findViewById(R.id.vStatus);

        mBackground = new ColorDrawable(ResourcesCompat.getColor(getResources(),
                R.color.profile_light_background, null));

        findViewById(R.id.topLevelLayout).setBackground(mBackground);
        findViewById(R.id.topLevelLayout).setOnClickListener(this);
        mCvContainer.setOnClickListener(this);

        // Profile views
        mSdvBusinessHeader = (SimpleDraweeView) findViewById(R.id.sdvProfileHeader);
        SimpleDraweeView mSdvBusinessImage = (SimpleDraweeView) findViewById(R.id.sdvProfileAvatar);
        MediumTextView mMtvBusinessName = (MediumTextView) findViewById(R.id.mtvBusinessName);
        RegularTextView mBtvConversaId = (RegularTextView) findViewById(R.id.rtvConversaId);
        mBtvFollowers = (RegularTextView) findViewById(R.id.rtvFollowers);
        // Action buttons
        mBtnFavorite = (Button) findViewById(R.id.btnFavorite);

        mMtvBusinessName.setText(ConversaApp.getInstance(this).getPreferences().getAccountDisplayName());
        mBtvConversaId.setText("@".concat(ConversaApp.getInstance(this).getPreferences().getAccountConversaId()));

        Uri uri = Utils.getUriFromString(ConversaApp.getInstance(this).getPreferences().getAccountAvatar());

        if (uri == null) {
            uri = Utils.getDefaultImage(this, R.drawable.ic_business_default);
        }

        mSdvBusinessImage.setImageURI(uri);

        mBtnFavorite.setOnClickListener(this);
        findViewById(R.id.btnStartChat).setOnClickListener(this);
        findViewById(R.id.btnShare).setOnClickListener(this);
        findViewById(R.id.btnCloseProfile).setOnClickListener(this);
        mBtnFavorite.setEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtnFavorite.setBackground
                    (getResources().getDrawable(R.drawable.ic_fav, null));
        } else {
            mBtnFavorite.setBackground
                    (getResources().getDrawable(R.drawable.ic_fav));
        }

        // Call Parse for registry
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("businessId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());
        params.put("count", false);

        NetworkingManager.getInstance().post("general/getBusinessProfile", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object json, FirebaseCustomException exception) {
                if(exception == null) {
                    parseResult(json.toString());
                } else {
                    if (AppActions.validateParseException(exception)) {
                        AppActions.appLogout(getApplicationContext(), true);
                    } else {
                        parseResult("");
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.topLevelLayout: {
                onBackPressed();
                break;
            }
            case R.id.btnFavorite: {
                if (liked) {
                    liked = false;
                    followers--;

                    if (followers < 0)
                        followers = 0;

                    setFollowersText(followers);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav_not, null));
                    } else {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav_not));
                    }
                } else {
                    liked = true;
                    followers++;
                    setFollowersText(followers);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav, null));
                    } else {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav));
                    }
                }

                Animation animationScaleUp = AnimationUtils.loadAnimation(this, R.anim.pop_out);
                Animation animationScaleDown = AnimationUtils.loadAnimation(this, R.anim.pop_in);

                AnimationSet growShrink = new AnimationSet(true);
                growShrink.addAnimation(animationScaleUp);
                growShrink.addAnimation(animationScaleDown);
                mBtnFavorite.startAnimation(growShrink);
                break;
            }
            case R.id.btnStartChat: {
                break;
            }
            case R.id.btnShare: {
                final Intent intent_one = new Intent(android.content.Intent.ACTION_SEND);
                intent_one.setType("text/plain");
                // Add data to the intent, the receiving app will decide what to do with it.
                intent_one.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_using_conversa));
                intent_one.putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_using_conversa_text, ConversaApp.getInstance(this).getPreferences().getAccountDisplayName(), "https://conversa.link/" + ConversaApp.getInstance(this).getPreferences().getAccountConversaId()));

                final List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent_one, 0);

                List<String> appNames = new ArrayList<>(2);
                List<Drawable> appIcons = new ArrayList<>(2);

                for (ResolveInfo info : activities) {
                    appNames.add(info.loadLabel(getPackageManager()).toString());
                    String packageName = info.activityInfo.packageName;

                    try {
                        Drawable icon = getPackageManager().getApplicationIcon(packageName);
                        appIcons.add(icon);
                    } catch (PackageManager.NameNotFoundException e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            appIcons.add(getResources().getDrawable(R.drawable.ic_business_default, null));
                        } else {
                            appIcons.add(getResources().getDrawable(R.drawable.ic_business_default));
                        }
                    }
                }

                ListAdapter adapter = new ArrayAdapterWithIcon(this, appNames, appIcons);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.settings_share_conversa));
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResolveInfo info = activities.get(which);
                        //if (info.activityInfo.packageName.equals("com.facebook.katana")) {
                        // Facebook was chosen
                        //}
                        // Start the selected activity
                        intent_one.setPackage(info.activityInfo.packageName);
                        startActivity(intent_one);
                    }
                });

                AlertDialog share = builder.create();
                share.show();
                break;
            }
            case R.id.btnCloseProfile: {
                onBackPressed();
                break;
            }
        }
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it is complete.
     */
    @Override
    public void onBackPressed() {
        runExitAnimation(new Runnable() {
            public void run() {
                finish();
            }
        });
    }

    private void setFollowersText(int followers) {
        mBtvFollowers.setText(Utils.numberWithFormat(followers));
    }

    private void parseResult(String result) {
        try {
            if (!result.isEmpty()) {
                JSONObject jsonRootObject = new JSONObject(result);

                followers = jsonRootObject.optInt("followers", 0);
                String headerUrl = jsonRootObject.optString("header", null);
                boolean favorite = jsonRootObject.optBoolean("favorite", false);
                int status = jsonRootObject.optInt("status", 0);

                liked = favorite;

                if (favorite) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav, null));
                    } else {
                        mBtnFavorite.setBackground
                                (getResources().getDrawable(R.drawable.ic_fav));
                    }
                }

                setFollowersText(followers);

                Uri uri = Utils.getUriFromString(headerUrl);

                if (uri != null) {
                    mSdvBusinessHeader.setImageURI(uri);
                }

                GradientDrawable shapeDrawable;

                switch (status) {
                    case 0: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_online);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_online);
                        }
                        break;
                    }
                    case 1: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_away);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_away);
                        }
                        break;
                    }
                    default: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            shapeDrawable = (GradientDrawable) getDrawable(R.drawable.circular_status_offline);
                        } else {
                            shapeDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.circular_status_offline);
                        }
                        break;
                    }
                }

                mVwStatus.setBackground(shapeDrawable);
            }
        } catch (JSONException e) {
            Logger.error("parseResult", e.getMessage());
        } finally {
            mBtnFavorite.setEnabled(true);
        }
    }

    private class ArrayAdapterWithIcon extends ArrayAdapter<String> {

        private List<Drawable> images;

        ArrayAdapterWithIcon(Context context, List<String> items, List<Drawable> images) {
            super(context, android.R.layout.select_dialog_item, items);
            this.images = images;
        }

        @Override
        public @NonNull
        View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), null, null, null);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), null, null, null);
            }
            textView.setCompoundDrawablePadding(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
            return view;
        }
    }

}