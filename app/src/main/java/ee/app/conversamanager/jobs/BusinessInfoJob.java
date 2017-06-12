package ee.app.conversamanager.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.util.HashMap;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.management.AblyConnection;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;

/**
 * Created by edgargomez on 10/12/16.
 */

public class BusinessInfoJob extends Job {

    private final String TAG = BusinessInfoJob.class.getSimpleName();

    public BusinessInfoJob(String businessId) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(businessId).addTags(businessId));
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        HashMap<String, String> params = new HashMap<>();

        String json = ParseCloud.callFunction("getBusinessId", params);

        JSONObject jsonRootObject = new JSONObject(json);

        String objectId = jsonRootObject.optString("ob", "");
        String displayName = jsonRootObject.optString("dn", "");
        String paidPlan = jsonRootObject.optString("pp", "");
        String country = jsonRootObject.optString("ct", "");
        String conversaId = jsonRootObject.optString("id", "");
        String about = jsonRootObject.optString("ab", "");
        boolean verified = jsonRootObject.optBoolean("vd", false);
        boolean redirect = jsonRootObject.optBoolean("rc", false);
        String avatar = jsonRootObject.optString("av", "");
        int status = jsonRootObject.optInt("st", 0);

        if (status == -1) {
            redirect = true;
        }

        // 1. Save Customer object id
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountBusinessId(objectId, false);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountDisplayName(displayName, false);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountPaidPlan(paidPlan);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountCountry(country);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountConversaId(conversaId);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountAbout(about);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountVerified(verified);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountRedirect(redirect);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountAvatar(avatar);
        ConversaApp.getInstance(getApplicationContext()).getPreferences().setAccountStatus(status);
        // 2. Subscribe to Customer channels
        AblyConnection.getInstance().subscribeToChannels();
        AblyConnection.getInstance().subscribeToPushChannels();

        ConversaApp.getInstance(getApplicationContext())
                .getJobManager()
                .addJob(new DownloadAvatarJob(objectId));
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof ParseException) {
            if (AppActions.validateParseException((ParseException) throwable)) {
                AppActions.appLogout(getApplicationContext(), true);
                return RetryConstraint.CANCEL;
            }
        }

        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specify a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        RetryConstraint rtn = RetryConstraint.createExponentialBackoff(runCount, 1000);
        rtn.setNewPriority(Priority.MID);
        return rtn;
    }

}