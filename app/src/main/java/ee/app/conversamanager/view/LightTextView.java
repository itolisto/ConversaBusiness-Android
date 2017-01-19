package ee.app.conversamanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import ee.app.conversamanager.ConversaApp;

/**
 * Created by edgargomez on 5/11/15.
 */
public class LightTextView extends TextView {

    public LightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!this.isInEditMode())
            this.setTypeface(ConversaApp.getInstance(context).getTfRalewayLight());
    }

}
