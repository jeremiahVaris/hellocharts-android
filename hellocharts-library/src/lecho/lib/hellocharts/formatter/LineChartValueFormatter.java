package lecho.lib.hellocharts.formatter;


import android.text.Spannable;

import lecho.lib.hellocharts.model.PointValue;

public interface LineChartValueFormatter {

    public Spannable formatChartValue(PointValue value);
}
