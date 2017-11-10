package ee.app.conversamanager.interfaces;

import android.view.View;

import ee.app.conversamanager.model.nBusiness;

/**
 * Created by edgargomez on 01/03/17.
 */

public interface OnBusinessClickListener {
    void onBusinessClick(nBusiness business, View v, int position);
}
