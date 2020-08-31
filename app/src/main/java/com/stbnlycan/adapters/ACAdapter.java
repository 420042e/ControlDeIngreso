package com.stbnlycan.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

public class ACAdapter extends ArrayAdapter<Visitante> {
    private Context context;
    private int resourceId;
    private List<Visitante> items, tempItems, suggestions;
    public ACAdapter(@NonNull Context context, int resourceId, ArrayList<Visitante> items) {
        super(context, resourceId, items);
        this.items = items;
        this.context = context;
        this.resourceId = resourceId;
        tempItems = new ArrayList<>(items);
        suggestions = new ArrayList<>();
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(resourceId, parent, false);
            }
            Visitante visitante = getItem(position);
            TextView name = (TextView) view.findViewById(R.id.textView);
            LinearLayout fila = (LinearLayout) view.findViewById(R.id.fila);
            name.setText(visitante.getVteNombre()+" "+visitante.getVteApellidos());

            /*if(position % 2 == 0)
                fila.setBackgroundColor(0xFFF0F0F0);
            else
                fila.setBackgroundColor(0xFFE8E8E8);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
    @Nullable
    @Override
    public Visitante getItem(int position) {
        return items.get(position);
    }
    @Override
    public int getCount() {
        return items.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return visitanteFilter;
    }
    private Filter visitanteFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Visitante visitante = (Visitante) resultValue;
            return visitante.getVteCi();
        }
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence != null) {
                suggestions.clear();
                for (Visitante visitante: tempItems) {
                    if (visitante.getVteCi().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        suggestions.add(visitante);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ArrayList<Visitante> tempValues = (ArrayList<Visitante>) filterResults.values;
            if (filterResults != null && filterResults.count > 0) {
                clear();
                for (Visitante visitanteObj : tempValues) {
                    add(visitanteObj);
                }
                notifyDataSetChanged();
            } else {
                clear();
                notifyDataSetChanged();
            }
        }
    };
}