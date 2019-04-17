package com.smakhorin.telegramchartapp.charts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smakhorin.telegramchartapp.R;

import java.util.ArrayList;
import java.util.List;

import static com.smakhorin.telegramchartapp.utils.DensityConverter.dpToPx;


/**
 * LinearLayout holder for our RecyclerView
 */
public class LineChartLayout extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

    private final List<TextView> textViewList = new ArrayList<>();

    private Followers followers;
    private LineChart lineChart;
    private TextView textViewFollowers;
    private RelativeLayout relativeLayout;

    private final int marginSixteenDp = (int) dpToPx(16);

    public LineChartLayout(Context context) {
        super(context);
        init();
    }

    public LineChartLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        initTextViewFollowers();
        initLineChart();
        initRelativeLayout();
        initColors();
    }

    private void initTextViewFollowers() {
        textViewFollowers = new TextView(getContext());
        textViewFollowers.setText(R.string.followers);
        textViewFollowers.setTypeface(Typeface.create(getContext().getString(R.string.TitleFont), Typeface.NORMAL));
        textViewFollowers.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        LayoutParams paramsTextViewFollowers = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        paramsTextViewFollowers.setMargins(marginSixteenDp, marginSixteenDp, 0, 0);
        textViewFollowers.setLayoutParams(paramsTextViewFollowers);
        addView(textViewFollowers);
    }

    /**
     * Initializes linechart
     */
    private void initLineChart() {
        lineChart = new LineChart(getContext());
        int margin = (int) dpToPx(16);
        LayoutParams paramsLineChart = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) dpToPx(340));
        paramsLineChart.setMargins(margin, margin, margin, 0);
        lineChart.setLayoutParams(paramsLineChart);
        addView(lineChart);
    }

    /**
     * Initializes colors and sets a theme based on AppCompatDelegate
     */
    public void setDarkTheme() {
        initColors();
    }

    /**
     * Intitalizes colors for switching themes
     */
    private void initColors() {
        int color;
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            textViewFollowers.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFollowersNight));
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorCardBackgroundNight));
            color = ContextCompat.getColor(getContext(), R.color.colorLineNameNight);
        } else {
            textViewFollowers.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFollowersDay));
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorCardBackgroundDay));
            color = ContextCompat.getColor(getContext(), R.color.colorLineNameDay);
        }
        for (TextView textView : textViewList) {
            textView.setTextColor(color);
        }
        lineChart.setDarkTheme();
    }

    private void initRelativeLayout() {
        relativeLayout = new RelativeLayout(getContext());
        LayoutParams paramsRelativeLayout = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsRelativeLayout.setMargins(marginSixteenDp, marginSixteenDp, 0, marginSixteenDp);
        relativeLayout.setLayoutParams(paramsRelativeLayout);
        addView(relativeLayout);
    }

    /**
     * Sets data to this layout manager
     * @param followers     Followers object
     */
    public void setData(Followers followers) {
        if (this.followers == null || !this.followers.equals(followers)) {
            this.followers = followers;
            initCheckBoxes();
            lineChart.setData(followers);
        }
    }

    /**
     * Initializes check boxes inside a relative layout
     */
    private void initCheckBoxes() {
        for (int i = 0; i < followers.getLines().size(); i++) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setId(i + 1);
            checkBox.setChecked(true);
            checkBox.setOnCheckedChangeListener(this);
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(Color.parseColor(followers.getLines().get(i).getColor())));
            RelativeLayout.LayoutParams layoutParamsCheckBox = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            checkBox.setLayoutParams(layoutParamsCheckBox);
            if (i != 0) {
                layoutParamsCheckBox.addRule(RelativeLayout.BELOW, i);
                layoutParamsCheckBox.setMargins(0, marginSixteenDp, 0, 0);
            }

            TextView textView = new TextView(getContext());
            textView.setText(followers.getLines().get(i).getName());
            RelativeLayout.LayoutParams layoutParamsTextView = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParamsTextView.addRule(RelativeLayout.RIGHT_OF, i + 1);
            layoutParamsTextView.addRule(RelativeLayout.ALIGN_BASELINE, i + 1);
            layoutParamsTextView.setMargins(marginSixteenDp, 0, 0, 0);
            textView.setLayoutParams(layoutParamsTextView);
            textViewList.add(textView);

            relativeLayout.addView(checkBox);
            relativeLayout.addView(textView);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (lineChart.isScrollingWindowTouched() || lineChart.isChartWindowTouched())
            requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) {
            lineChart.removeLine(buttonView.getId());
        } else {
            lineChart.showLine(buttonView.getId());
        }
    }
}