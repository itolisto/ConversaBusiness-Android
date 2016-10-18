package ee.app.conversabusiness;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import ee.app.conversabusiness.extendables.ConversaFragment;
import ee.app.conversabusiness.model.database.Statistics;

/**
 * Created by edgargomez on 10/8/16.
 */

public class FragmentAccount extends ConversaFragment implements View.OnClickListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Statistics mStatistics;
    private GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srlStatistics);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.orange, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getStatisticsAsync();
            }
        });

        gridView = (GridView) rootView.findViewById(R.id.gvStatistics);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), mStatistics.getTitle(position, (AppCompatActivity)getActivity()), Toast.LENGTH_SHORT).show();
            }
        });

        TextView mTvName, mTvAbout, mTvCountry, mTvCity, mTvFounded;

//        mTvName = (TextView) rootView.findViewById(R.id.tvBusinessName);
//        mTvName.setText(ConversaApp.getPreferences().getUserName());
//        mTvAbout = (TextView) rootView.findViewById(R.id.tvBusinessAbout);
//        mTvAbout.setText(ConversaApp.getDB().getAbout());
//
//        mTvCountry = (TextView) rootView.findViewById(R.id.tvCountry);
//        mTvCountry.setText(ConversaApp.getDB().getCountry());
//        mTvCity = (TextView) rootView.findViewById(R.id.tvCity);
//        mTvCity.setText(ConversaApp.getDB().getCity());
//        mTvFounded = (TextView) rootView.findViewById(R.id.tvFounded);
//        mTvFounded.setText(ConversaApp.getDB().getFounded());

        return rootView;
    }

    @Override
    public void onResume() {
        getStatisticsAsync();
        super.onResume();
    }

    private void getStatisticsAsync() {
//        CouchDB.findStatistics(
//                new GetStatisticsFinish(), getActivity(), true
//        );
    }

//    private class GetStatisticsFinish implements ResultListener<Statistics> {
//        @Override
//        public void onResultsSucceded(Statistics result) {
//
//            mStatistics = result;
//
//            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
//                mSwipeRefreshLayout.setRefreshing(false);
//                if(mStatisticsAdapter != null)
//                    mStatisticsAdapter.notifyDataSetChanged();
//            }
//
//            //Cada elemento pasa a una Lista
//            int b = mStatistics.getTotalOfStatistics();
//            List<String> st = new ArrayList<>();
//
//            for(int a = 0; a < b; a++) {
//                st.add(mStatistics.getField(a));
//            }
//
//            if(mStatisticsAdapter == null) {
//                mStatisticsAdapter = new StatisticsAdapter(
//                        (ActionBarActivity) getActivity(), st);
//                gridView.setAdapter(mStatisticsAdapter);
//            } else {
//                mStatisticsAdapter.setItems(st);
//                mStatisticsAdapter.notifyDataSetChanged();
//            }
//        }
//
//        @Override
//        public void onResultsFail() {
//
//            mStatistics = ConversaApp.getDB().getStatistics();
//
//            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
//                mSwipeRefreshLayout.setRefreshing(false);
//                if(mStatisticsAdapter != null)
//                    mStatisticsAdapter.notifyDataSetChanged();
//            }
//
//            //Cada elemento pasa a una Lista
//            int b = mStatistics.getTotalOfStatistics();
//            List<String> st = new ArrayList<>();
//
//            for(int a = 0; a < b; a++) {
//                st.add(mStatistics.getField(a));
//            }
//
//            if(mStatisticsAdapter == null) {
//                mStatisticsAdapter = new StatisticsAdapter(
//                        (ActionBarActivity) getActivity(), st);
//                gridView.setAdapter(mStatisticsAdapter);
//            } else {
//                mStatisticsAdapter.setItems(st);
//            }
//
//        }
//    }

    @Override
    public void onClick(View v) {

    }

}
