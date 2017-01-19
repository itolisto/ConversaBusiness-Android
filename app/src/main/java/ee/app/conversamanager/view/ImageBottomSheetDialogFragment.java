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
import android.widget.ImageButton;

import java.lang.ref.WeakReference;

import ee.app.conversamanager.ActivityCameraCrop;
import ee.app.conversamanager.R;

/**
 * Created by edgargomez on 9/9/16.
 */
@SuppressLint("ValidFragment")
public class ImageBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    final WeakReference<AppCompatActivity>mActivity;

    public static ImageBottomSheetDialogFragment newInstance(AppCompatActivity context) {
        return new ImageBottomSheetDialogFragment(context);
    }

    public ImageBottomSheetDialogFragment(AppCompatActivity mActivity) {
        this.mActivity = new WeakReference<>(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_avatar, container, false);
		ImageButton mBtnCamera = (ImageButton) v.findViewById(R.id.btnCamera);
		ImageButton mBtnGallery = (ImageButton) v.findViewById(R.id.btnGallery);
		mBtnCamera.setOnClickListener(this);
		mBtnGallery.setOnClickListener(this);
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
        }

        dismiss();
    }

}