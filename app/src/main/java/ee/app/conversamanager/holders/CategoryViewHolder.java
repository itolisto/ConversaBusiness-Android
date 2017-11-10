package ee.app.conversamanager.holders;

import android.view.View;
import android.widget.ImageButton;

import ee.app.conversamanager.R;
import ee.app.conversamanager.view.MediumTextView;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by edgargomez on 10/31/16.
 */

public class CategoryViewHolder extends FlexibleViewHolder {

    public MediumTextView mTvCategoryTitle;
    public ImageButton mIbDelete;
    public View mVDivider;

    public CategoryViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter, false);
        this.mTvCategoryTitle = (MediumTextView) view.findViewById(R.id.tvCategoryTitle);
        this.mVDivider = view.findViewById(R.id.vDivider);
        this.mIbDelete = (ImageButton) view.findViewById(R.id.ibDelete);
    }

    public void removeDivider(boolean remove) {
        mVDivider.setVisibility(remove ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

}
