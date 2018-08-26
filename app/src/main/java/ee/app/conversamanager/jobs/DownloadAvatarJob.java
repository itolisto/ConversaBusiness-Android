package ee.app.conversamanager.jobs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.InputStream;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.events.account.AccountEvent;
import ee.app.conversamanager.events.account.AvatarEvent;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.utils.Utils;

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
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://luminous-inferno-3905-business");
        StorageReference storageRef = storage.getReference();
        // Create a child reference
        // avatarRef now points to avatar folder
        String path = ConversaApp.getInstance(getApplicationContext()).getPreferences().getAccountBusinessId() + "/avatar/avatar.jpg";

        StorageReference avatarRef = storageRef.child(path);

        InputStream inputStream = avatarRef.getStream().getResult().getStream();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        ConversaApp.getInstance(getApplicationContext())
                .getPreferences()
                .setAccountAvatar(
                        Utils.saveAvatarToInternalStorage(
                                getApplicationContext(),
                                bitmap)
                );

        // Notify loading
        EventBus.getDefault().post(new AvatarEvent());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
        // Notify loading
        EventBus.getDefault().post(new AvatarEvent());
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
        return RetryConstraint.CANCEL;
    }

}