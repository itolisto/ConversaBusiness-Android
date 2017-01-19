package ee.app.conversamanager.interfaces;

import android.view.View;

import ee.app.conversamanager.model.database.dbMessage;

/**
 * Created by edgargomez on 10/31/16.
 */

public interface OnMessageClickListener {
    void onMessageClick(dbMessage message, View view, int position);
}
