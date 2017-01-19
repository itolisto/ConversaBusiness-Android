package ee.app.conversamanager;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

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

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ee.app.conversamanager.extendables.ConversaFragment;
import ee.app.conversamanager.settings.PreferencesKeys;
import ee.app.conversamanager.utils.AppActions;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.view.BoldTextView;

/**
 * Created by edgargomez on 8/23/16.
 */
public class FragmentStatistics extends ConversaFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PieChart mLcMessageChart;
    private boolean load;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

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
        l.setXEntrySpace((float)7.0);
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
        load = true;
        HashMap<String, Object> params = new HashMap<>();
        params.put("business", ConversaApp.getInstance(getActivity()).getPreferences().getAccountBusinessId());
        ParseCloud.callFunctionInBackground("getBusinessStatisticsAll", params, new FunctionCallback<String>() {
            @Override
            public void done(String jsonStatistics, ParseException e) {
                Logger.error("getBusinessStatisticsAll", "\nResult: " + jsonStatistics);
                if (e != null) {
                    if (AppActions.validateParseException(e)) {
                        AppActions.appLogout(getActivity(), true);
                    }
                } else {
                    try {
                        JSONObject jsonRootObject = new JSONObject(jsonStatistics);

                        int sent = jsonRootObject.optInt("ms", 0);
                        int received = jsonRootObject.optInt("mr", 0);
                        int favs = jsonRootObject.optInt("nf", 0);
                        int views = jsonRootObject.optInt("np", 0);

                        if (sent > 999) {
                            ((BoldTextView) getView().findViewById(R.id.mtvSent)).setText(
                                    String.format(Locale.getDefault(), "%.1f", sent/1000.0)
                            );
                        } else {
                            ((BoldTextView) getView().findViewById(R.id.mtvSent)).setText(
                                    String.valueOf(sent)
                            );
                        }

                        if (received > 999) {
                            ((BoldTextView) getView().findViewById(R.id.mtvReceived)).setText(
                                    String.format(Locale.getDefault(), "%.1f", received/1000.0)
                            );
                        } else {
                            ((BoldTextView) getView().findViewById(R.id.mtvReceived)).setText(
                                    String.valueOf(received)
                            );
                        }

                        if (favs > 999) {
                            ((BoldTextView) getView().findViewById(R.id.mtvFavs)).setText(
                                    String.format(Locale.getDefault(), "%.1f", favs/1000.0)
                            );
                        } else {
                            ((BoldTextView) getView().findViewById(R.id.mtvFavs)).setText(
                                    String.valueOf(favs)
                            );
                        }

                        if (views > 999) {
                            ((BoldTextView) getView().findViewById(R.id.mtvViews)).setText(
                                    String.format(Locale.getDefault(), "%.1f", views/1000.0)
                            );
                        } else {
                            ((BoldTextView) getView().findViewById(R.id.mtvViews)).setText(
                                    String.valueOf(views)
                            );
                        }

                        updateChart(sent, received);
                    } catch (Exception ignored) {}
                }
            }
        });
    }


    private void updateChart(int sent, int received) {
        ArrayList<PieEntry> entries = new ArrayList<>(2);
        entries.add(new PieEntry((float)sent, getString(R.string.stats_chart_sent_title)));
        entries.add(new PieEntry((float)received, getString(R.string.stats_chart_received_title)));

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
    }

}
