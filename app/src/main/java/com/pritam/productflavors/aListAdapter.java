package com.pritam.productflavors;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class aListAdapter extends BaseAdapter implements View.OnClickListener {
    private Activity activity;
    private LayoutInflater inflater;
    private List<HashMap<String, Object>> rowItems;
    // private View promptsView;

    public aListAdapter(Activity activity, List<HashMap<String, Object>> rowItems) {
        this.activity = activity;
        this.rowItems = rowItems;
    }

    public List getRowItems() {
        return this.rowItems;
    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int location) {
        return rowItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.listitem, null);

        // getting data for the row
        String details = rowItems.get(position).get("description").toString();
        if( details != null && details .length() > 0) {
            ((TextView) convertView.findViewById(R.id.details_item)).setText(details);
        } else  {
            ((TextView) convertView.findViewById(R.id.details_item)).setVisibility(View.GONE);
        }

        String title = rowItems.get(position).get("name").toString();
        if( title != null && title .length() > 0) {
            ((TextView) convertView.findViewById(R.id.title_item)).setText(title);
        } else  {
            ((TextView) convertView.findViewById(R.id.title_item)).setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {

    }
}