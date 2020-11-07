package com.jbol.dailydrops.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.jbol.dailydrops.MainActivity;
import com.jbol.dailydrops.models.DropModel;

import java.util.ArrayList;

public class DropClickListener implements View.OnClickListener {
    private RecyclerView recyclerView;
    private ArrayList<DropModel> dropModelArrayList;
    private Context ctx;

    public DropClickListener(Context ctx, RecyclerView recyclerView, ArrayList<DropModel> dropModelArrayList) {
        this.ctx = ctx;
        this.recyclerView = recyclerView;
        this.dropModelArrayList = dropModelArrayList;
}

    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        DropModel drop = dropModelArrayList.get(itemPosition);
        Log.d("dev", drop.getTitle());

        MainActivity.getInstance().showDetails(drop);

        Toast.makeText(ctx, drop.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void setDropModelArrayList(ArrayList<DropModel> dropModelArrayList) {
        this.dropModelArrayList = dropModelArrayList;
    }

}
