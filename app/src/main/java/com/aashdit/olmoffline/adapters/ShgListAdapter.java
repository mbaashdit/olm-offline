package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.SHG;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShgListAdapter extends RecyclerView.Adapter<ShgListAdapter.ProfilingViewHolder>{

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<SHG> profilingList;
    List<String> colors;
    public ShgListAdapter(Context mContext, List<SHG> profilingList) {
        this.mContext = mContext;
        this.profilingList = profilingList;

        colors = new ArrayList<>();

        colors.add("#5E97F6");
        colors.add("#9CCC65");
        colors.add("#FF8A65");
        colors.add("#9E9E9E");
        colors.add("#9FA8DA");
        colors.add("#90A4AE");
        colors.add("#AED581");
        colors.add("#F6BF26");
        colors.add("#FFA726");
        colors.add("#4DD0E1");
        colors.add("#BA68C8");
        colors.add("#A1887F");
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_shg_list, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {
        SHG shg = profilingList.get(position);
        holder.mTvShgName.setText(shg.getShgName());
        holder.mTvShgId.setText(shg.getShgCode());


        Random r = new Random();
        int i1 = r.nextInt(11 - 0) + 0;

        GradientDrawable draw = new GradientDrawable();
        draw.setShape(GradientDrawable.RECTANGLE);
        draw.setCornerRadius(8.0f);
        draw.setColor(Color.parseColor(colors.get(i1)));

        holder.mTvCharacterLbl.setBackground(draw);
        holder.mTvCharacterLbl.setText(shg.getShgName().charAt(0)+"");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shgDetailsListener.showShgDetails(position);
            }
        });
//        holder.mTvCharacterLbl.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.purple_500));
    }

    @Override
    public int getItemCount() {
        return profilingList.size();
    }

    public static  class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvShgName;
        TextView mTvShgId;
        TextView mTvCharacterLbl;
        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvShgName = itemView.findViewById(R.id.cell_tv_shg_name);
            mTvShgId = itemView.findViewById(R.id.cell_tv_shg_id);
            mTvCharacterLbl = itemView.findViewById(R.id.cell_tv_lbl);
        }
    }


    ShgDetailsListener shgDetailsListener;

    public void setShgDetailsListener(ShgDetailsListener shgDetailsListener) {
        this.shgDetailsListener = shgDetailsListener;
    }

    public  interface  ShgDetailsListener{
        void showShgDetails(int position);
    }
}
