package ee.app.conversamanager.holders;

import android.view.View;

import ee.app.conversamanager.R;
import ee.app.conversamanager.view.MediumTextView;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by edgargomez on 10/31/16.
 */

public class HeaderViewHolder extends FlexibleViewHolder {

    public MediumTextView mRtvHeader;

    public HeaderViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter, true);//True for sticky
        this.mRtvHeader = (MediumTextView) view.findViewById(R.id.rtvHeader);
    }

}
