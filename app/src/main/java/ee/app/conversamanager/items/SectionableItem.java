package ee.app.conversamanager.items;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import java.util.List;

import ee.app.conversamanager.R;
import ee.app.conversamanager.holders.CategoryViewHolder;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;

/**
 * Created by edgargomez on 2/20/17.
 */

public class SectionableItem extends AbstractSectionableItem<CategoryViewHolder, HeaderItem>
{

    final private Context context;
    final private String id;
    final private String title;

    public SectionableItem(HeaderItem header, Context context, String id, String title) {
        super(header);
        this.id = id;
        this.title = title;
        this.context = context;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof SectionableItem) {
            SectionableItem inItem = (SectionableItem) inObject;
            return this.getId().equals(inItem.getId());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        if (context == null) {
            return "";
        } else {
            return (TextUtils.isEmpty(title)) ? context.getString(R.string.category) : title;
        }
    }

    @Override
    public int getLayoutRes() {
        return R.layout.category_item;
    }

    @Override
    public CategoryViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new CategoryViewHolder(view, adapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindViewHolder(final FlexibleAdapter adapter, CategoryViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            if (getHeader().getId().equals("0")) {
                if (position == adapter.getSectionItems(getHeader()).size()) {
                    holder.removeDivider(true);
                } else {
                    holder.removeDivider(false);
                }
            }
        } else {
            if (getHeader().getId().equals("0")) {
                final SectionableItem item = this;
                holder.mIbDelete.setVisibility(View.VISIBLE);
                holder.mIbDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.mItemClickListener.onItemClick(v, (adapter.getSectionItems(getHeader()).indexOf(item) + 1) * -1);
                    }
                });

                if (position == adapter.getSectionItems(getHeader()).size()) {
                    holder.removeDivider(true);
                }
            } else {
                holder.mIbDelete.setVisibility(View.GONE);
                holder.removeDivider(false);
            }

            holder.mTvCategoryTitle.setText(getTitle());
        }
    }

    @Override
    public String toString() {
        return "[" + id + "," + title + "," + getHeader().getId() + "]";
    }
}
