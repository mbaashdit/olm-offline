package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.Profiling;

import java.util.List;

public class ProfilingAdapter extends RecyclerView.Adapter<ProfilingAdapter.ProfilingViewHolder>{

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<Profiling> profilingList;

    public ProfilingAdapter(Context mContext, List<Profiling> profilingList) {
        this.mContext = mContext;
        this.profilingList = profilingList;
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_profiling_dash, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        Profiling profiling = profilingList.get(position);
        holder.mTvProfilingLbl.setText(profiling.getName());
        holder.mTvProfilingTotal.setText(profiling.getAmount());
        holder.mTvProfilingTotal.setTextColor(Color.parseColor(profiling.getNoColor()));
        holder.mTvProfilingTotalLbl.setText("Total "+profiling.getName());
        char letter = profiling.getName().charAt(0);
        holder.mTvCharacterLbl.setText(String.valueOf(letter));
        holder.mCvProfilingCard.setCardBackgroundColor(Color.parseColor(profiling.getCharColorBg()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                profilingListener.onProfileItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profilingList.size();
    }

    public static  class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvProfilingLbl;
        TextView mTvCharacterLbl;
        TextView mTvProfilingTotal;
        TextView mTvProfilingTotalLbl;
        CardView mCvProfilingCard;
        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);

            mTvProfilingLbl = itemView.findViewById(R.id.cell_tv_name);
            mTvProfilingTotal = itemView.findViewById(R.id.cell_tv_total);
            mTvProfilingTotalLbl = itemView.findViewById(R.id.cell_tv_total_lbl);
            mTvCharacterLbl = itemView.findViewById(R.id.tv_character);
            mCvProfilingCard = itemView.findViewById(R.id.cv_profiling_item_bg);
        }
    }
    ProfilingListener profilingListener;

    public void setProfilingListener(ProfilingListener profilingListener) {
        this.profilingListener = profilingListener;
    }

    public interface ProfilingListener{
        void  onProfileItemClick(int position);
    }
}
