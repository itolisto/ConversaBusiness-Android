package ee.app.conversamanager.jobs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.parse.ParseException;

import java.io.IOException;
import java.io.InputStream;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by edgargomez on 10/12/16.
 */
public class DownloadAvatarJob extends Job {

    private final String TAG = DownloadAvatarJob.class.getSimpleName();

    public DownloadAvatarJob(String group) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(group).addTags(group));
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk, add item to database
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(
                        ConversaApp.getInstance(getApplicationContext())
                                .getPreferences()
                                .getAccountAvatar()
                )
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ConversaApp.getInstance(getApplicationContext())
                        .getPreferences()
                        .setAccountAvatar(
                                Utils.saveAvatarToInternalStorage(
                                        getApplicationContext(),
                                        bitmap)
                        );
            } else {
                Logger.error(TAG, "Request received unsuccessful response code: " + response.code());
            }
        } catch (IOException|IllegalStateException e) {
            Logger.error(TAG, "Image download error: " + e.getMessage());
        }
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