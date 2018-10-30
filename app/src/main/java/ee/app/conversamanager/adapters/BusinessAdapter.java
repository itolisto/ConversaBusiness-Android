package ee.app.conversamanager.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import ee.app.conversamanager.R;
import ee.app.conversamanager.holders.BaseHolder;
import ee.app.conversamanager.holders.BusinessViewHolder;
import ee.app.conversamanager.interfaces.OnBusinessClickListener;
import ee.app.conversamanager.model.nBusiness;

public class BusinessAdapter extends RecyclerView.Adapter<BaseHolder> {

    private final AppCompatActivity mActivity;
    private List<nBusiness> mBusiness;
    private OnBusinessClickListener listener;

    public BusinessAdapter(AppCompatActivity activity, OnBusinessClickListener listener) {
        this.mActivity = activity;
        this.mBusiness = new ArrayList<>(1);
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return mBusiness.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BusinessViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.business_item, parent, false),
                this.mActivity);
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int i) {
        ((BusinessViewHolder) holder).setBusiness(mBusiness.get(i), listener);
    }

    public void addItems(List<nBusiness> business) {
        mBusiness.addAll(business);
        notifyItemRangeInserted(mBusiness.size(), business.size());
    }

    public void clear() {
        int size = mBusiness.size();
        mBusiness.clear();
        notifyItemRangeRemoved(0, size);
    }

}

