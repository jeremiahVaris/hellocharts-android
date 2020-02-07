package lecho.lib.hellocharts.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.StaticLayout;

import lecho.lib.hellocharts.computator.ChartComputator;
import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;

/**
 * Abstract renderer implementation, every chart renderer extends this class(although it is not required it helps).
 */
public abstract class AbstractChartRenderer implements ChartRenderer {
    public int DEFAULT_LABEL_MARGIN_DP = 8;
    protected Chart chart;
    protected ChartComputator computator;
    /**
     * Paint for value labels.
     */
    protected Paint labelPaint = new Paint();
    /**
     * Paint for labels background.
     */
    protected Paint labelBackgroundPaint = new Paint();
    /**
     * Holds coordinates for label background rect.
     */
    protected RectF labelBackgroundRect = new RectF();
    /**
     * Font metrics for label paint, used to determine text height.
     */
    protected FontMetricsInt fontMetrics = new FontMetricsInt();
    /**
     * If true maximum and current viewport will be calculated when chart data change or during data animations.
     */
    protected boolean isViewportCalculationEnabled = true;
    protected float density;
    protected float scaledDensity;
    protected SelectedValue selectedValue = new SelectedValue();
    protected char[] labelBuffer = new char[64];
    protected int labelOffset;
    protected int labelMargin;
    protected boolean isValueLabelBackgroundEnabled;
    protected boolean isValueLabelBackgroundAuto;
    private int valueLabelBackgroundRadius;

    public AbstractChartRenderer(Context context, Chart chart) {
        this.density = context.getResources().getDisplayMetrics().density;
        this.scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        this.chart = chart;
        this.computator = chart.getChartComputator();

        labelMargin = ChartUtils.dp2px(density, DEFAULT_LABEL_MARGIN_DP);
        labelOffset = labelMargin;

        labelPaint.setAntiAlias(true);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextAlign(Align.LEFT);
        labelPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        labelPaint.setColor(Color.WHITE);

        labelBackgroundPaint.setAntiAlias(true);
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void resetRenderer() {
        this.computator = chart.getChartComputator();
    }

    @Override
    public void onChartDataChanged() {
        final ChartData data = chart.getChartData();

        Typeface typeface = chart.getChartData().getValueLabelTypeface();
        if (null != typeface) {
            labelPaint.setTypeface(typeface);
        }

        labelPaint.setColor(data.getValueLabelTextColor());
        labelPaint.setTextSize(ChartUtils.sp2px(scaledDensity, data.getValueLabelTextSize()));
        labelPaint.getFontMetricsInt(fontMetrics);

        this.isValueLabelBackgroundEnabled = data.isValueLabelBackgroundEnabled();
        this.isValueLabelBackgroundAuto = data.isValueLabelBackgroundAuto();
        this.labelBackgroundPaint.setColor(data.getValueLabelBackgroundColor());
        this.valueLabelBackgroundRadius = data.getValueLabelBackgroundRadius();

        // Important - clear selection when data changed.
        selectedValue.clear();

    }

    /**
     * Draws label text and label background if isValueLabelBackgroundEnabled is true.
     */
    protected void drawLabelTextAndBackground(Canvas canvas, char[] labelBuffer, int startIndex, int numChars,
                                              int autoBackgroundColor) {
        final float textX;
        final float textY;

        if (isValueLabelBackgroundEnabled) {

            if (isValueLabelBackgroundAuto) {
                labelBackgroundPaint.setColor(autoBackgroundColor);
            }

            canvas.drawRect(labelBackgroundRect, labelBackgroundPaint);

            textX = labelBackgroundRect.left + labelMargin;
            textY = labelBackgroundRect.bottom - labelMargin;
        } else {
            textX = labelBackgroundRect.left;
            textY = labelBackgroundRect.bottom;
        }

        canvas.drawText(labelBuffer, startIndex, numChars, textX, textY, labelPaint);
    }


    public static Path RoundedRect(
            RectF rect, float rx, float ry,
            boolean tl, boolean tr, boolean br, boolean bl
    ){
        float left = rect.left; float top=rect.top; float right = rect.right; float bottom = rect.bottom;

        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        if (tr)
            path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        else{
            path.rLineTo(0, -ry);
            path.rLineTo(-rx,0);
        }
        path.rLineTo(-widthMinusCorners, 0);
        if (tl)
            path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        else{
            path.rLineTo(-rx, 0);
            path.rLineTo(0,ry);
        }
        path.rLineTo(0, heightMinusCorners);

        if (bl)
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
        else{
            path.rLineTo(0, ry);
            path.rLineTo(rx,0);
        }

        path.rLineTo(widthMinusCorners, 0);
        if (br)
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        else{
            path.rLineTo(rx,0);
            path.rLineTo(0, -ry);
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }

    /**
     * Draws multiline label text and label background if isValueLabelBackgroundEnabled is true.
     */
    protected void drawMultilineLabelTextAndBackground(Canvas canvas, StaticLayout staticLayout,
                                                       int autoBackgroundColor) {
        final float textX;
        final float textY;

        if (isValueLabelBackgroundEnabled) {

            if (isValueLabelBackgroundAuto) {
                labelBackgroundPaint.setColor(autoBackgroundColor);
            }

            int radiusInPx = ChartUtils.dp2px(density,valueLabelBackgroundRadius);
            Path path = RoundedRect(labelBackgroundRect, radiusInPx,radiusInPx,
                    true, true, true, true);
            canvas.drawPath(path,labelBackgroundPaint);

            textX = labelBackgroundRect.left + labelMargin;
            textY = labelBackgroundRect.bottom - labelMargin;
        } else {
            textX = labelBackgroundRect.left;
            textY = labelBackgroundRect.bottom;
        }

        canvas.save();
        canvas.translate(textX, labelBackgroundRect.top+labelMargin);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean isTouched() {
        return selectedValue.isSet();
    }

    @Override
    public void clearTouch() {
        selectedValue.clear();
    }

    @Override
    public Viewport getMaximumViewport() {
        return computator.getMaximumViewport();
    }

    @Override
    public void setMaximumViewport(Viewport maxViewport) {
        if (null != maxViewport) {
            computator.setMaxViewport(maxViewport);
        }
    }

    @Override
    public Viewport getCurrentViewport() {
        return computator.getCurrentViewport();
    }

    @Override
    public void setCurrentViewport(Viewport viewport) {
        if (null != viewport) {
            computator.setCurrentViewport(viewport);
        }
    }

    @Override
    public boolean isViewportCalculationEnabled() {
        return isViewportCalculationEnabled;
    }

    @Override
    public void setViewportCalculationEnabled(boolean isEnabled) {
        this.isViewportCalculationEnabled = isEnabled;
    }

    @Override
    public void selectValue(SelectedValue selectedValue) {
        this.selectedValue.set(selectedValue);
    }

    @Override
    public SelectedValue getSelectedValue() {
        return selectedValue;
    }
}


