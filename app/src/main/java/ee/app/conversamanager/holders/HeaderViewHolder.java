package ee.app.conversamanager.holders;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ee.app.conversamanager.R;
import ee.app.conversamanager.view.MediumTextView;

/**
 * Created by edgargomez on 10/31/16.
 */

public class HeaderViewHolder extends BaseHolder {

    public MediumTextView mRtvHeader;

    public HeaderViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView, activity);
        this.mRtvHeader = (MediumTextView) itemView.findViewById(R.id.rtvHeader);
    }

    public void setHeaderTitle(String title) {
        mRtvHeader.setText((title == null) ? "Encabezado" : title);
    }

}
