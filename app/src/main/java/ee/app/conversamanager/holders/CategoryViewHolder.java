package ee.app.conversamanager.holders;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import ee.app.conversamanager.R;
import ee.app.conversamanager.interfaces.OnCategoryClickListener;
import ee.app.conversamanager.model.nCategory;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.MediumTextView;

/**
 * Created by edgargomez on 10/31/16.
 */

public class CategoryViewHolder extends BaseHolder {

    private OnCategoryClickListener listener;
    private MediumTextView tvCategoryTitle;
    private nCategory category;
    private View vDivider;

    public CategoryViewHolder(View itemView, AppCompatActivity activity, OnCategoryClickListener listener) {
        super(itemView, activity);
        this.tvCategoryTitle = (MediumTextView) itemView.findViewById(R.id.tvCategoryTitle);
        this.vDivider = itemView.findViewById(R.id.vDivider);
        this.listener = listener;
        itemView.setOnClickListener(this);
    }

    public void setCategory(nCategory category) {
        this.category = category;

        tvCategoryTitle.setText(category.getCategoryName(activity));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.getResources().getDimensionPixelSize(R.dimen.category_item_divider_height));

        if (category.getRemoveDividerMargin()) {
            params.setMargins(0, 0, 0, 0);
        } else {
            params.setMargins(Utils.dpToPixels(activity, 35), 0, 0, 0);
        }

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        vDivider.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onCategoryClick(category, itemView, getAdapterPosition());
        }
    }

}
