package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.Month;

import java.util.List;

public class MonthListAdapter extends RecyclerView.Adapter<MonthListAdapter.ProfilingViewHolder> {

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<Month> monthList;

    private int selectedPosition = -1;

    public MonthListAdapter(Context mContext, List<Month> monthList, int selectedPosition) {
        this.mContext = mContext;
        this.monthList = monthList;
        this.selectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_month_view, parent, false);
        return new ProfilingViewHolder(v);
    }
    public void restMonthSelected(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }
    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        Month month = monthList.get(position);
        holder.mTvMonth.setText(month.getMonthName());
        holder.mTvYear.setText(month.getYearName());


        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                monthListener.onProfileItemClick(position,month.getYearName());
                selectedPosition = position;
                notifyDataSetChanged();
            }
        });

        if (selectedPosition == position) {
            holder.mTvMonth.setTextColor(Color.parseColor("#FFFFFF"));
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_rectangle_month_select));
        } else {
            holder.mTvMonth.setTextColor(Color.parseColor("#000000"));
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.unselected_month));
        }
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }

    public static class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvYear;
        TextView mTvMonth;

        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);

            mTvYear = itemView.findViewById(R.id.cell_year);
            mTvMonth = itemView.findViewById(R.id.cell_month);
        }
    }

    MonthListener monthListener;

    public void setMonthListener(MonthListener monthListener) {
        this.monthListener = monthListener;
    }

    public interface MonthListener {
        void onProfileItemClick(int position, String year);
    }
}
