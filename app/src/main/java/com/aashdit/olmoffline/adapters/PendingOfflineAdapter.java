package com.aashdit.olmoffline.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.Task;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class PendingOfflineAdapter extends RealmRecyclerViewAdapter<Task,PendingOfflineAdapter.ProfilingViewHolder> /*RecyclerView.Adapter<PendingOfflineAdapter.ProfilingViewHolder>*/ {

    private static final String TAG = "ProfilingAdapter";

    private final Context mContext;
    private final List<Task> profilingList;

    private final SharedPrefManager sp;
    OnOfflinePendingClickListener onPendingClickListener;
    private Long selectedActivity;
    private String userType;

    public PendingOfflineAdapter(Context mContext, OrderedRealmCollection<Task> profilingList, String userType) {
        super(profilingList,true);
        this.mContext = mContext;
        this.profilingList = profilingList;
        this.userType = userType;

        sp = SharedPrefManager.getInstance(mContext);
    }

    @NonNull
    @Override
    public ProfilingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        if (userType.equals(Constant.PMITRA)) {
            v = LayoutInflater.from(mContext).inflate(R.layout.cell_pending_approval, parent, false);
        } else if (userType.equals(Constant.KMITRA)) {
            v = LayoutInflater.from(mContext).inflate(R.layout.cell_pending_approval_km, parent, false);
        } else if (userType.equals(Constant.UMITRA) || userType.equals(Constant.CRPEP)) {
            v = LayoutInflater.from(mContext).inflate(R.layout.cell_pending_approval_um, parent, false);
        }
        return new ProfilingViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ProfilingViewHolder holder, int position) {

        Task pending = profilingList.get(position);
        holder.mTvPendingLabel.setText(pending.schemeName);
        holder.mTvPendingSchemeName.setText(pending.schemeName);
        holder.mTvPendingTitle.setText(pending.activityName);
        holder.mTvPendingMonth.setText(pending.monthCode);
        holder.mTvPendingYear.setText(String.valueOf(pending.year));
        if (userType.equals(Constant.PMITRA)) {
            holder.mTvPendingClf.setText("Cluster : ".concat(String.valueOf(pending.clfCount)));
            holder.mTvPendingClf.setPaintFlags(holder.mTvPendingClf.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.mTvPendingShg.setText("SHG : ".concat(String.valueOf(pending.shgCount)));
            holder.mTvPendingShg.setPaintFlags(holder.mTvPendingShg.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.mTvPendingHh.setText("Member : ".concat(String.valueOf(pending.householdCount)));
            holder.mTvPendingHh.setPaintFlags(holder.mTvPendingHh.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            holder.mTvPendingClf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.clfCount > 0)
                        onPendingClickListener.onOfflineClfClick(position);
                }
            });

            holder.mTvPendingShg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.shgCount > 0)
                        onPendingClickListener.onOfflineShgClick(position);
                }
            });

            holder.mTvPendingHh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.householdCount > 0)
                        onPendingClickListener.onOfflineHhClick(position);
                }
            });
        } else if (userType.equals(Constant.KMITRA)) {
            holder.mTvPendingShg.setText("SHG : ".concat(String.valueOf(pending.shgCount)));
            holder.mTvPendingShg.setPaintFlags(holder.mTvPendingShg.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.mTvPendingHh.setText("Member : ".concat(String.valueOf(pending.householdCount)));
            holder.mTvPendingHh.setPaintFlags(holder.mTvPendingHh.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            holder.mTvPendingShg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.shgCount > 0)
                        onPendingClickListener.onOfflineShgClick(position);
                }
            });

            holder.mTvPendingHh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.householdCount > 0)
                        onPendingClickListener.onOfflineHhClick(position);
                }
            });
        } else if (userType.equals(Constant.UMITRA) || userType.equals(Constant.CRPEP)) {
            if (userType.equals(Constant.CRPEP)) {
                holder.mTvPendingPg.setVisibility(View.GONE);
                holder.mTvPendingShg.setVisibility(View.VISIBLE);
                holder.mTvPendingHh.setVisibility(View.VISIBLE);
                holder.mTvPendingEg.setVisibility(View.VISIBLE);
            } else {
                holder.mTvPendingPg.setVisibility(View.VISIBLE);
                holder.mTvPendingShg.setVisibility(View.GONE);
                holder.mTvPendingHh.setVisibility(View.GONE);
                holder.mTvPendingEg.setVisibility(View.GONE);
            }
            holder.mTvPendingShg.setText("SHG : ".concat(String.valueOf(pending.shgCount)));
            holder.mTvPendingShg.setPaintFlags(holder.mTvPendingShg.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.mTvPendingHh.setText("Member : ".concat(String.valueOf(pending.householdCount)));
            holder.mTvPendingHh.setPaintFlags(holder.mTvPendingHh.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.mTvPendingPg.setText("PG : ".concat(String.valueOf(pending.pgCount)));
            holder.mTvPendingPg.setPaintFlags(holder.mTvPendingPg.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.mTvPendingEg.setText("EG : ".concat(String.valueOf(pending.egCount)));
            holder.mTvPendingEg.setPaintFlags(holder.mTvPendingEg.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            holder.mTvPendingShg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.shgCount > 0)
                        onPendingClickListener.onOfflineShgClick(position);
                }
            });

            holder.mTvPendingHh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.householdCount > 0)
                        onPendingClickListener.onOfflineHhClick(position);
                }
            });
            holder.mTvPendingPg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.pgCount > 0)
                        onPendingClickListener.onOfflinePgClick(position);
                }
            });
            holder.mTvPendingEg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pending.egCount > 0)
                        onPendingClickListener.onOfflineEgClick(position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return profilingList.size();
    }

    public void setOnPendingClickListener(OnOfflinePendingClickListener onPendingClickListener) {
        this.onPendingClickListener = onPendingClickListener;
    }

    public interface OnOfflinePendingClickListener {
        void onOfflineClfClick(int position);

        void onOfflineShgClick(int position);

        void onOfflineHhClick(int position);

        void onOfflinePgClick(int position);

        void onOfflineEgClick(int position);
    }

    public static class ProfilingViewHolder extends RecyclerView.ViewHolder {

        TextView mTvPendingTitle;
        TextView mTvPendingLabel;
        TextView mTvPendingSchemeName;
        TextView mTvPendingClf;
        TextView mTvPendingShg;
        TextView mTvPendingHh;
        TextView mTvPendingPg;
        TextView mTvPendingEg;
        TextView mTvPendingMonth;
        TextView mTvPendingYear;


        public ProfilingViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvPendingTitle = itemView.findViewById(R.id.cell_pending_title);
            mTvPendingLabel = itemView.findViewById(R.id.cell_pending_lbl);
            mTvPendingSchemeName = itemView.findViewById(R.id.cell_pending_scheme_name);
            mTvPendingClf = itemView.findViewById(R.id.cell_pending_clf);
            mTvPendingShg = itemView.findViewById(R.id.cell_pending_shg);
            mTvPendingHh = itemView.findViewById(R.id.cell_pending_hh);
            mTvPendingPg = itemView.findViewById(R.id.cell_pending_pg);
            mTvPendingEg = itemView.findViewById(R.id.cell_pending_eg);
            mTvPendingMonth = itemView.findViewById(R.id.cell_pending_month);
            mTvPendingYear = itemView.findViewById(R.id.cell_pending_year);

        }
    }
}
