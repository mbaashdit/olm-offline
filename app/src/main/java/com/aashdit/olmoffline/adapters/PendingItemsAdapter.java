package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.TaskListItem;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PendingItemsAdapter extends RecyclerView.Adapter<PendingItemsAdapter.ProfilingViewHolder> {

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<TaskListItem> approvedList;

    private final SharedPrefManager sp;
    private final Long type;
    PendingDataListener pendingDataListener;
    private Long selectedActivity;
    private String userType;

    public PendingItemsAdapter(Context mContext, List<TaskListItem> profilingList, Long type, String userType) {
        this.mContext = mContext;
        this.approvedList = profilingList;
        this.type = type;
        this.userType = userType;

        sp = SharedPrefManager.getInstance(mContext);
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cell_pending_items, parent, false);
        return new ProfilingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        TaskListItem approved = approvedList.get(position);
        holder.mTvPendingTitle.setText(approved.entityName);
        holder.mTvPendingAddress.setText(approved.entityLine1);
        holder.mTvPendingMon.setText(approved.monthName);
        holder.mTvPendingYear.setText(String.valueOf(approved.year));
        if (userType.equals("ROLE_KMITRA") ) {
//            holder.mTvPendingGoat.setVisibility(View.VISIBLE);
//            holder.mTvPendingBuck.setVisibility(View.VISIBLE);
            JSONObject o = null;
            try {
                o = new JSONObject(approved.properties);
                String goats = o.optString("QTY Sold");
                String qtyProduced = o.optString("QTY Produced");

//                holder.mTvPendingGoat.setText("QTY Sold :".concat(" ").concat(goats));
//                holder.mTvPendingBuck.setText("QTY Produced :".concat(" ").concat(qtyProduced));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else if(userType.equals("ROLE_UMITRA") || userType.equals("ROLE_CRPEP")){
//            holder.mTvPendingGoat.setVisibility(View.VISIBLE);
//            holder.mTvPendingBuck.setVisibility(View.GONE);
            JSONObject o = null;
            try {
                o = new JSONObject(approved.properties);
                String goats = o.optString("Total Profit");

//                holder.mTvPendingGoat.setText("Profit :".concat(" ").concat(String.format("%.2f", Double.parseDouble(goats))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (type.equals(1L)) {
                try {
                    JSONObject o = new JSONObject(approved.properties);
                    String goats = o.optString("Goats");
                    String bucks = o.optString("Kids");

//                    holder.mTvPendingGoat.setText(mContext.getResources().getString(R.string.goats).concat(" ").concat(goats));
//                    holder.mTvPendingBuck.setText(mContext.getResources().getString(R.string.bucks).concat(" ").concat(bucks));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (type.equals(2L)) {
                try {
                    JSONObject o = new JSONObject(approved.properties);
                    String goats = o.optString("Birds");
//                String bucks = o.optString("bucks");

//                    holder.mTvPendingGoat.setText(mContext.getResources().getString(R.string.birds).concat(" ").concat(goats));
//                    holder.mTvPendingBuck.setVisibility(View.GONE);//setText(mContext.getResources().getString(R.string.bucks).concat(" ").concat(bucks));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (type.equals(3L)) {
                try {
                    JSONObject o = new JSONObject(approved.properties);
                    String fingerlings = o.optString("Fingerlings Harvested");
                    String table_size = o.optString("Table Size Harvested");
//                String bucks = o.optString("bucks");

//                    holder.mTvPendingGoat.setText(mContext.getResources().getString(R.string.fingerlings).concat("\n").concat(fingerlings));
//                    holder.mTvPendingBuck.setText(mContext.getResources().getString(R.string.table_size_fish).concat("\n").concat(table_size));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (type.equals(7L)) {
                try {
                    JSONObject o = new JSONObject(approved.properties);
                    String fingerlings = o.optString("Cows / Buffaloes");
                    String table_size = o.optString("Calves");
//                String bucks = o.optString("bucks");

//                    holder.mTvPendingGoat.setText(mContext.getResources().getString(R.string.cows).concat(" ").concat(fingerlings));
//                    holder.mTvPendingBuck.setText(mContext.getResources().getString(R.string.calves).concat(" ").concat(table_size));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
//        holder.mTvPendingBmu.setText(approved.bmu);
//        holder.mTvPendingMbk.setText(approved.mbk);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pendingDataListener.onPendingClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return approvedList.size();
    }

    public void setPendingDataListener(PendingDataListener pendingDataListener) {
        this.pendingDataListener = pendingDataListener;
    }

    public interface PendingDataListener {
        void onPendingClick(int position);
    }


    public static class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvPendingTitle;
        TextView mTvPendingAddress;
//        TextView mTvPendingBuck;
//        TextView mTvPendingGoat;
        TextView mTvPendingMon;
        TextView mTvPendingYear;


        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvPendingTitle = itemView.findViewById(R.id.cell_pending_title);
            mTvPendingAddress = itemView.findViewById(R.id.cell_pending_address);
//            mTvPendingGoat = itemView.findViewById(R.id.cell_pending_goat);
//            mTvPendingBuck = itemView.findViewById(R.id.cell_pending_buck);
            mTvPendingMon = itemView.findViewById(R.id.cell_pending_month);
            mTvPendingYear = itemView.findViewById(R.id.cell_pending_year);

        }
    }
}
