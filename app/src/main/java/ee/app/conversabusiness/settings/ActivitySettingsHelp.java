package ee.app.conversabusiness.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import ee.app.conversabusiness.R;
import ee.app.conversabusiness.extendables.ConversaActivity;

/**
 * Created by edgargomez on 10/10/16.
 */

public class ActivitySettingsHelp extends ConversaActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_help);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
