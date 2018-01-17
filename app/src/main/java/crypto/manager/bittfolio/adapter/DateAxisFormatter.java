package crypto.manager.bittfolio.adapter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ghodk on 1/15/2018.
 */

public class DateAxisFormatter implements IAxisValueFormatter {

    private int interval;

    public DateAxisFormatter(int i) {
        this.interval = i;
    }

    public void setInterval(int i) {
        this.interval = i;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long millis = (long) value;
        String time;
        switch (interval) {
            case 0:
                time = new SimpleDateFormat("HH:MM").format(new Date(millis * 1000L));
                break;
            case 1:
                time = new SimpleDateFormat("HH:MM").format(new Date(millis * 1000L));
                break;
            case 2:
                time = new SimpleDateFormat("HH:MM").format(new Date(millis * 1000L));
                break;
            case 3:
                time = new SimpleDateFormat("MM/dd").format(new Date(millis * 1000L));
                break;
            case 4:
                time = new SimpleDateFormat("MM/dd").format(new Date(millis * 1000L));
                break;
            case 5:
                time = new SimpleDateFormat("MM/dd").format(new Date(millis * 1000L));
                break;
            case 6:
                time = new SimpleDateFormat("MM/yyyy").format(new Date(millis * 1000L));
                break;
            case 7:
                time = new SimpleDateFormat("MM/yyyy").format(new Date(millis * 1000L));
                ;
                break;
            default:
                time = new SimpleDateFormat("HH:MM").format(new Date(millis * 1000L));
                break;
        }
        return time;
    }
}
