package ee.app.conversabusiness;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentHome extends Fragment {

//    private SwipeRefreshLayout mSwipeRefreshLayout;
//    private Statistics mStatistics;
//    private StatisticsAdapter mStatisticsAdapter;
//    private GridView gridView;
//    private CircleImageView ivLogo;
//    private ProgressBar pbLoadLogo;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, container, false);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}