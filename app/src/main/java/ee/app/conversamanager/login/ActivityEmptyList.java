package ee.app.conversamanager.login;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.BaseActivity;

/**
 * Created by edgargomez on 1/3/17.
 */

public class ActivityEmptyList extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_empty);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        findViewById(R.id.btnRegister).setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister: {
                Intent intent = new Intent(this, ActivityRegister.class);
                startActivity(intent);
                break;
            }
        }
    }

}