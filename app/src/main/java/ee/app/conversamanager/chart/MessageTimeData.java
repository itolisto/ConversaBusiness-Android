package ee.app.conversamanager.chart;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by edgargomez on 10/10/16.
 */

public class MessageTimeData {

    private Timeline timeline;
    private MessageChart messageChart;
    private List<Integer> sentMessages;
    private List<Integer> receivedMessages;
    private List<String> dates;
    private LineDataSet sentComp1;
    private LineDataSet receivedComp2;

    public MessageTimeData(Timeline timeline, MessageChart messageChart) {
        this.timeline = timeline;
        this.messageChart = messageChart;

        if (timeline == Timeline.DAILY || timeline == Timeline.ALL) {
            if (messageChart == MessageChart.SENT) {
                sentMessages = new ArrayList<>(1);
            } else if (messageChart == MessageChart.RECEIVE) {
                receivedMessages = new ArrayList<>(1);
            } else {
                sentMessages = new ArrayList<>(1);
                receivedMessages = new ArrayList<>(1);
            }
        } else if (timeline == Timeline.WEEKLY) {
            if (messageChart == MessageChart.SENT) {
                sentMessages = new ArrayList<>(7);
            } else if (messageChart == MessageChart.RECEIVE) {
                receivedMessages = new ArrayList<>(7);
            } else {
                sentMessages = new ArrayList<>(7);
                receivedMessages = new ArrayList<>(7);
            }
        } else {
            if (messageChart == MessageChart.SENT) {
                sentMessages = new ArrayList<>(31);
            } else if (messageChart == MessageChart.RECEIVE) {
                receivedMessages = new ArrayList<>(31);
            } else {
                sentMessages = new ArrayList<>(31);
                receivedMessages = new ArrayList<>(31);
            }
        }

        dates = getCalendarDates(timeline);
    }


    public List<ILineDataSet> getDataSets() {
        List<ILineDataSet> dataSets = new ArrayList<>(2);

        if (messageChart == MessageChart.SENT) {
//            sentComp1 = new LineDataSet(valsComp1, "Sent");
            sentComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSets.add(sentComp1);
        } else if (messageChart == MessageChart.RECEIVE) {
//            receivedComp2 = new LineDataSet(valsComp1, "Received");
            receivedComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSets.add(receivedComp2);
        } else {
//            sentComp1 = new LineDataSet(valsComp1, "Sent");
//            receivedComp2 = new LineDataSet(valsComp1, "Received");
            sentComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            receivedComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSets.add(sentComp1);
            dataSets.add(receivedComp2);
        }

        return  dataSets;
    }

    private List<String> getCalendarDates(Timeline timeline) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

        if (timeline == Timeline.DAILY || timeline == Timeline.ALL) {
            String[] days = new String[1];

            for (int i = 0; i < 7; i++) {
                days[i] = format.format(now.getTime());
                now.add(Calendar.DAY_OF_MONTH, 1);
            }

            return Arrays.asList(days);
        } else if (timeline == Timeline.WEEKLY) {
            String[] days = new String[1];
            int day = now.get(Calendar.DAY_OF_WEEK);
            now.add(Calendar.DAY_OF_MONTH, -day + 1);

            for (int i = 1; i <= day; i++) {
                days[i] = format.format(now.getTime());
                now.add(Calendar.DAY_OF_MONTH, 1);
            }

            return Arrays.asList(days);
        } else {
            String[] days = new String[1];
            int day = now.get(Calendar.DAY_OF_MONTH);
            now.add(Calendar.DAY_OF_MONTH, -day);

            for (int i = 0; i < day; i++) {
                days[i] = format.format(now.getTime());
                now.add(Calendar.DAY_OF_MONTH, 1);
            }

            return Arrays.asList(days);
        }
    }
}
