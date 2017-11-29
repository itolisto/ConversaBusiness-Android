package ee.app.conversamanager.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Customer")
public class Customer extends ParseObject {

    public String getDisplayName() {
        return getString("displayName");
    }

}