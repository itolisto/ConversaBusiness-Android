package ee.app.conversamanager.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.ConversaActivity;
import ee.app.conversamanager.model.nCategory;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.view.RegularTextView;

/**
 * Created by edgargomez on 1/15/17.
 */

public class ActivitySettingsCategory extends ConversaActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_category);
        initialization();
    }

    @Override
    protected void initialization() {
        super.initialization();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.preferences__category);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        rvCategories
         */

        String language = ConversaApp.getInstance(this).getPreferences().getLanguage();

        if (language.equals("zz")) {
            if (Locale.getDefault().getLanguage().startsWith("es")) {
                language = "es";
            } else {
                language = "en";
            }
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("language", language);
        params.put("objectId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());

        ParseCloud.callFunctionInBackground("getBusinessCategories", params, new FunctionCallback<String>() {
            @Override
            public void done(String jsonCategories, ParseException e) {
                if (e != null) {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(getApplicationContext(), true);
                    }
                } else {
                    try {
                        JSONObject jsonRootObject = new JSONObject(jsonCategories);

                        JSONArray unsortedIds = jsonRootObject.optJSONArray("ids");
                        JSONArray selIds = jsonRootObject.optJSONArray("select");
                        int limit = jsonRootObject.optInt("limit", 3);

                        ((RegularTextView)findViewById(R.id.rtvDetail)).setText(
                                getString(R.string.sett_category_detail_title, limit)
                        );

                        int size = unsortedIds.length();
                        List<nCategory>unsortedCategory = new ArrayList<>(size);

                        for (int i = 0; i < size; i++) {
                            JSONObject jsonCategory = unsortedIds.getJSONObject(i);

                            nCategory category = new nCategory(
                                    jsonCategory.optString("id", ""),
                                    jsonCategory.optString("na", ""),
                                    "");
                            unsortedCategory.add(category);
                        }

                        Collections.sort(unsortedCategory, new Comparator<nCategory>() {
                            @Override
                            public int compare(final nCategory object1, final nCategory object2) {
                                return object1.getCategoryName(getApplicationContext())
                                        .compareTo(object2.getCategoryName(getApplicationContext()));
                            }
                        });

                        int sizeSelected = selIds.length();
                        List<String>unsortedSelectedCategory = new ArrayList<>(limit);

                        for (int i = 0; i < sizeSelected; i++) {
                            unsortedSelectedCategory.add(unsortedIds.getString(i));
                        }

                        List<nCategory>selectedCategory = new ArrayList<>(limit);

                        if (sizeSelected > 0) {
                            for (int i = 0; i < size; i++) {
                                nCategory category = unsortedCategory.get(i);
                                if (category.getCategoryName(getApplicationContext()).equals(unsortedSelectedCategory.get(0))) {
                                    selectedCategory.add(category);
                                    unsortedCategory.remove(i);
                                    unsortedSelectedCategory.remove(0);
                                    i = -1;
                                    size--;
                                    sizeSelected--;

                                    if (sizeSelected == 0) {
                                        break;
                                    }
                                }
                            }
                        }

                        Collections.sort(selectedCategory, new Comparator<nCategory>() {
                            @Override
                            public int compare(final nCategory object1, final nCategory object2) {
                                return object1.getCategoryName(getApplicationContext())
                                        .compareTo(object2.getCategoryName(getApplicationContext()));
                            }
                        });

                        // Add headers and both lists (selectedCategory,unsortedCategory)


                        findViewById(R.id.rlInfo).setVisibility(View.GONE);
                    } catch (JSONException ignored) {}
                }
            }
        });
    }

}