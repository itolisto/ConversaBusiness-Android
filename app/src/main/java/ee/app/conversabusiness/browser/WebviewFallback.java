package ee.app.conversabusiness.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * A Fallback that opens a Webview when Custom Tabs is not available
 * Created by hitherejoe
 *
 */

public class WebviewFallback implements CustomTabActivityHelper.CustomTabFallback {
    @Override
    public void openUri(Activity activity, Uri uri) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
        activity.startActivity(intent);
    }
}