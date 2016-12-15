package ee.app.conversabusiness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;

import ee.app.conversabusiness.extendables.ConversaFragment;
import ee.app.conversabusiness.utils.AppActions;
import ee.app.conversabusiness.utils.Logger;

/**
 * Created by edgargomez on 8/23/16.
 */
public class FragmentStatistics extends ConversaFragment implements View.OnClickListener {

    private LineChart mLcMessageChart;
    private boolean loading;
    private boolean load;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        Button mBtnLastHours = (Button) rootView.findViewById(R.id.btnLastHours);
        Button mBtnLastWeek = (Button) rootView.findViewById(R.id.btnLastWeek);
        Button mBtnLastMonth = (Button) rootView.findViewById(R.id.btnLastMonth);

        mBtnLastHours.setOnClickListener(this);
        mBtnLastWeek.setOnClickListener(this);
        mBtnLastMonth.setOnClickListener(this);

        mLcMessageChart = (LineChart) rootView.findViewById(R.id.lcMessageChart);

        mLcMessageChart.setTouchEnabled(false);
        mLcMessageChart.setDragEnabled(false);

        loading = false;
        load = false;

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLastHours:
                if (!loading) {
                    changeStatisticsPeriod(1);
                } else {
                    showLoadingAlert();
                }
                break;
            case R.id.btnLastWeek:
                if (!loading) {
                    changeStatisticsPeriod(2);
                } else {
                    showLoadingAlert();
                }
                break;
            case R.id.btnLastMonth:
                if (!loading) {
                    changeStatisticsPeriod(3);
                } else {
                    showLoadingAlert();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!load && !ConversaApp.getInstance(getActivity()).getPreferences().getAccountBusinessId().isEmpty()) {
            // Change load value so next time this fragment is loaded
            // we don't call Parse Server for information automatically
            load = true;
            changeStatisticsPeriod(1);
        }
    }

    private void changeStatisticsPeriod(int value) {
        loading = true;
        HashMap<String, Object> params = new HashMap<>();
        params.put("business", ConversaApp.getInstance(getActivity()).getPreferences().getAccountBusinessId());
        params.put("timeperiod", value);
        ParseCloud.callFunctionInBackground("businessStatistics", params, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                Logger.error("changeStatisticsPeriod", "\nResult: " + object);
                if (e == null) {

                } else {
                    AppActions.validateParseException(getActivity(), e);
                }
            }
        });
    }

    private void showLoadingAlert() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title("")
                .content(getString(R.string.signup_register_error))
                .positiveText(getString(android.R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        MaterialDialog dialog = builder.build();
        dialog.show();
    }

}
