package ee.app.conversamanager.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.HashMap;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;

/**
 * Created by edgargomez on 1/16/17.
 */

public class SettingsRedirectJob extends Job {

    private final String TAG = SettingsRedirectJob.class.getSimpleName();
    private final boolean redirect;

    public SettingsRedirectJob(String businessId, boolean redirect) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(businessId).addTags(businessId));
        this.redirect = redirect;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("businessId", ConversaApp.getInstance(getApplicationContext())
                .getPreferences().getAccountBusinessId());
        params.put("redirect", redirect);

        NetworkingManager.getInstance().postSync(getApplicationContext(),"business/updateBusinessRedirect", params);

        ConversaApp.getInstance(getApplicationContext())
                .getPreferences()
                .setAccountRedirect(redirect);

        if (redirect) {
            ConversaApp.getInstance(getApplicationContext())
                    .getPreferences()
                    .setAccountStatus(-1);
        } else {
            ConversaApp.getInstance(getApplicationContext())
                    .getPreferences()
                    .setAccountStatus(0);
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof FirebaseCustomException) {
            if (AppActions.validateParseException((FirebaseCustomException) throwable)) {
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