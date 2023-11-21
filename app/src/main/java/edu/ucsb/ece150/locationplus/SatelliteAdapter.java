package edu.ucsb.ece150.locationplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SatelliteAdapter extends RecyclerView.Adapter<SatelliteAdapter.ViewHolder> {

    private List<Satellite> satelliteList;

    public void setSatelliteList(List<Satellite> satelliteList) {
        this.satelliteList = satelliteList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.satellite_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Satellite satellite = satelliteList.get(position);
        holder.satelliteInfoTextView.setText(satellite.toString());
    }

    @Override
    public int getItemCount() {
        return satelliteList != null ? satelliteList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView satelliteInfoTextView;

        public ViewHolder(View view) {
            super(view);
            satelliteInfoTextView = view.findViewById(R.id.satelliteInfoTextView);
        }
    }
}

