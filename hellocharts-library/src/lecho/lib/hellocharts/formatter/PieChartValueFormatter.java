package lecho.lib.hellocharts.formatter;

import android.text.Spannable;

import lecho.lib.hellocharts.model.SliceValue;

public interface PieChartValueFormatter {

    public Spannable formatChartValue(SliceValue value);
}
