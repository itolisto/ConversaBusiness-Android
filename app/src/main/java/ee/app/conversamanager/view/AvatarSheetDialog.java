package ee.app.conversamanager.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import ee.app.conversamanager.ActivityCameraCrop;
import ee.app.conversamanager.R;
import ee.app.conversamanager.utils.Const;

/**
 * Created by edgargomez on 9/9/16.
 */
@SuppressLint("ValidFragment")
public class AvatarSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    boolean hideView;
    final WeakReference<AppCompatActivity>mActivity;

    public static AvatarSheetDialog newInstance(AppCompatActivity mActivity, boolean hideView) {
        AvatarSheetDialog f = new AvatarSheetDialog(mActivity);
        Bundle args = new Bundle();
        args.putBoolean(Const.LOCATION, hideView);
        f.setArguments(args);
        return f;
    }

    public AvatarSheetDialog(AppCompatActivity mActivity) {
        this.mActivity = new WeakReference<>(mActivity);
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
        v.findViewById(R.id.btnGallery).setOnClickListener(this);
        v.findViewById(R.id.btnCamera).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        if (mActivity.get() == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.btnCamera: {
                Intent intent = new Intent(mActivity.get(), ActivityCameraCrop.class);
                intent.putExtra("type", "camera");
                mActivity.get().startActivityForResult(intent, ActivityCameraCrop.PICK_CAMERA_REQUEST);
                break;
            }
            case R.id.btnGallery: {
                Intent intent = new Intent(mActivity.get(), ActivityCameraCrop.class);
                intent.putExtra("type", "gallery");
                mActivity.get().startActivityForResult(intent, ActivityCameraCrop.PICK_GALLERY_REQUEST);
                break;
            }
            case R.id.btnView: {
                // View
                break;
            }
        }

        dismiss();
    }

}