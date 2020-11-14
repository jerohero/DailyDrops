package com.jbol.dailydrops.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.jbol.dailydrops.MainActivity;
import com.jbol.dailydrops.models.GlobalDropModel;
import java.util.ArrayList;

public class DropClickListener implements View.OnClickListener {
    private RecyclerView recyclerView;
    private ArrayList<GlobalDropModel> dropModelArrayList;
    private Context ctx;

    public DropClickListener(Context ctx, RecyclerView recyclerView, ArrayList<GlobalDropModel> dropModelArrayList) {
        this.ctx = ctx;
        this.recyclerView = recyclerView;
        this.dropModelArrayList = dropModelArrayList;
}

    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        GlobalDropModel drop = dropModelArrayList.get(itemPosition);
        Log.d("dev", drop.getTitle());

        MainActivity.getInstance().showDetails(drop);

        Toast.makeText(ctx, drop.getTitle(), Toast.LENGTH_SHORT).show();
    }

}
