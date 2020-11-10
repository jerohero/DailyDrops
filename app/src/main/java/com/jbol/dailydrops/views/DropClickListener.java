package com.jbol.dailydrops.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.jbol.dailydrops.MainActivity;
import com.jbol.dailydrops.models.SQLiteDropModel;
import java.util.ArrayList;

public class DropClickListener implements View.OnClickListener {
    private RecyclerView recyclerView;
    private ArrayList<SQLiteDropModel> SQLiteDropModelArrayList;
    private Context ctx;

    public DropClickListener(Context ctx, RecyclerView recyclerView, ArrayList<SQLiteDropModel> SQLiteDropModelArrayList) {
        this.ctx = ctx;
        this.recyclerView = recyclerView;
        this.SQLiteDropModelArrayList = SQLiteDropModelArrayList;
}

    @Override
    public void onClick(View v) {
        int itemPosition = recyclerView.getChildLayoutPosition(v);
        SQLiteDropModel drop = SQLiteDropModelArrayList.get(itemPosition);
        Log.d("dev", drop.getTitle());

        MainActivity.getInstance().showDetails(drop);

        Toast.makeText(ctx, drop.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void setSQLiteDropModelArrayList(ArrayList<SQLiteDropModel> SQLiteDropModelArrayList) {
        this.SQLiteDropModelArrayList = SQLiteDropModelArrayList;
    }

}
