package ee.app.conversamanager.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

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
import ee.app.conversamanager.interfaces.FunctionCallback;
import ee.app.conversamanager.items.HeaderItem;
import ee.app.conversamanager.items.SectionableItem;
import ee.app.conversamanager.networking.FirebaseCustomException;
import ee.app.conversamanager.networking.NetworkingManager;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.view.RegularTextView;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.ISectionable;

/**
 * Created by edgargomez on 1/15/17.
 */

public class ActivitySettingsCategory extends ConversaActivity implements FlexibleAdapter.OnItemClickListener, View.OnClickListener {

    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private HeaderItem selectedHeader;
    private HeaderItem categoriesHeader;
    private List<ISectionable> originalSelected;
    private FloatingActionButton mFabSave;
    private int limit;

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

        originalSelected = new ArrayList<>(2);
        RecyclerView mRvCategories = (RecyclerView) findViewById(R.id.rvCategories);
        mFabSave = (FloatingActionButton) findViewById(R.id.fabSave);
        mFabSave.setOnClickListener(this);

        mAdapter = new FlexibleAdapter<>(new ArrayList<AbstractFlexibleItem>(), this);
        mRvCategories.setLayoutManager(new SmoothScrollLinearLayoutManager(this));
        mRvCategories.setAdapter(mAdapter);
        mRvCategories.setHasFixedSize(true);
        mRvCategories.setItemAnimator(new DefaultItemAnimator());

        mAdapter.setUnlinkAllItemsOnRemoveHeaders(true)
                .setDisplayHeadersAtStartUp(true) //Show Headers at startUp!
                .setStickyHeaders(true); //Make headers sticky

        String language = ConversaApp.getInstance(this).getPreferences().getLanguage();

        if (language.equals("zz")) {
            if (Locale.getDefault().getLanguage().startsWith("es")) {
                language = "es";
            } else {
                language = "en";
            }
        }

        HashMap<String, String> params = new HashMap<>(2);
        params.put("language", language);
        params.put("businessId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());

        NetworkingManager.getInstance().post("business/getBusinessCategories", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object json, FirebaseCustomException exception) {
                if (exception != null) {
                    if (AppActions.validateParseException(exception)) {
                        AppActions.appLogout(getApplicationContext(), true);
                    } else {
                        ((RegularTextView)findViewById(R.id.rtvInfo)).setText(
                                getString(R.string.sett_category_info_error)
                        );
                    }
                } else {
                    try {
                        // Add headers and both lists (selectedCategory,sortedCategory)
                        if (selectedHeader == null || categoriesHeader == null) {
                            selectedHeader = new HeaderItem("0", getString(R.string.sett_category_selected_title));
                            categoriesHeader = new HeaderItem("1", getString(R.string.sett_category_available_title));
                        }

                        JSONObject jsonRootObject = new JSONObject(json.toString());
                        JSONArray unsortedCategories = jsonRootObject.optJSONArray("ids");
                        JSONArray selectedIds = jsonRootObject.optJSONArray("select");
                        limit = jsonRootObject.optInt("limit", 0);

                        int size = unsortedCategories.length();
                        List<SectionableItem>sortedCategory = new ArrayList<>(size);

                        for (int i = 0; i < size; i++) {
                            JSONObject jsonCategory = unsortedCategories.getJSONObject(i);
                            SectionableItem category = new SectionableItem(
                                    categoriesHeader,
                                    getApplicationContext(),
                                    jsonCategory.optString("id", ""),
                                    jsonCategory.optString("na", "")
                            );
                            sortedCategory.add(category);
                        }

                        Collections.sort(sortedCategory, new Comparator<SectionableItem>() {
                            @Override
                            public int compare(final SectionableItem object1, final SectionableItem object2) {
                                return object1.getTitle().compareTo(object2.getTitle());
                            }
                        });

                        int sizeSelected = selectedIds.length();
                        List<String>selectedIdsString = new ArrayList<>(sizeSelected);

                        for (int i = 0; i < sizeSelected; i++) {
                            selectedIdsString.add(selectedIds.optString(i, ""));
                        }

                        List<SectionableItem>selectedCategory = new ArrayList<>(sizeSelected);

                        if (sizeSelected > 0) {
                            for (int i = 0; i < size; i++) {
                                SectionableItem category = sortedCategory.get(i);
                                if (category.getId().equals(selectedIdsString.get(0))) {
                                    selectedCategory.add(category);
                                    sortedCategory.remove(i);
                                    selectedIdsString.remove(0);
                                    i = -1;
                                    size--;
                                    sizeSelected--;

                                    if (sizeSelected == 0) {
                                        break;
                                    }
                                }
                            }
                        }

                        Collections.sort(selectedCategory, new Comparator<SectionableItem>() {
                            @Override
                            public int compare(final SectionableItem object1, final SectionableItem object2) {
                                return object1.getTitle().compareTo(object2.getTitle());
                            }
                        });

                        originalSelected.addAll(selectedCategory);

                        // Add headers and both lists (selectedCategory,sortedCategory)
                        mAdapter.addSection(categoriesHeader);
                        mAdapter.addSection(selectedHeader);

                        sizeSelected = selectedCategory.size();

                        for (int i = 0; i < sizeSelected; i++) {
                            mAdapter.addItemToSection(selectedCategory.get(i), selectedHeader, i);
                        }

                        for (int i = 0; i < size; i++) {
                            mAdapter.addItemToSection(sortedCategory.get(i), categoriesHeader, i);
                        }

                        ((RegularTextView)findViewById(R.id.rtvDetail)).setText(
                                getString(R.string.sett_category_detail_title, limit)
                        );

                        findViewById(R.id.rlInfo).setVisibility(View.GONE);
                    } catch (JSONException ignored) {
                        ((RegularTextView)findViewById(R.id.rtvInfo)).setText(
                                getString(R.string.sett_category_info_error)
                        );
                    }
                }
            }
        });
    }

    @Override
    public boolean onItemClick(int position) {
        if (mAdapter.getItem(position) instanceof HeaderItem)
            return false;

        if (position < 0) {
            // Return to normal position
            position = position * -1;
            int size = mAdapter.getSectionItems(selectedHeader).size();

            SectionableItem flexibleItem = (SectionableItem) mAdapter.getItem(position);

            mAdapter.removeItem(position);
            flexibleItem.setHeader(categoriesHeader);

            mAdapter.addItemToSection(flexibleItem, categoriesHeader, new Comparator<IFlexible>() {
                @Override
                public int compare(IFlexible object1, IFlexible object2) {
                    return ((SectionableItem) object1).getTitle().compareTo(((SectionableItem) object2).getTitle());
                }
            });

            // Update divider
            if (position == size) {
                if (position > 1) {
                    mAdapter.updateItem(mAdapter.getItem(position - 1), "updateDivider");
                }
            } else {
                mAdapter.updateItem(mAdapter.getItem(position), "updateDivider");

                if (size > 1) {
                    mAdapter.updateItem(mAdapter.getItem(position + 1), "updateDivider");
                }
            }
        } else {
            SectionableItem flexibleItem = (SectionableItem) mAdapter.getItem(position);

            if (flexibleItem.getHeader().getId().equals("1")) {
                if (mAdapter.getSectionItems(selectedHeader).size() + 1 <= limit) {
                    mAdapter.removeItem(position);
                    flexibleItem.setHeader(selectedHeader);

                    mAdapter.addItemToSection(flexibleItem, selectedHeader, new Comparator<IFlexible>() {
                        @Override
                        public int compare(IFlexible object1, IFlexible object2) {
                            return ((SectionableItem) object1).getTitle().compareTo(((SectionableItem) object2).getTitle());
                        }
                    });

                    position = mAdapter.getGlobalPositionOf(flexibleItem);

                    if (position == -1)
                        return false;

                    int size = mAdapter.getSectionItems(selectedHeader).size();

                    //Logger.error("ADD", "[" + flexibleItem.getTitle() + "," + position + "," + size + "]");

                    // Update divider
                    if (position == size) {
                        if (position > 1) {
                            mAdapter.updateItem(mAdapter.getItem(position - 1), "updateDivider");
                            mAdapter.updateItem(mAdapter.getItem(position), "updateDivider");
                        }
                    } else {
                        mAdapter.updateItem(mAdapter.getItem(position), "updateDivider");

                        if (size > 1) {
                            mAdapter.updateItem(mAdapter.getItem(position + 1), "updateDivider");
                        }
                    }
                } else {
                    new MaterialDialog.Builder(this)
                            .content(getString(R.string.sett_category_limit_warning))
                            .positiveColorRes(R.color.purple)
                            .positiveText(getString(android.R.string.ok))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    return false;
                }
            } else {
                return false;
            }
        }

        // Find out if items are different, if they are show Save
        if (mAdapter.getSectionItems(selectedHeader).equals(originalSelected)) {
            mFabSave.hide();
        } else {
            mFabSave.show();
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabSave) {
            int sizeSelected = mAdapter.getSectionItems(selectedHeader).size();
            List<String>selectedIdsString = new ArrayList<>(sizeSelected);

            for (int i = 0; i < sizeSelected; i++) {
                selectedIdsString.add(((SectionableItem)mAdapter.getSectionItems(selectedHeader).get(i)).getId());
            }

            Logger.error("saveCategories", "items:" + selectedIdsString);

            HashMap<String, Object> params = new HashMap<>(3);
            params.put("categories", selectedIdsString);
            params.put("businessId", ConversaApp.getInstance(this).getPreferences().getAccountBusinessId());
            params.put("limit", limit);
            NetworkingManager.getInstance().post("business/updateBusinessCategory", params, new FunctionCallback<Integer>() {
                @Override
                public void done(Integer jsonCategories, FirebaseCustomException e) {
                    if (e != null) {
                        if (AppActions.validateParseException(e)) {
                            AppActions.appLogout(getApplicationContext(), true);
                        } else {
                            if (!isFinishing()) {
                                new MaterialDialog.Builder(getApplicationContext())
                                        .content(getString(R.string.sett_category_limit_warning))
                                        .positiveColorRes(R.color.purple)
                                        .positiveText(getString(android.R.string.ok))
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        }
                    } else {
                        if (!isFinishing()) {
                            mFabSave.hide();
                            originalSelected.clear();
                            originalSelected.addAll(mAdapter.getSectionItems(selectedHeader));
                        }
                    }
                }
            });
        }
    }
}