package com.smakhorin.telegramchartapp.charts;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smakhorin.telegramchartapp.R;

import java.util.List;

/**
 * RecyclerView for containing charts
 */
public class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.ViewHolder> {

    private List<Followers> followersList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!followersList.isEmpty()) {
            holder.lineChartLayout.setData(followersList.get(position));
            holder.lineChartLayout.setDarkTheme();
        }
    }

    @Override
    public int getItemCount() {
        if (followersList.isEmpty())
            return 0;
        else
            return followersList.size();
    }


    public void setData(List<Followers> followersList) {
        this.followersList = followersList;
        notifyDataSetChanged();
    }

    public void changeTheme() {
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LineChartLayout lineChartLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            lineChartLayout = itemView.findViewById(R.id.followers_chart);
        }
    }
}
