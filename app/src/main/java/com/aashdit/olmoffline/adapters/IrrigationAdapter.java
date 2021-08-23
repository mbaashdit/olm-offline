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
import com.aashdit.olmoffline.models.IrrigationSource;

import java.util.List;

/**
 * Created by Manabendu on 26/03/21
 */
public class IrrigationAdapter extends RecyclerView.Adapter<IrrigationAdapter.FundSourceVHViewHolder> {

    private static final String TAG = IrrigationAdapter.class.getSimpleName();

    private Context mContext;
    private List<IrrigationSource> fundSources;


    public IrrigationAdapter(Context context, List<IrrigationSource> data) {
        this.mContext = context;
        this.fundSources = data;
    }


    @NonNull
    @Override
    public FundSourceVHViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cell_irrigation, viewGroup, false);

        return new FundSourceVHViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FundSourceVHViewHolder holder, int position) {
        // include binding logic here
        IrrigationSource fundSource = fundSources.get(position);
        holder.mTvFundSource.setText(fundSource.valueEn);
        holder.mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                irrigationSelectListener.onIrrigationSelect(position,fundSource.valueId,b);
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
    IrrigationSelectListener irrigationSelectListener;

    public void setIrrigationSelectListener(IrrigationSelectListener irrigationSelectListener) {
        this.irrigationSelectListener = irrigationSelectListener;
    }

    public interface IrrigationSelectListener{
        void onIrrigationSelect(int pos, Long selectedId, boolean isSelect);
    }
}