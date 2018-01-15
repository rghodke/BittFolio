package crypto.manager.bittfolio.adapter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ghodk on 1/15/2018.
 */

public class HourlyDateAxisFormatter implements IAxisValueFormatter {


    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long millis = (long) value;
        String time = new SimpleDateFormat("HH:MM").format(new Date(millis * 1000L));
        return time;
    }
}
