package ee.app.conversabusiness.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import ee.app.conversabusiness.R;
import ee.app.conversabusiness.utils.Logger;
import ee.app.conversabusiness.view.CircleImageView;

public class StatisticsAdapter extends BaseAdapter {

    private AppCompatActivity mActivity;
    private List<String> mStatistics;

    public StatisticsAdapter(AppCompatActivity activity, List<String> categories) {
        mStatistics = categories;
        mActivity = activity;
    }

    @Override
    public Object getItem(int position) { return null; }

    @Override
    public long getItemId(int position) { return 0; }

    @Override
    public int getCount() { return (mStatistics == null) ? 0 : mStatistics.size(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        try{
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.statistic_item, parent, false);
                holder = new ViewHolder(convertView);

                if(position == 1) {
                    String[] split = new String[2];
                    split = mStatistics.get(position).split(";");
                    holder.mTvStatisticInfo.setText(split[1]);
                } else {
                    holder.mTvStatisticInfo.setText(mStatistics.get(position));
                }

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }


        }catch (Exception e){
            Logger.error("MessagesAdapter", e.getMessage());
        }

        return convertView;
    }

    class ViewHolder {
        public TextView mTvStatisticInfo;
        public CircleImageView mIvStatisticImage;
        public RelativeLayout mRlStatisticItem;

        ViewHolder(View view) {
            mTvStatisticInfo  = (TextView) view.findViewById(R.id.tvStatisticInfo);
            mIvStatisticImage = (CircleImageView) view.findViewById(R.id.ivStatisticImage);
            mRlStatisticItem  = (RelativeLayout) view.findViewById(R.id.rlStatisticItem);
        }
    }

    public void setItems(List<String> statistics) { mStatistics = statistics; }
}

