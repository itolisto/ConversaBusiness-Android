package ee.app.conversamanager.view;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ee.app.conversamanager.ActivityImageDetail;
import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.camara.ImagePickerDemo;

/**
 * Created by edgargomez on 9/9/16.
 */
@SuppressLint("ValidFragment")
public class AvatarSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    boolean hideView;
    final AppCompatActivity mActivity;

    public static AvatarSheetDialog newInstance(AppCompatActivity mActivity, boolean hideView) {
        AvatarSheetDialog f = new AvatarSheetDialog(mActivity);
        Bundle args = new Bundle();
        args.putBoolean(Const.LOCATION, hideView);
        f.setArguments(args);
        return f;
    }

    public AvatarSheetDialog(AppCompatActivity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideView = getArguments().getBoolean(Const.LOCATION, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_avatar, container, false);

        if (hideView) {
            v.findViewById(R.id.btnView).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.btnView).setOnClickListener(this);
        }

        v.findViewById(R.id.btnCamera).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCamera: {
                /*
                new SandriosCamera(mActivity, Const.CAPTURE_MEDIA)
                        .setShowPicker(true)
                        .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                        .enableImageCropping(false)
                        .setDefaultMediaQuality(QualityOptions.QUALITY_MID)
                        .launchCamera();
                        */
                Intent intent = new Intent(mActivity, ImagePickerDemo.class);
                intent.putExtra("picker", "single");
                //mActivity.startActivity(intent);
                mActivity.startActivityForResult(intent, Const.CAPTURE_MEDIA);

                break;
            }
            case R.id.btnView: {
                Intent i = new Intent(mActivity, ActivityImageDetail.class);
                i.putExtra(ActivityImageDetail.EXTRA_IMAGE, ConversaApp.getInstance(mActivity).getPreferences().getAccountAvatar());
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
                        view, 0, 0, view.getWidth(), view.getHeight());
                startActivity(i, options.toBundle());
                break;
            }
        }

        dismiss();
    }

}