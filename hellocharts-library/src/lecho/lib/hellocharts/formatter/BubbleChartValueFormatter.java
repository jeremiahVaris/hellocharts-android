package lecho.lib.hellocharts.formatter;

import android.text.Spannable;

import lecho.lib.hellocharts.model.BubbleValue;

public interface BubbleChartValueFormatter {

    public Spannable formatChartValue(Spannable formattedValue, BubbleValue value);
}
