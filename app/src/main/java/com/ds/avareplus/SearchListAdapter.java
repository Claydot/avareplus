package com.ds.avareplus;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by clayd on 7/27/2017.
 */


public class SearchListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;

    public SearchListAdapter(Context context, ArrayList<String> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.search_list_item, parent, false);


        String val = values.get(position);

        String[] split = val.split("::", 2);
        String code = split[0];
        String remainder = split[1];
        String[] info = remainder.split(";",3);
        String type = info[0];
        String additionalType = info[1];
        String name = info[2];

        TextView codeView = (TextView) rowView.findViewById(R.id.code);
        codeView.setText(code);
        TextView typeView = (TextView) rowView.findViewById(R.id.type);
        typeView.setText(type);
        TextView addTypeView = (TextView) rowView.findViewById(R.id.add_type);
        addTypeView.setText(additionalType);
        TextView nameView = (TextView) rowView.findViewById(R.id.name);
        nameView.setText(name);

        return rowView;
    }
}

