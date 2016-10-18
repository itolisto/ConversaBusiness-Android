package ee.app.conversabusiness.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Customer")
public class Customer extends ParseObject {

    public String getName() {
        return getString("name");
    }

    public String getDisplayName() {
        return getString("displayName");
    }

    public ParseFile getAvatar() {
        return getParseFile("avatar");
    }

}