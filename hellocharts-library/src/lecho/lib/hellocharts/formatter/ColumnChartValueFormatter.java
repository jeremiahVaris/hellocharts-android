package lecho.lib.hellocharts.formatter;

import android.text.Spannable;

import lecho.lib.hellocharts.model.SubcolumnValue;

public interface ColumnChartValueFormatter {

    public Spannable formatChartValue(SubcolumnValue value);

}
