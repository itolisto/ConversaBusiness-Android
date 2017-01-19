package ee.app.conversamanager.holders;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import ee.app.conversamanager.R;
import ee.app.conversamanager.interfaces.OnBusinessClickListener;
import ee.app.conversamanager.model.nBusiness;
import ee.app.conversamanager.utils.Utils;

/**
 * Created by edgargomez on 10/31/16.
 */

public class BusinessViewHolder extends BaseHolder {

    public TextView tvBusiness;
    public TextView tvConversaId;
    public SimpleDraweeView sdvCategoryImage;
    public nBusiness business;

    private OnBusinessClickListener listener;

    public BusinessViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView, activity);

        this.tvBusiness = (TextView) itemView.findViewById(R.id.mtvDisplayName);
        this.tvConversaId = (TextView) itemView.findViewById(R.id.ltvConversaId);
        this.sdvCategoryImage = (SimpleDraweeView) itemView.findViewById(R.id.sdvBusinessImage);

        itemView.setOnClickListener(this);
    }

    public void setBusiness(nBusiness business, OnBusinessClickListener listener) {
        this.business = business;
        this.listener = listener;

        this.tvBusiness.setText(business.getDisplayName());
        this.tvConversaId.setText("@".concat(business.getConversaId()));

        Uri uri = Utils.getUriFromString(business.getAvatarThumbFileId());

        if (uri == null) {
            uri = Utils.getDefaultImage(activity, R.drawable.ic_business_default);
        }

        this.sdvCategoryImage.setImageURI(uri);
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onBusinessClick(business, view, getAdapterPosition());
        }
    }

}
