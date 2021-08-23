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

public class RevertedAdapter extends RecyclerView.Adapter<RevertedAdapter.ProfilingViewHolder>{

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<Task> revertedList;

    private final SharedPrefManager sp;
    private Long selectedActivity;

    public RevertedAdapter(Context mContext, List<Task> revertedList) {
        this.mContext = mContext;
        this.revertedList = revertedList;

        sp = SharedPrefManager.getInstance(mContext);
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_reverted_approval, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        Task reverted = revertedList.get(position);
        holder.mTvRevertedTitle.setText(reverted.activityName);
        holder.mTvRevertedCrpcm.setText("CRPCM : ".concat(String.valueOf(reverted.shgCount)));
        holder.mTvRevertedMbk.setText("MBK : ".concat(String.valueOf(reverted.householdCount)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRevertClickListener.onRevertClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return revertedList.size();
    }


    OnRevertClickListener onRevertClickListener;

    public void setOnRevertClickListener(OnRevertClickListener onRevertClickListener) {
        this.onRevertClickListener = onRevertClickListener;
    }

    public interface OnRevertClickListener{
        void onRevertClick(int position);
    }


    public static  class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvRevertedTitle;
        TextView mTvRevertedCrpcm;
        TextView mTvRevertedMbk;


        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvRevertedTitle = itemView.findViewById(R.id.cell_reverted_title);
            mTvRevertedCrpcm = itemView.findViewById(R.id.cell_reverted_crpcm);
            mTvRevertedMbk = itemView.findViewById(R.id.cell_reverted_mbk);

        }
    }
}
