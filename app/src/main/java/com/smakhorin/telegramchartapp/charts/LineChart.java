package com.smakhorin.telegramchartapp.charts;


import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.smakhorin.telegramchartapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.smakhorin.telegramchartapp.utils.DensityConverter.dpToPx;


public class LineChart extends View {

    private Followers followers;
    private final List<Path> linePathsFull = new ArrayList<>();
    private final List<Path> linePathsDetailed = new ArrayList<>();
    private List<Line> lines;
    private final List<Integer> maxValues = new ArrayList<>();
    private final List<Integer> removedLines = new ArrayList<>();
    private final List<String> monthAndDate = new ArrayList<>();
    private final List<String> days = new ArrayList<>();
    private final List<Date> dates = new ArrayList<>();

    private float heightDetailedChartPx;
    private float heightFullChartPx;
    private float marginSixteenDp;
    private float widthDividedByTwo;
    private float heightDividedBySixtyFour;
    private float heightDividedBySix;

    private float windowLeftBorder;
    private float windowRightBorder;
    private float windowTopBorder;
    private float windowBottomBorder;

    private boolean isMaxValueUpdated = false;
    private boolean isWindowTouched = false;
    private boolean isLeftBorderTouched = false;
    private boolean isRightBorderTouched = false;
    private boolean isChartWindowTouched = false;
    private boolean cachedGrid = false;
    private boolean drawDetails = false;

    private int circleIndex = 0;
    private int maxValue;
    private int countX = 24;
    private final int defaultCountX = 24;
    private final float strokeWidthVertical = dpToPx(6);
    private final float strokeWidthHorizontal = dpToPx(2);
    private float stepXFullChart;
    private float startYPosValueText;
    private float stepYPosGrid;
    private float stepYValue;

    private float eventX;

    private TextPaint paintText;
    private TextPaint paintColorfulText;

    private Paint paintWindowSelector;
    private Paint paintLine;
    private Paint paintGridLine;
    private Paint paintCircle;
    private Paint paintRectangle;
    private Paint paintRoundRect;
    private Paint paintRoundRectBorder;

    private Path pathGridLine;
    private Path pathWindowHorizontal;
    private Path pathWindowVertical;

    private ValueAnimator animator;
    private float stepYDetailed;
    private float stepYFull;
    private int alpha;

    private int toChange = -1; // to prevent redrawing disabled charts on multiple graphs

    private float dx;

    private int startIndex;
    private int endIndex;

    public LineChart(Context context) {
        super(context);
        init();
    }

    public LineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        heightDetailedChartPx = dpToPx(256);
        heightFullChartPx = dpToPx(38);
        marginSixteenDp = dpToPx(16);


        animator = new ValueAnimator();
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        //animator.setInterpolator(new LinearInterpolator());

        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setAntiAlias(true);
        paintLine.setStrokeJoin(Paint.Join.ROUND);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        paintLine.setStrokeWidth(dpToPx(2));
        paintLine.setStyle(Paint.Style.STROKE);

        paintGridLine = new Paint();
        paintGridLine.setColor(Color.parseColor("#E0E0E0"));
        paintGridLine.setStrokeWidth(2);
        paintGridLine.setStyle(Paint.Style.STROKE);

        paintText = new TextPaint();
        paintText.setColor(Color.parseColor("#96A2AA"));
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextSize(dpToPx(12));

        paintColorfulText = new TextPaint();
        paintColorfulText.setColor(Color.parseColor("#00FF00"));
        paintColorfulText.setStyle(Paint.Style.FILL);
        paintColorfulText.setTextSize(dpToPx(12));

        paintWindowSelector = new Paint();
        paintWindowSelector.setColor(Color.parseColor("#DBE7F0"));
        paintWindowSelector.setStrokeWidth(dpToPx(2));
        paintWindowSelector.setAlpha(210);
        paintWindowSelector.setStyle(Paint.Style.STROKE);

        paintRectangle = new Paint();
        paintRectangle.setColor(Color.parseColor("#E4EEF5"));
        paintRectangle.setAlpha(125);

        paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(Color.parseColor("#FFFFFE"));

        paintRoundRect = new Paint();
        paintRoundRect.setStyle(Paint.Style.FILL);
        paintRoundRect.setColor(Color.parseColor("#FFFFFF"));

        paintRoundRectBorder = new Paint();
        paintRoundRectBorder.setStyle(Paint.Style.STROKE);
        paintRoundRectBorder.setColor(Color.parseColor("#000000"));

        pathGridLine = new Path();
        pathWindowHorizontal = new Path();
        pathWindowVertical = new Path();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stepXFullChart = w / (float) (followers.getListOfX().size() - 1);
        windowRightBorder = w;
        windowLeftBorder = w - countX * stepXFullChart;
        windowTopBorder = h - heightFullChartPx;
        windowBottomBorder = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!lines.isEmpty()) {
            drawGridWithYValues(canvas);
            drawXValues(canvas);
            drawDetailedLineChart(canvas);
            drawFullChart(canvas);
            drawRectangle(canvas);
            drawWindowSelector(canvas);
        }
    }

    private void drawGridWithYValues(Canvas canvas) {
        if (!cachedGrid) {
            //margin between values
            float marginSixDp = dpToPx(6);
            //Start at 16dp
            float startYPosGridLine = marginSixteenDp;
            //16dp - 6dp
            startYPosValueText = marginSixteenDp - marginSixDp;
            //Calculate step by dividing full chart to 5 pieces
            stepYPosGrid = heightDetailedChartPx / 5f;
            //Step value
            stepYValue = (float)(maxValue / 5);

            for (int i = 0; i < 6; i++) {
                float yPosGridLine = startYPosGridLine + (stepYPosGrid * i);
                pathGridLine.moveTo(0, yPosGridLine);
                pathGridLine.lineTo(getWidth(), yPosGridLine);
                float yPosValue = startYPosValueText + (stepYPosGrid * i);
                canvas.drawText(String.valueOf((int)(stepYValue * (5 - i))), 0, yPosValue, paintText);
            }
            cachedGrid = true;
        } else {
            for(int i = 0; i < 6;i ++) {
                float yPosValue = startYPosValueText + (stepYPosGrid * i);
                canvas.drawText(String.valueOf((int)(stepYValue * (5 - i))), 0, yPosValue, paintText);
            }
        }
            canvas.drawPath(pathGridLine, paintGridLine);
    }

    private void drawXValues(Canvas canvas) {
        float startYPos = heightDetailedChartPx + marginSixteenDp * 2;
        float startXPos = 0;
        float stepXPos = getWidth() / 5f - dpToPx(7);
        int stepValue = countX / 6;
        canvas.drawText(monthAndDate.get(startIndex), 0, startYPos, paintText);
        for (int i = 1; i < 6; i++) {
            // -1 to prevent OutOfBounds exception
            canvas.drawText(monthAndDate.get((startIndex + stepValue*(i+1))-1), startXPos + stepXPos * i, startYPos, paintText);
        }
        //canvas.drawText(monthAndDate.get((startIndex + stepValue*6)-1), startXPos + stepXPos * 5, startYPos, paintText);
    }

    /**
     * Draws the bigger portion of the graph which is resizeable
     *
     * @param canvas    canvas to be drawn on
     */
    private void drawDetailedLineChart(Canvas canvas) {
        paintLine.setStrokeWidth(dpToPx(2));

        float stepYPos = heightDetailedChartPx / maxValue;
        if (isMaxValueUpdated) {
            stepYPos = (float) animator.getAnimatedValue("detailed");
        }
        float stepXPosDetailed = getWidth() / (float) (countX - 1);

        int size = lines.size();
        //draw vertical stroke (only once)
        if(drawDetails) {
            canvas.drawLine(circleIndex * stepXPosDetailed, 0, circleIndex * stepXPosDetailed, heightDetailedChartPx, paintText);
        }
        for (int i = 0; i < countX; i++) {
            for (int j = 0; j < size; j++) {
                int index = (startIndex + i);
                if(index > lines.get(j).getListOfY().size()-1) {
                    index = lines.get(j).getListOfY().size()-1;
                }
                float y = lines.get(j).getListOfY().get(index) * stepYPos;
                Path path = linePathsDetailed.get(j);
                if (i == 0)
                    path.moveTo(0, heightDetailedChartPx + marginSixteenDp - y);
                else {
                    path.lineTo(i * stepXPosDetailed, heightDetailedChartPx + marginSixteenDp - y);
                    if (i == countX - 1) {
                        paintLine.setColor(Color.parseColor(lines.get(j).getColor()));
                        if (removedLines.contains(j)) {
                            paintLine.setAlpha(alpha);
                            if(j != toChange) {
                                paintLine.setAlpha(0);
                            }
                        } else {
                            paintLine.setAlpha(255);
                            if(j == toChange) {
                                paintLine.setAlpha(alpha);
                            }
                        }
                        canvas.drawPath(path, paintLine);
                        if(drawDetails) {
                            widthDividedByTwo = (float)(getWidth()/2);
                            heightDividedBySixtyFour = (float)(getHeight()/64);
                            heightDividedBySix = (float)(getHeight()/6);
                            //OutOfBounds Fix (-1 and 112)
                            int clampedIndex =  MathUtils.clamp(startIndex+ circleIndex,0,lines.get(j).getListOfY().size()-2);
                            //fix to prevent circle drawing when chart is gone
                            if(!removedLines.contains(j)) {
                                //draw a colored circle
                                canvas.drawCircle(circleIndex * stepXPosDetailed, heightDetailedChartPx + marginSixteenDp - lines.get(j).getListOfY().get(clampedIndex) * stepYPos, dpToPx(8), paintLine);
                                //then draw a filled circle defined by paintCircle paint cass
                                canvas.drawCircle(circleIndex * stepXPosDetailed, heightDetailedChartPx + marginSixteenDp - lines.get(j).getListOfY().get(clampedIndex) * stepYPos, dpToPx(7), paintCircle);
                            }
                            //draw roundrect
                            canvas.drawRoundRect(widthDividedByTwo-dpToPx(32f*size),heightDividedBySixtyFour,widthDividedByTwo+dpToPx(32f*size), heightDividedBySixtyFour + heightDividedBySix,dpToPx(4f),dpToPx(4f),paintRoundRect);
                            //draw borders
                            canvas.drawRoundRect(widthDividedByTwo-dpToPx(32f*size)-1,heightDividedBySixtyFour,widthDividedByTwo+dpToPx(32f*size), heightDividedBySixtyFour + heightDividedBySix,dpToPx(4f),dpToPx(4f), paintRoundRectBorder);
                            canvas.drawRoundRect(widthDividedByTwo-dpToPx(32f*size),heightDividedBySixtyFour,widthDividedByTwo+dpToPx(32f*size)+1, heightDividedBySixtyFour + heightDividedBySix,dpToPx(4f),dpToPx(4f), paintRoundRectBorder);
                            canvas.drawRoundRect(widthDividedByTwo-dpToPx(32f*size),heightDividedBySixtyFour-1,widthDividedByTwo+dpToPx(32f*size), heightDividedBySixtyFour + heightDividedBySix,dpToPx(4f),dpToPx(4f), paintRoundRectBorder);
                            canvas.drawRoundRect(widthDividedByTwo-dpToPx(32f*size),heightDividedBySixtyFour,widthDividedByTwo+dpToPx(32f*size), heightDividedBySixtyFour + heightDividedBySix+1,dpToPx(4f),dpToPx(4f), paintRoundRectBorder);
                            //draw text inside roundrect
                            canvas.drawText((days.get(clampedIndex))+" "+(monthAndDate.get(clampedIndex)),widthDividedByTwo-dpToPx(32f*size)+dpToPx(8f),heightDividedBySixtyFour+dpToPx(12f),paintText);
                            for(int k = 0; k < size;k++) {
                                if(!removedLines.contains(k)) {
                                    paintColorfulText.setColor(Color.parseColor(lines.get(k).getColor()));
                                    canvas.drawText(lines.get(k).getListOfY().get(clampedIndex).toString(), (widthDividedByTwo - dpToPx(32f * size - k * 64)) + dpToPx(8f), heightDividedBySixtyFour + dpToPx(32f), paintColorfulText);
                                    canvas.drawText(followers.getLines().get(k).getName(), (widthDividedByTwo - dpToPx(32f * size - k * 64)) + dpToPx(8f), heightDividedBySixtyFour + dpToPx(48f), paintColorfulText);
                                }
                            }
                        }
                        path.reset();
                    }
                }
            }
        }
    }

    /**
     * Draws a small rectangle which a resizeable window sits on
     *
     * @param canvas    canvas to be drawn on
     */
    private void drawRectangle(Canvas canvas) {
        canvas.drawRect(0, heightDetailedChartPx + marginSixteenDp * 3, windowLeftBorder, heightDetailedChartPx + marginSixteenDp * 3 + heightFullChartPx, paintRectangle);
        canvas.drawRect(windowRightBorder, heightDetailedChartPx + marginSixteenDp * 3, getWidth(), heightDetailedChartPx + marginSixteenDp * 3 + heightFullChartPx, paintRectangle);
    }

    /**
     * Draws a full chart on a smaller rectangle
     *
     * @param canvas    canvas to be drawn on
     */
    private void drawFullChart(Canvas canvas) {
        paintLine.setStrokeWidth(dpToPx(1));

        float stepY = (heightFullChartPx - dpToPx(4)) / maxValue;
        if (isMaxValueUpdated) {
            stepY = (float) animator.getAnimatedValue("full");
        }

        for (int i = 0; i < followers.getListOfX().size(); i++) {
            for (int j = 0; j < lines.size(); j++) {
                float y = (lines.get(j).getListOfY().get(i) * stepY);

                Path path = linePathsFull.get(j);
                if (i == 0)
                    path.moveTo(0, heightDetailedChartPx + marginSixteenDp * 3 + heightFullChartPx - y - dpToPx(2));
                else {
                    path.lineTo(i * stepXFullChart, heightDetailedChartPx + marginSixteenDp * 3 + heightFullChartPx - y - dpToPx(2));
                    if (i == followers.getListOfX().size() - 1) {
                        paintLine.setColor(Color.parseColor(lines.get(j).getColor()));
                        if (removedLines.contains(j)) {
                            paintLine.setAlpha(alpha);
                            //basically to prevent drawing invisible lines (implying there are more than two)
                            //we double check j-index against toChange which was assigned during a TextBox click
                            if (j != toChange) {
                                paintLine.setAlpha(0);
                            }
                        } else {
                            paintLine.setAlpha(alpha);
                            if (j != toChange) {
                                paintLine.setAlpha(255);
                            }
                        }
                        canvas.drawPath(path, paintLine);
                        path.reset();
                    }
                }
            }
        }
    }

    /**
     * Draws a resizeable rectangle for sizing
     *
     * @param canvas    canvas to be drawn on
     */
    private void drawWindowSelector(Canvas canvas) {
        pathWindowHorizontal.moveTo(windowLeftBorder+strokeWidthVertical, windowTopBorder + strokeWidthHorizontal / 2);
        pathWindowHorizontal.lineTo(windowRightBorder-strokeWidthVertical, windowTopBorder + strokeWidthHorizontal / 2);
        pathWindowHorizontal.moveTo(windowLeftBorder+strokeWidthVertical, windowBottomBorder - strokeWidthHorizontal / 2);
        pathWindowHorizontal.lineTo(windowRightBorder-strokeWidthVertical, windowBottomBorder - strokeWidthHorizontal / 2);

        pathWindowVertical.moveTo(windowLeftBorder + strokeWidthVertical / 2, windowTopBorder);
        pathWindowVertical.lineTo(windowLeftBorder + strokeWidthVertical / 2, windowBottomBorder);
        pathWindowVertical.moveTo(windowRightBorder - strokeWidthVertical / 2, windowTopBorder);
        pathWindowVertical.lineTo(windowRightBorder - strokeWidthVertical / 2, windowBottomBorder);

        paintWindowSelector.setStrokeWidth(strokeWidthHorizontal);
        canvas.drawPath(pathWindowHorizontal, paintWindowSelector);
        pathWindowHorizontal.reset();

        paintWindowSelector.setStrokeWidth(strokeWidthVertical);
        canvas.drawPath(pathWindowVertical, paintWindowSelector);
        pathWindowVertical.reset();
    }

    /**
     * Sets data from a collection
     *
     * @param followers     a collection of JSON objects
     */
    public void setData(Followers followers) {
        this.followers = followers;
        lines = followers.getLines();
        startIndex = followers.getListOfX().size() - defaultCountX;
        initMaxValues();
        initChartPaths();
        initDates();
        initStepY();
    }

    /**
     * Initializes dates converting them to "Nov 8" from Unix timestamp
     * also separates "Mon/Tue/Sat" date format in another list
     */
    private void initDates() {
        DateFormat parseFormat = new SimpleDateFormat("MMM d", Locale.ENGLISH);
        DateFormat dayFormat = new SimpleDateFormat("E",Locale.ENGLISH);
        List<Long> datesList = followers.getListOfX();
        for (Long date : datesList) {
            dates.add(new Date(date));
        }
        for (Date d : dates) {
            monthAndDate.add(parseFormat.format(d));
            days.add(dayFormat.format(d).substring(0,3));
        }
    }

    /**
     * Initializing max values for a future shift if the peak was changed
     */
    private void initMaxValues() {
        for (Line line : lines) {
            maxValues.add(Collections.max(line.getListOfY()));
        }
        maxValue = Collections.max(maxValues) / 10 * 10 + 10;

    }

    /**
     * Initializing a list of fullPaths based on lines
     */
    private void initChartPaths() {
        for (int i = 0; i < lines.size(); i++) {
            linePathsFull.add(new Path());
            linePathsDetailed.add(new Path());
        }
    }

    /**
     * Initializing a y-coord step
     */
    private void initStepY() {
        stepYDetailed = heightDetailedChartPx / maxValue;
        stepYFull = heightFullChartPx / maxValue;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                //Ensure that touch is either in the smaller resizeable scroll or inside our chart window because they're the same View
                if (y < getHeight() && y > getHeight() - heightFullChartPx) {
                    isLeftBorderTouched = x >= windowLeftBorder && x <= windowLeftBorder + strokeWidthVertical;
                    isRightBorderTouched = x <= windowRightBorder && x >= windowRightBorder - strokeWidthVertical;
                    isWindowTouched = x >= windowLeftBorder + strokeWidthVertical && x <= windowRightBorder - strokeWidthVertical;
                }
                else {
                    isChartWindowTouched = x > dpToPx(4f) && x <= (getWidth()-dpToPx(4f));
                    circleIndex = (int)(x / (getWidth() / countX));
                    drawDetails = true;
                }
                eventX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                //Calculate delta for moving things
                dx = event.getX() - eventX;
                if (isWindowTouched) {
                    if (windowLeftBorder + dx > 0 && windowRightBorder + dx < getWidth()) {
                        windowLeftBorder += dx;
                        windowRightBorder += dx;
                    }
                } else if (isLeftBorderTouched) {

                    if (windowRightBorder - (windowLeftBorder + dx) < defaultCountX * stepXFullChart) {
                        windowLeftBorder = windowRightBorder - defaultCountX * stepXFullChart;
                    } else if (windowLeftBorder + dx > 0) {
                        windowLeftBorder += dx;
                    }
                } else if (isRightBorderTouched) {
                    if ((windowRightBorder + dx) - windowLeftBorder < defaultCountX * stepXFullChart) {
                        windowRightBorder = windowLeftBorder + defaultCountX * stepXFullChart;
                    } else if (windowRightBorder + dx < getWidth()) {
                        windowRightBorder += dx;
                    }
                } else if(isChartWindowTouched){
                    //Basically we divide our X-coord (which can from 0 to Width) and divide it by a factor (Width / number of px charts(our step to determine a point on a graph))
                    circleIndex = (int)(event.getX() / (getWidth() / countX));
                    drawDetails = true;

                }
                startIndex = (int) (windowLeftBorder / stepXFullChart);
                endIndex = (int) (windowRightBorder / stepXFullChart);
                countX = endIndex - startIndex;
                eventX = event.getX();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isWindowTouched = false;
                isLeftBorderTouched = false;
                isRightBorderTouched = false;
                isChartWindowTouched = false;
                if(drawDetails) {
                    drawDetails = false;
                    invalidate();
                }
                break;
        }
        return true;
    }

    public boolean isScrollingWindowTouched() {
        return isWindowTouched || isLeftBorderTouched || isRightBorderTouched;
    }

    public boolean isChartWindowTouched() {
        return isChartWindowTouched;
    }

    /**
     * Removes a line and passes control to a Value Animator for smooth animation
     * also adds it to a List of removed lines
     *
     * @param index     index of the line
     */
    public void removeLine(int index) {
        removedLines.add(index - 1);
        toChange = index-1;
        createValueAnimator(false);
    }

    /**
     * Adds a line and passes control to a Value Animator for smooth animation
     * also removes it from a List of removed lines
     *
     * @param index     index of the line which is casted to an object
     */
    public void showLine(int index) {
        removedLines.remove(Integer.valueOf(index-1));
        toChange = index-1;
        createValueAnimator(true);
    }

    /**
     * Sets a dark theme
     */
    public void setDarkTheme() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            paintGridLine.setColor(ContextCompat.getColor(getContext(), R.color.colorGridNight));
            paintText.setColor(ContextCompat.getColor(getContext(), R.color.colorValuesNight));
            paintRectangle.setColor(ContextCompat.getColor(getContext(), R.color.colorRectangleNight));
            paintWindowSelector.setColor(ContextCompat.getColor(getContext(), R.color.colorWindowNight));
            paintCircle.setColor(ContextCompat.getColor(getContext(), R.color.colorCircleNight));
            paintRoundRect.setColor(ContextCompat.getColor(getContext(), R.color.colorBackgroundNight));
        } else {
            paintGridLine.setColor(ContextCompat.getColor(getContext(), R.color.colorGridDay));
            paintText.setColor(ContextCompat.getColor(getContext(), R.color.colorValuesDay));
            paintRectangle.setColor(ContextCompat.getColor(getContext(), R.color.colorRectangleDay));
            paintWindowSelector.setColor(ContextCompat.getColor(getContext(), R.color.colorWindowDay));
            paintCircle.setColor(ContextCompat.getColor(getContext(), R.color.colorCircleDay));
            paintRoundRect.setColor(ContextCompat.getColor(getContext(), R.color.colorBackgroundDay));
        }
        invalidate();
    }

    /**
     * Updates a max y-coord value
     */
    private void updateMaxValue() {
        int max = 0;
        for (int i = 0; i < maxValues.size(); i++) {
            if (!removedLines.contains(i) && maxValues.get(i) > max) {
                max = maxValues.get(i);
            }
        }
        if (max / 10 * 10 + 10 != maxValue) {
            isMaxValueUpdated = true;
            maxValue = max / 10 * 10 + 10;
        }
        cachedGrid = false;
    }

    /**
     * Creates a value animator to handle line appearance/disappearance
     *
     * @param isShow    determines if it's a fade from 0 to 255 or vice-versa
     */
    private void createValueAnimator(boolean isShow) {
        updateMaxValue();

        float newStepYDetailed = heightDetailedChartPx / maxValue;
        float newStepYFull = heightFullChartPx / maxValue;

        List<PropertyValuesHolder> holder = new ArrayList<>();
        holder.add(PropertyValuesHolder.ofFloat("detailed", stepYDetailed, newStepYDetailed));
        holder.add(PropertyValuesHolder.ofFloat("full", stepYFull, newStepYFull));
        if (isShow) {
            holder.add(PropertyValuesHolder.ofInt("alpha", 0, 255));
        }
        else {
            holder.add(PropertyValuesHolder.ofInt("alpha", 255, 0));
        }

        animator.setValues(holder.toArray(new PropertyValuesHolder[0]));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                stepYDetailed = (float) valueAnimator.getAnimatedValue("detailed");
                stepYFull = (float) valueAnimator.getAnimatedValue("full");
                alpha = (int) valueAnimator.getAnimatedValue("alpha");
                invalidate();
            }
        });
        animator.start();
    }


}

