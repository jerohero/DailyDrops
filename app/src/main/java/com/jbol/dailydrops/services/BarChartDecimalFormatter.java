package com.jbol.dailydrops.services;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.text.DecimalFormat;

/**
 * Removes decimals that automatically appear on MPAndroid bar charts
 * https://stackoverflow.com/a/44800697/11391965
 */

public class BarChartDecimalFormatter implements ValueFormatter {
    private DecimalFormat format;

    public BarChartDecimalFormatter() {
        format = new DecimalFormat("#");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return format.format(value);
    }
}
