package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.SeedType;

import java.util.List;

/**
 * Created by Manabendu on 26/03/21
 */
public class SeedTypeAdapter extends RecyclerView.Adapter<SeedTypeAdapter.FundSourceVHViewHolder> {

    private static final String TAG = SeedTypeAdapter.class.getSimpleName();

    private Context mContext;
    private List<SeedType> fundSources;

    private boolean isEabled ;


    public SeedTypeAdapter(Context context, List<SeedType> data) {
        this.mContext = context;
        this.fundSources = data;
    }

    public void setEnable(boolean enable) {
        isEabled = enable;
    }


    @NonNull
    @Override
    public FundSourceVHViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cell_seed_type, viewGroup, false);

        return new FundSourceVHViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FundSourceVHViewHolder holder, int position) {
        // include binding logic here
        SeedType fundSource = fundSources.get(position);
        holder.mTvFundSource.setText(fundSource.valueEn);
//        holder.mCheckbox.setEnabled(isEabled);
        holder.mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                seedTypeSelectListener.onSeedTypeSelect(position, fundSource.valueId, b);
            }
        });
    }


    @Override
    public int getItemCount() {
        return fundSources.size();
    }


    public static class FundSourceVHViewHolder extends RecyclerView.ViewHolder {

        TextView mTvFundSource;
        CheckBox mCheckbox;
        public FundSourceVHViewHolder(View itemView) {
            super(itemView);

            mTvFundSource = itemView.findViewById(R.id.cell_tv_seed_type);
            mCheckbox = itemView.findViewById(R.id.cell_checkbox);
        }
    }

    SeedTypeSelectListener seedTypeSelectListener;

    public void setSeedTypeSelectListener(SeedTypeSelectListener irrigationSelectListener) {
        this.seedTypeSelectListener = irrigationSelectListener;
    }

    public interface SeedTypeSelectListener{
        void onSeedTypeSelect(int pos, Long selectedId, boolean isSelect);
    }
}