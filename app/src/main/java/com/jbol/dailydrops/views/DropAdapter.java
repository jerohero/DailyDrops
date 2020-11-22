package com.jbol.dailydrops.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jbol.dailydrops.R;
import com.jbol.dailydrops.models.GlobalDropModel;
import java.util.ArrayList;

public class DropAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<GlobalDropModel> drops;
    private DropClickListener dropClickListener;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public DropAdapter(Context context, ArrayList<GlobalDropModel> drops, DropClickListener dropClickListener) {
        this.context = context;
        this.drops = drops;
        this.dropClickListener = dropClickListener;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.drop_card, parent, false);
            view.setOnClickListener(dropClickListener);
            return new DropHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof DropHolder) {
                DropHolder dropHolder = (DropHolder) holder;
                GlobalDropModel drop = this.drops.get(position);
                dropHolder.setDetails(drop);
            } else if (holder instanceof LoadingHolder) {
                showLoadingView((LoadingHolder) holder, position);
            }

    }

    @Override
    public int getItemCount() {
        return drops == null ? 0 : drops.size();
    }

    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return drops.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    private class LoadingHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }

//    private void populateItemRows(DropHolder viewHolder, int position) {
//
//        GlobalDropModel item = drops.get(position);
//        viewHolder.tvItem.setText(item);
//
//    }

}
