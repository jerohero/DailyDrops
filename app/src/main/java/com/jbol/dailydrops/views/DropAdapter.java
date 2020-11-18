package com.jbol.dailydrops.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jbol.dailydrops.R;
import com.jbol.dailydrops.models.GlobalDropModel;
import java.util.ArrayList;

public class DropAdapter extends RecyclerView.Adapter<DropHolder> {

    private Context context;

    private ArrayList<GlobalDropModel> drops;
    private DropClickListener dropClickListener;

    public DropAdapter(Context context, ArrayList<GlobalDropModel> drops, DropClickListener dropClickListener) {
        this.context = context;
        this.drops = drops;
        this.dropClickListener = dropClickListener;
    }

    @NonNull @Override
    public DropHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.drop_card, parent, false);
        view.setOnClickListener(dropClickListener);

        return new DropHolder(view);
    }


    @Override
    public void onBindViewHolder(DropHolder holder, int position) {
        GlobalDropModel drop = this.drops.get(position);
        holder.setDetails(drop);
    }

    @Override
    public int getItemCount() {
        return drops.size();
    }
}