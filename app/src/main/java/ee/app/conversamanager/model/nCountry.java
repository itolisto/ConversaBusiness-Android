package ee.app.conversamanager.model;

/**
 * Created by edgargomez on 10/5/16.
 */
public class nCountry {

    private final String mId;
    private final String mName;

    public nCountry(String mId, String mName) {
        this.mId = mId;
        this.mName = mName;
    }

    public String getId() { return mId; }
    public String getName() { return mName; }

}