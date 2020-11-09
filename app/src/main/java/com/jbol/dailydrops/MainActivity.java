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
import com.jbol.dailydrops.database.FirebaseDataBaseHelper;
import com.jbol.dailydrops.database.SQLiteDataBaseHelper;
import com.jbol.dailydrops.models.DropModel;
import com.jbol.dailydrops.views.DropAdapter;
import com.jbol.dailydrops.views.DropClickListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SQLiteDataBaseHelper sqldbHelper;
    private FirebaseDataBaseHelper fbdbHelper;

    private RecyclerView recyclerView;
    private DropAdapter adapter;
    private ArrayList<DropModel> dropModelArrayList;

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

    private void initializeFirebase() {
        fbdbHelper = FirebaseDataBaseHelper.getHelper();
        fbDropsReference = FirebaseDatabase.getInstance().getReference("drops");

        // Read from the database
        fbDropsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("dev", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
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

    public void showDetails(DropModel drop) {
        // Show details of clicked drop
        Intent i = new Intent(MainActivity.this, DetailsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("drop", drop);
        MainActivity.getContext().startActivity(i);
    }

    private void updateListData() {
        List<DropModel> drops = sqldbHelper.getAllDrops();
        dropModelArrayList.clear();
        dropModelArrayList.addAll(drops);
//        customerClickListener.setCustomerModelArrayList(customerModelArrayList);
        adapter.notifyDataSetChanged();
    }
}