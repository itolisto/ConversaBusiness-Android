package ee.app.conversamanager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ee.app.conversamanager.extendables.ConversaFragment;
import ee.app.conversamanager.settings.PreferencesKeys;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Utils;
import ee.app.conversamanager.view.BoldTextView;
import ee.app.conversamanager.view.RegularTextView;

/**
 * Created by edgargomez on 8/23/16.
 */
public class FragmentStatistics extends ConversaFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private PieChart mLcMessageChart;
    private SwipeRefreshLayout mSrlStats;
    private RelativeLayout mRlInfo;
    private RelativeLayout mRlRetry;
    private RelativeLayout mRlChartInfo;
    private RegularTextView mRtvChartMessage;
    private AVLoadingIndicatorView mPbLoadingStats;
    private boolean load;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        mRlInfo = (RelativeLayout) rootView.findViewById(R.id.rlInfo);
        mRlRetry = (RelativeLayout) rootView.findViewById(R.id.rlRetry);

        mRlChartInfo = (RelativeLayout) rootView.findViewById(R.id.rlChartInfo);
        mRtvChartMessage = (RegularTextView) rootView.findViewById(R.id.rtvChartMessage);

        mPbLoadingStats = (AVLoadingIndicatorView) rootView.findViewById(R.id.pbLoadingStats);

        rootView.findViewById(R.id.btnRetry).setOnClickListener(this);

        mSrlStats = (SwipeRefreshLayout) rootView.findViewById(R.id.srlStats);
        mSrlStats.setOnRefreshListener(this);
        mSrlStats.setColorSchemeColors(Color.GREEN, Color.BLUE, Color.RED, Color.CYAN);

        mLcMessageChart = (PieChart) rootView.findViewById(R.id.pcMessageChart);
        mLcMessageChart.setUsePercentValues(true);
        mLcMessageChart.setDrawSlicesUnderHole(true);
        mLcMessageChart.setHoleRadius((float) 0.58);
        mLcMessageChart.setTransparentCircleRadius((float) 0.61);
        mLcMessageChart.getDescription().setEnabled(false);

        mLcMessageChart.setDrawHoleEnabled(false);
        mLcMessageChart.setRotationAngle((float)0.0);
        mLcMessageChart.setRotationEnabled(false);
        mLcMessageChart.setHighlightPerTapEnabled(true);

        Legend l = mLcMessageChart.getLegend();
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace((float)0.0);
        l.setYEntrySpace((float)0.0);
        l.setYOffset((float)0.0);

        mLcMessageChart.setEntryLabelColor(android.R.color.white);
        mLcMessageChart.setEntryLabelTypeface(
                ConversaApp.getInstance(getActivity()).getTfRalewayLight()
        );
        mLcMessageChart.setEntryLabelTextSize((float) 12.0);
        mLcMessageChart.animateX(1400, Easing.EasingOption.EaseOutBack);

        load = false;

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!load && !ConversaApp.getInstance(getActivity()).getPreferences().getAccountBusinessId().isEmpty()) {
            // Change load value so next time this fragment is loaded
            // we don't call Parse Server for information automatically
            changeStatisticsPeriod();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesKeys.ACCOUNT_BUSINESS_ID_KEY)) {
            changeStatisticsPeriod();
        }
    }

    private void changeStatisticsPeriod() {
        if (load) {
            return;
        }

        if (mRlInfo.getVisibility() == View.GONE) {
            mRlInfo.setVisibility(View.VISIBLE);
            mRlRetry.setVisibility(View.GONE);
        }

        mPbLoadingStats.smoothToShow();

        load = true;

        String language = ConversaApp.getInstance(getActivity()).getPreferences().getLanguage();

        if (language.equals("zz")) {
            if (Locale.getDefault().getLanguage().startsWith("es")) {
                language = "es";
            } else {
                language = "en";
            }
        }

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("businessId", ConversaApp.getInstance(getActivity()).getPreferences().getAccountBusinessId());
        params.put("language", language);

        ParseCloud.callFunctionInBackground("getBusinessStatisticsAll", params, new FunctionCallback<String>() {
            @Override
            public void done(String jsonStatistics, ParseException e) {
                load = false;

                if (mSrlStats.isRefreshing())
                    mSrlStats.setRefreshing(false);

                mPbLoadingStats.smoothToHide();

                if (e != null) {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(getActivity(), true);
                    } else {
                        mRlRetry.setVisibility(View.VISIBLE);
                    }
                } else {
                    try {
                        JSONObject jsonRootObject = new JSONObject(jsonStatistics);

                        JSONObject all = jsonRootObject.getJSONObject("all");
                        JSONObject charts = jsonRootObject.getJSONObject("charts");

                        int sent = all.optInt("ms", 0);
                        int received = all.optInt("mr", 0);
                        int favs = all.optInt("nf", 0);
                        int views = all.optInt("np", 0);
                        int conversations = all.optInt("cn", 0);
                        int links = all.optInt("lc", 0);

                        ((BoldTextView) getView().findViewById(R.id.btvSent)).setText(Utils.numberWithFormat(sent));
                        ((BoldTextView) getView().findViewById(R.id.btvReceived)).setText(Utils.numberWithFormat(received));
                        ((BoldTextView) getView().findViewById(R.id.btvFavs)).setText(Utils.numberWithFormat(favs));
                        ((BoldTextView) getView().findViewById(R.id.btvViews)).setText(Utils.numberWithFormat(views));
                        ((BoldTextView) getView().findViewById(R.id.btvConversations)).setText(Utils.numberWithFormat(conversations));
                        ((BoldTextView) getView().findViewById(R.id.btvLinks)).setText(Utils.numberWithFormat(links));

                        updateChart(sent, received);

                        mRlInfo.setVisibility(View.GONE);
                    } catch (Exception ignored) {
                        mRlRetry.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }


    private void updateChart(int sent, int received) {
        if (sent > 0 || received > 0) {
            ArrayList<PieEntry> entries = new ArrayList<>(2);
            entries.add(new PieEntry((float) sent, getString(R.string.stats_chart_sent_title)));
            entries.add(new PieEntry((float) received, getString(R.string.stats_chart_received_title)));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setSliceSpace(3f);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dataSet.setColors(
                        getResources().getColor(R.color.pieChartReceived, null),
                        getResources().getColor(R.color.pieChartSent, null));
            } else {
                dataSet.setColors(
                        getResources().getColor(R.color.pieChartReceived),
                        getResources().getColor(R.color.pieChartSent));
            }

            PieData data = new PieData(dataSet);
            DecimalFormat pFormatter = new DecimalFormat();
            pFormatter.setMaximumFractionDigits(1);
            pFormatter.setMultiplier(1);
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setPercent('%');
            pFormatter.setDecimalFormatSymbols(symbols);
            data.setValueFormatter(new PercentFormatter(pFormatter));
            data.setValueTextSize(11f);
            data.setValueTextColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
            data.setValueTypeface(
                    ConversaApp.getInstance(getActivity()).getTfRalewayLight()
            );

            mLcMessageChart.setData(data);
            mLcMessageChart.highlightValues(null);
            mLcMessageChart.invalidate();

            mRlChartInfo.setVisibility(View.GONE);
            mRtvChartMessage.setText("");
        } else {
            mRlChartInfo.setVisibility(View.VISIBLE);
            mRtvChartMessage.setText(getString(R.string.stats_no_chart_available));
        }
    }

    @Override
    public void onRefresh() {
        changeStatisticsPeriod();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRetry) {
            changeStatisticsPeriod();
        }
    }
}
