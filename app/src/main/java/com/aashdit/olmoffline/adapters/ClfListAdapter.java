package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.Entries;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import java.util.List;

public class ClfListAdapter extends RecyclerView.Adapter<ClfListAdapter.ProfilingViewHolder>{

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<Entries> profilingList;

    private final SharedPrefManager sp;
    private Long selectedActivity;

    public ClfListAdapter(Context mContext, List<Entries> profilingList) {
        this.mContext = mContext;
        this.profilingList = profilingList;

        sp = SharedPrefManager.getInstance(mContext);
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_shg_activity, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        this.selectedActivity = sp.getLongData("ACTIVITY_ID");

        Entries shg = profilingList.get(position);
        holder.mTvShgName.setText(shg.getOrgName());
        holder.mTvActAmount.setText(mContext.getResources().getString(R.string.Rs).concat(" ").concat(shg.getBankBalance()));
        holder.mTvBucks.setText(String.valueOf(shg.getBucks()));
        holder.mTvGoats.setText(String.valueOf(shg.getGoats()));
        holder.mTvBirds.setText(String.valueOf(shg.getBirds()));
        holder.mCvShgActBg.setCardBackgroundColor(Color.parseColor(shg.getColorCode()));
        holder.mTvPercentage.setText(shg.getGrowthValue());
        if (selectedActivity.equals(1L)){
            holder.mLlGoatryRoot.setVisibility(View.VISIBLE);
            holder.mLlFishRoot.setVisibility(View.GONE);
            holder.mLlPoultryRoot.setVisibility(View.GONE);
            holder.mLlDairyRoot.setVisibility(View.GONE);
        }else if (selectedActivity.equals(2L)){
            holder.mLlGoatryRoot.setVisibility(View.GONE);
            holder.mLlFishRoot.setVisibility(View.GONE);
            holder.mLlPoultryRoot.setVisibility(View.VISIBLE);
            holder.mLlDairyRoot.setVisibility(View.GONE);
        } else if (selectedActivity.equals(3L)) {
            holder.mLlGoatryRoot.setVisibility(View.GONE);
            holder.mLlFishRoot.setVisibility(View.VISIBLE);
            holder.mLlPoultryRoot.setVisibility(View.GONE);
            holder.mLlDairyRoot.setVisibility(View.GONE);
            holder.mTvActAmount.setText(mContext.getResources().getString(R.string.Rs) + " " + shg.getBankBalance());
            holder.mTvFingerlings.setText(shg.getFingerLings() + "");
            holder.mTvFish.setText(shg.getFish() + "");
        } else if (selectedActivity.equals(7L)) {
            holder.mLlDairyRoot.setVisibility(View.VISIBLE);
            holder.mLlGoatryRoot.setVisibility(View.GONE);
            holder.mLlFishRoot.setVisibility(View.GONE);
            holder.mLlPoultryRoot.setVisibility(View.GONE);
            holder.mTvActAmount.setText(mContext.getResources().getString(R.string.Rs) + " " + shg.getBankBalance());
            holder.mTvFingerlings.setText(shg.getFingerLings() + "");
            holder.mTvFish.setText(shg.getFish() + "");
            holder.mTvBuffaloes.setText(shg.getCalf()+"");
            holder.mTvCows.setText(shg.getCows()+"");
        }
//        else {
//            holder.mLlGoatryRoot.setVisibility(View.GONE);
//            holder.mLlPoultryRoot.setVisibility(View.GONE);
//        }
        if (shg.getArrowType().equals("U")){
            holder.mIvArrow.setImageDrawable(mContext.getResources().getDrawable(R.drawable.up_arrow));
        }else{
            holder.mIvArrow.setImageDrawable(mContext.getResources().getDrawable(R.drawable.down_arrow));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCLFDetailsListener.onActivityCLFClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return profilingList.size();
    }


    ActivityCLFDetailsListener activityCLFDetailsListener;

    public void setActivityClfDetailsListener(ActivityCLFDetailsListener activityDetailsListener) {
        this.activityCLFDetailsListener = activityDetailsListener;
    }

    public  interface  ActivityCLFDetailsListener{
        void onActivityCLFClick(int position);
    }

    public static  class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvShgName;
        TextView mTvActAmount;
        TextView mTvGoats;
        TextView mTvBucks;
        TextView mTvBirds;
        TextView mTvPercentage;
        TextView mTvFingerlings;
        TextView mTvFish;
        TextView mTvCows;
        TextView mTvBuffaloes;
        CardView mCvShgActBg;
        ImageView mIvArrow;

        LinearLayout mLlGoatryRoot,mLlPoultryRoot,mLlFishRoot,mLlDairyRoot;


        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvShgName = itemView.findViewById(R.id.cell_tv_activity_name);
            mTvActAmount = itemView.findViewById(R.id.cell_tv_amount);
            mTvGoats = itemView.findViewById(R.id.cell_tv_goat_no);
            mTvBucks = itemView.findViewById(R.id.cell_tv_buck_no);
//            mTvBirds = itemView.findViewById(R.id.cell_tv_bird_no);
//            mTvFingerlings = itemView.findViewById(R.id.cell_tv_fingerLings);
//            mTvFish = itemView.findViewById(R.id.cell_tv_fish);
//            mTvCows = itemView.findViewById(R.id.cell_tv_cows);
//            mTvBuffaloes = itemView.findViewById(R.id.cell_tv_buffalo);
            mCvShgActBg = itemView.findViewById(R.id.cv_letter);
            mTvPercentage = itemView.findViewById(R.id.tv_percent_letter);
            mIvArrow = itemView.findViewById(R.id.cell_iv_arrow);

//            mLlGoatryRoot = itemView.findViewById(R.id.ll_goatry);
//            mLlPoultryRoot = itemView.findViewById(R.id.ll_birds);
//            mLlFishRoot = itemView.findViewById(R.id.ll_fishery);
//            mLlDairyRoot = itemView.findViewById(R.id.ll_dairy);

        }
    }
}
