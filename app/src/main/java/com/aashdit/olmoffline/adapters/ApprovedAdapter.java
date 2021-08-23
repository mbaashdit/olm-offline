package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.Task;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import java.util.List;

public class ApprovedAdapter extends RecyclerView.Adapter<ApprovedAdapter.ProfilingViewHolder>{

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<Task> approvedList;

    private final SharedPrefManager sp;
    private Long selectedActivity;

    public ApprovedAdapter(Context mContext, List<Task> profilingList) {
        this.mContext = mContext;
        this.approvedList = profilingList;

        sp = SharedPrefManager.getInstance(mContext);
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_approved_approval, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        Task approved = approvedList.get(position);
        holder.mTvApprovedTitle.setText(approved.activityName);
        holder.mTvApprovedBmu.setText("BMU : ".concat(String.valueOf(approved.shgCount)));
        holder.mTvApprovedMbk.setText("MBK : ".concat(String.valueOf(approved.householdCount)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onApprovedClickListener.onApprovedClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return approvedList.size();
    }

    OnApprovedClickListener onApprovedClickListener;

    public void setOnApprovedClickListener(OnApprovedClickListener onApprovedClickListener) {
        this.onApprovedClickListener = onApprovedClickListener;
    }

    public interface OnApprovedClickListener{
        void onApprovedClick(int position);
    }


    public static  class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvApprovedTitle;
        TextView mTvApprovedMbk;
        TextView mTvApprovedBmu;


        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvApprovedTitle = itemView.findViewById(R.id.cell_approved_title);
            mTvApprovedMbk = itemView.findViewById(R.id.cell_approved_mbk);
            mTvApprovedBmu = itemView.findViewById(R.id.cell_approved_bmu);

        }
    }
}
