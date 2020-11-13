package com.jbol.dailydrops;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jbol.dailydrops.database.FirebaseDataBaseHelper;
import com.jbol.dailydrops.database.SQLiteDataBaseHelper;
import com.jbol.dailydrops.models.FirebaseDropList;
import com.jbol.dailydrops.models.FirebaseDropModel;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.views.DropAdapter;
import com.jbol.dailydrops.views.DropClickListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private SQLiteDataBaseHelper sqldbHelper;
    private FirebaseDataBaseHelper fbdbHelper;

    private RecyclerView recyclerView;
    private DropAdapter adapter;
    private ArrayList<FirebaseDropModel> firebaseDropModelArrayList;
    private ArrayList<GlobalDropModel> dropModelArrayList;

    private DatabaseReference fbDropsReference;

    private static MainActivity instance;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        MainActivity.context = getApplicationContext();

        sqldbHelper = SQLiteDataBaseHelper.getHelper(MainActivity.this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dropModelArrayList = new ArrayList<>();
        DropClickListener dropClickListener = new DropClickListener(this, recyclerView, dropModelArrayList);

        adapter = new DropAdapter(this, dropModelArrayList, dropClickListener);
        recyclerView.setAdapter(adapter);
        updateListData();

        Button btn_addOne = findViewById(R.id.btn_addOne);
        btn_addOne.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddActivity.class);
            startActivity(i);
        });

        initializeFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListData();
    }

    private ArrayList<FirebaseDropModel> collectFirebaseDrops(Object snapshotValue) {
        ArrayList<FirebaseDropModel> drops = new ArrayList<>();

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(snapshotValue);
        FirebaseDropList dropList = gson.fromJson(jsonElement, FirebaseDropList.class);

        Iterator it = dropList.getDrops().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            FirebaseDropModel drop = (FirebaseDropModel) pair.getValue();
            drops.add(drop);
            it.remove();
        }

        return drops;
    }

    private void initializeFirebase() {
        fbdbHelper = FirebaseDataBaseHelper.getHelper();
        fbDropsReference = FirebaseDatabase.getInstance().getReference();

        // Read from the database
        fbDropsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) { return; }
                Log.d("dev", "Changed after return");
                ArrayList<FirebaseDropModel> drops = collectFirebaseDrops(dataSnapshot.getValue());
                showFirebaseDrops(drops);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("dev", "Failed to read value.", error.toException());
            }
        });
    }

    public static Context getContext() {
        return MainActivity.context.getApplicationContext();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void showDetails(GlobalDropModel drop) {
        // Show details of clicked drop
        Intent i = new Intent(MainActivity.this, DetailsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("drop", drop);
        MainActivity.getContext().startActivity(i);
    }

    private void showFirebaseDrops(ArrayList<FirebaseDropModel> drops) {

    }

    private void updateListData() {
//        List<SQLiteDropModel> drops = sqldbHelper.getAllDrops();
//        SQLiteDropModelArrayList.clear();
//        SQLiteDropModelArrayList.addAll(drops);
//        customerClickListener.setCustomerModelArrayList(customerModelArrayList);

        dropModelArrayList.clear();

        List<SQLiteDropModel> sqLiteDropModels = sqldbHelper.getAllDrops();
        for (SQLiteDropModel sqLiteDropModel : sqLiteDropModels) {
            dropModelArrayList.add(new GlobalDropModel(sqLiteDropModel));
        }

        adapter.notifyDataSetChanged();
    }
}