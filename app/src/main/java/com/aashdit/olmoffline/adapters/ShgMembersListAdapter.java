package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.ShgMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShgMembersListAdapter extends RecyclerView.Adapter<ShgMembersListAdapter.ProfilingViewHolder>{

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<ShgMember> profilingList;
    List<String> colors;
    public ShgMembersListAdapter(Context mContext, List<ShgMember> profilingList) {
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
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_shg_member_list, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {
        ShgMember shg = profilingList.get(position);
        holder.mTvShgName.setText(shg.memberName);
        holder.mTvShgId.setText(shg.memberId+"");


        Random r = new Random();
        int i1 = r.nextInt(11 - 0) + 0;

        GradientDrawable draw = new GradientDrawable();
        draw.setShape(GradientDrawable.RECTANGLE);
        draw.setCornerRadius(8.0f);
        draw.setColor(Color.parseColor(colors.get(i1)));

        holder.mTvCharacterLbl.setBackground(draw);
        holder.mTvCharacterLbl.setText(shg.memberName.charAt(0)+"");

        holder.mRlCOntainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shgMemberDetailsListener.showShgMemberDetails(position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shgMemberDetailsListener.showShgMemberDetails(position);
            }
        });
//        holder.mTvCharacterLbl.setBackgroundTintList(mContext.getResources().getColorStateList(R.color.purple_500));

        holder.mTvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shgMemberDetailsListener.deleteShgMember(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return profilingList.size();
    }

    public static  class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvShgName;
        TextView mTvShgId;
        TextView mTvCharacterLbl;
        TextView mTvDelete;

        RelativeLayout mRlCOntainer;

        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvShgName = itemView.findViewById(R.id.cell_tv_shg_name);
            mTvShgId = itemView.findViewById(R.id.cell_tv_shg_id);
            mTvCharacterLbl = itemView.findViewById(R.id.cell_tv_lbl);

            mRlCOntainer = itemView.findViewById(R.id.rl_cell_container);
            mTvDelete = itemView.findViewById(R.id.cell_tv_delete);
        }
    }


    ShgMemberDetailsListener shgMemberDetailsListener;

    public void setShgMemberDetailsListener(ShgMemberDetailsListener shgMemberDetailsListener) {
        this.shgMemberDetailsListener = shgMemberDetailsListener;
    }

    public  interface  ShgMemberDetailsListener{
        void showShgMemberDetails(int position);
        void deleteShgMember(int position);
    }
}
