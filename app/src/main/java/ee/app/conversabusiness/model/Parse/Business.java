package ee.app.conversabusiness.model.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Business")
public class Business extends ParseObject {

    public String getConversaID() {
        return getString("conversaID");
    }

    public String getAbout() {
        return getString("about");
    }

    public String getStatus() {
        return getString("status");
    }

    public Account getBusinessInfo() {
        return (Account)getParseObject("businessInfo");
    }

}