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
import com.aashdit.olmoffline.models.FundSource;

import java.util.List;

/**
 * Created by Manabendu on 26/03/21
 */
public class
FundSourceAdapter extends RecyclerView.Adapter<FundSourceAdapter.FundSourceVHViewHolder> {

    private static final String TAG = FundSourceAdapter.class.getSimpleName();

    private Context mContext;
    private List<FundSource> fundSources;


    public FundSourceAdapter(Context context, List<FundSource> data) {
        this.mContext = context;
        this.fundSources = data;
    }


    @NonNull
    @Override
    public FundSourceVHViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cell_fund_source, viewGroup, false);

        return new FundSourceVHViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FundSourceVHViewHolder holder, int position) {
        // include binding logic here
        FundSource fundSource = fundSources.get(position);
        holder.mTvFundSource.setText(fundSource.valueEn);
        holder.mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                fundSourceSelectListener.onFundSourceSelect(position,fundSource.valueId,b);
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

            mTvFundSource = itemView.findViewById(R.id.cell_tv_fund_source_name);
            mCheckbox = itemView.findViewById(R.id.cell_checkbox);
        }
    }

    FundSourceSelectListener fundSourceSelectListener;

    public void setFundSourceSelectListener(FundSourceSelectListener fundSourceSelectListener) {
        this.fundSourceSelectListener = fundSourceSelectListener;
    }

    public interface FundSourceSelectListener{
        void onFundSourceSelect(int pos, Long selectedId, boolean isSelect);
    }
}