package ee.app.conversabusiness.interfaces;

import android.view.View;

import ee.app.conversabusiness.model.database.dbMessage;

/**
 * Created by edgargomez on 10/31/16.
 */

public interface OnMessageClickListener {
    void onMessageClick(dbMessage message, View view, int position);
}
