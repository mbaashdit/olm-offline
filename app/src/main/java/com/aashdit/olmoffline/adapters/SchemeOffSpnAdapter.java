package com.aashdit.olmoffline.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.db.PMSchemesOffline;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import java.util.List;

public class SchemeOffSpnAdapter extends BaseAdapter {

    private final Activity activity;
    private final List<PMSchemesOffline> resultArrayList;
//    private final List<Schemes> resultArrayList;
    private final LayoutInflater inflater;

    private final SharedPrefManager sp;

    public SchemeOffSpnAdapter(Activity activity, List<PMSchemesOffline> resultArrayList) {
        this.activity = activity;
        this.resultArrayList = resultArrayList;

        sp = SharedPrefManager.getInstance(activity);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return resultArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spn_scheme, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PMSchemesOffline currentItem = (PMSchemesOffline) getItem(position);
        viewHolder.bindData(currentItem);


        return convertView;

    }

    private class ViewHolder {
        TextView itemName;

        public ViewHolder(View view) {
            itemName = view.findViewById(R.id.text1);
        }

        public void bindData(PMSchemesOffline currentItem) {
            itemName.setText(currentItem.schemeNameEn);
        }
    }
}
