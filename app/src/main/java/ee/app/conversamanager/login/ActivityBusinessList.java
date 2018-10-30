package ee.app.conversamanager.login;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ee.app.conversamanager.R;
import ee.app.conversamanager.adapters.BusinessAdapter;
import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.interfaces.OnBusinessClickListener;
import ee.app.conversamanager.model.nBusiness;
import ee.app.conversamanager.utils.Const;

/**
 * Created by edgargomez on 1/3/17.
 */

public class ActivityBusinessList extends BaseActivity implements OnBusinessClickListener {

    private RecyclerView mRvResults;
    private BusinessAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_list);
        initialization();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.clear();
    }

    @Override
    protected void initialization() {
        super.initialization();
        mRvResults = (RecyclerView) findViewById(R.id.rvResults);
        adapter = new BusinessAdapter(this, this);
        mRvResults.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRvResults.setAdapter(adapter);
        adapter.addItems(getIntent().getExtras().<nBusiness>getParcelableArrayList(Const.iExtraList));
    }

    @Override
    public void onBackPressed() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }
    }

    @Override
    public void onBusinessClick(nBusiness business, View v, int position) {
        Intent intent = new Intent(this, ActivityContact.class);
        intent.putExtra(Const.iExtraBusinessReclaimed, business);
        startActivity(intent);
    }

}