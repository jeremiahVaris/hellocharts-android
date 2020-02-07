package lecho.lib.hellocharts.formatter;

import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import lecho.lib.hellocharts.util.FloatUtils;

public class ValueFormatterHelper {
    public static final int DEFAULT_DIGITS_NUMBER = 0;
    private static final String TAG = "ValueFormatterHelper";
    private int decimalDigitsNumber = Integer.MIN_VALUE;
    private char[] appendedText = new char[0];
    private char[] prependedText = new char[0];
    private char decimalSeparator = '.';

    public void determineDecimalSeparator() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        if (numberFormat instanceof DecimalFormat) {
            decimalSeparator = ((DecimalFormat) numberFormat).getDecimalFormatSymbols().getDecimalSeparator();
        }
    }

    public int getDecimalDigitsNumber() {
        return decimalDigitsNumber;
    }

    public ValueFormatterHelper setDecimalDigitsNumber(int decimalDigitsNumber) {
        this.decimalDigitsNumber = decimalDigitsNumber;
        return this;
    }

    public char[] getAppendedText() {
        return appendedText;
    }

    public ValueFormatterHelper setAppendedText(char[] appendedText) {
        if (null != appendedText) {
            this.appendedText = appendedText;
        }
        return this;
    }

    public char[] getPrependedText() {
        return prependedText;
    }

    public ValueFormatterHelper setPrependedText(char[] prependedText) {
        if (null != prependedText) {
            this.prependedText = prependedText;
        }
        return this;
    }

    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    public ValueFormatterHelper setDecimalSeparator(char decimalSeparator) {
        char nullChar = '\0';
        if (nullChar != decimalSeparator) {
            this.decimalSeparator = decimalSeparator;
        }
        return this;
    }

    /**
     * Formats float value. Result is stored in (output) formattedValue array. Method
     * returns number of chars of formatted value. The formatted value starts at index [formattedValue.length -
     * charsNumber] and ends at index [formattedValue.length-1].
     * Note: If label is not null it will be used as formattedValue instead of float value.
     * Note: Parameter defaultDigitsNumber is used only if you didn't change decimalDigitsNumber value using
     * method {@link #setDecimalDigitsNumber(int)}.
     */
    public Spannable formatFloatValueWithPrependedAndAppendedText(float value, int
            defaultDigitsNumber, Spannable label) {
        if (null != label) {
            // If custom label is not null use only name characters as formatted value.
            // Copy label into formatted value array.
            int labelLength = label.length();
            if (labelLength > 64) {
                Log.w(TAG, "Label length is larger than buffer size (64chars), some chars will be skipped!");
                labelLength = 64;
                label = new SpannableString(label.toString().substring(0,64));

            }
            return label;
        }

        final int appliedDigitsNumber = getAppliedDecimalDigitsNumber(defaultDigitsNumber);
        String labelString = formatFloatValue(value, appliedDigitsNumber);
        int charsNumber = labelString.length();
        labelString = appendText(labelString);
        labelString = prependText(labelString, charsNumber);
        return new SpannableString(labelString);
    }

    /**
     * @see #formatFloatValueWithPrependedAndAppendedText(float, int, Spannable)
     */
    public Spannable formatFloatValueWithPrependedAndAppendedText(float value, Spannable label) {
        return formatFloatValueWithPrependedAndAppendedText( value, DEFAULT_DIGITS_NUMBER, label);
    }

    /**
     * @see #formatFloatValueWithPrependedAndAppendedText( float, int, Spannable)
     */
    public Spannable formatFloatValueWithPrependedAndAppendedText(float value, int
            defaultDigitsNumber) {
        return formatFloatValueWithPrependedAndAppendedText( value, defaultDigitsNumber, null);
    }

    public int formatFloatValue(char[] formattedValue, float value, int decimalDigitsNumber) {
        return FloatUtils.formatFloat(formattedValue, value, formattedValue.length - appendedText.length,
                decimalDigitsNumber,
                decimalSeparator);
    }

    public String formatFloatValue(float value, int decimalDigitsNumber) {
        return String.format(
                "%."+decimalDigitsNumber+"f",
                        Double.parseDouble(String.valueOf(value))
        );
    }

    public void appendText(char[] formattedValue) {
        if (appendedText.length > 0) {
            System.arraycopy(appendedText, 0, formattedValue, formattedValue.length - appendedText.length,
                    appendedText.length);
        }
    }

    public String appendText(String formattedValue) {
        if (appendedText.length > 0) {
            if (appendedText.length+formattedValue.length()>64){
                char[] resultCharArray = formattedValue.toCharArray();
                System.arraycopy(appendedText, 0, resultCharArray, resultCharArray.length - appendedText.length,
                        appendedText.length);
                return new String(resultCharArray);
            }
                else return formattedValue+new String(appendedText);
        } else return formattedValue;
    }

    public String prependText(String formattedValue, int charsNumber) {
        if (prependedText.length > 0) {
            if (prependedText.length+formattedValue.length()+appendedText.length>64){
                char[] resultCharArray = formattedValue.toCharArray();
                System.arraycopy(prependedText, 0, resultCharArray, resultCharArray.length - charsNumber - appendedText.length
                        - prependedText.length, prependedText.length);
                return new String(resultCharArray);
            }
            else return new String(prependedText) + formattedValue;
        } else return formattedValue;
    }

    public void prependText(char[] formattedValue, int charsNumber) {
        if (prependedText.length > 0) {
            System.arraycopy(prependedText, 0, formattedValue, formattedValue.length - charsNumber - appendedText.length
                    - prependedText.length, prependedText.length);
        }
    }

    public int getAppliedDecimalDigitsNumber(int defaultDigitsNumber) {
        final int appliedDecimalDigitsNumber;
        if (decimalDigitsNumber < 0) {
            //When decimalDigitsNumber < 0 that means that user didn't set that value and defaultDigitsNumber should
            // be used.
            appliedDecimalDigitsNumber = defaultDigitsNumber;
        } else {
            appliedDecimalDigitsNumber = decimalDigitsNumber;
        }
        return appliedDecimalDigitsNumber;
    }

}
