package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.Entries;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import java.util.List;

public class UdyogShgActivityAdapter extends RecyclerView.Adapter<UdyogShgActivityAdapter.ProfilingViewHolder> {

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<Entries> profilingList;

    private final SharedPrefManager sp;
    private Long selectedActivity;

    public UdyogShgActivityAdapter(Context mContext, List<Entries> profilingList) {
        this.mContext = mContext;
        this.profilingList = profilingList;

        sp = SharedPrefManager.getInstance(mContext);

    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_nf_activity, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        this.selectedActivity = sp.getLongData("ACTIVITY_ID");

        Entries shg = profilingList.get(position);
        holder.mTvShgName.setText(shg.getOrgName());

        holder.mCvShgActBg.setCardBackgroundColor(Color.parseColor(shg.getColorCode()));
        holder.mTvPercentage.setText(shg.getGrowthValue());
        holder.mTvActAmount.setText(shg.profit);

        if (shg.getArrowType().equals("U")) {
            holder.mIvArrow.setImageDrawable(mContext.getResources().getDrawable(R.drawable.up_arrow));
        } else {
            holder.mIvArrow.setImageDrawable(mContext.getResources().getDrawable(R.drawable.down_arrow));
        }

        holder.mCvActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityDetailsListener.onUdyogActivityClick(position);
            }
        });

        holder.mIvCellAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                activityDetailsListener.onUdyogEditDetails(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return profilingList.size();
    }


    UdyogActivityDetailsListener activityDetailsListener;

    public void setActivityDetailsListener(UdyogActivityDetailsListener activityDetailsListener) {
        this.activityDetailsListener = activityDetailsListener;
    }

    public  interface  UdyogActivityDetailsListener{
        void onUdyogActivityClick(int position);
        void onUdyogEditDetails(int position);
    }

    public static class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvShgName;
        TextView mTvActAmount;
        TextView mTvPercentage;
        CardView mCvShgActBg;
        CardView mCvActivity;
        ImageView mIvArrow,mIvCellAction;

//        LinearLayout mLlGoatryRoot, mLlPoultryRoot,mLlFishRoot,mLlDairyRoot;

        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvShgName = itemView.findViewById(R.id.cell_tv_activity_name);
            mTvActAmount = itemView.findViewById(R.id.cell_tv_amount);
            mCvShgActBg = itemView.findViewById(R.id.cv_letter);
            mCvActivity = itemView.findViewById(R.id.cv_activity);
            mTvPercentage = itemView.findViewById(R.id.tv_percent_letter);
            mIvArrow = itemView.findViewById(R.id.cell_iv_arrow);
            mIvCellAction = itemView.findViewById(R.id.cell_iv_action);

        }
    }
}
