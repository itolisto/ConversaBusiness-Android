package ee.app.conversabusiness.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by edgargomez on 4/15/16.
 */
@ParseClassName("Business")
public class Business extends ParseObject {

    public void setDisplayName(String displayName) {
        put("displayName", displayName);
    }

    public void setConversaId(String conversaId) {
        put("conversaID", conversaId);
    }

    public void setAvatar(ParseFile avatar) {
        put("avatar", avatar);
    }

    public void setRedirectToConversa(boolean redirectToConversa) {
        put("redirectToConversa", redirectToConversa);
    }

    public void setAbout(String about) {
        put("about", about);
    }

    public void setStatus(String status) {
        put("status", status);
    }

    public void setTags(List<String> tags) {
        put("tags", tags);
    }

}