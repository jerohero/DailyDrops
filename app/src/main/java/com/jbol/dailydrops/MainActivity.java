package com.jbol.dailydrops;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jbol.dailydrops.database.SQLiteDataBaseHelper;
import com.jbol.dailydrops.models.FirebaseDropList;
import com.jbol.dailydrops.models.FirebaseDropModel;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.views.DropAdapter;
import com.jbol.dailydrops.views.DropClickListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private SQLiteDataBaseHelper sqldbHelper;

    private RecyclerView recyclerView;
    private FloatingActionButton fab_add, fab_search;
    private ConstraintLayout cl_search_label;
    private TextView tv_no_results;
    private DropAdapter adapter;
    private ArrayList<FirebaseDropModel> firebaseDropModelArrayList = new ArrayList<>();
    private ArrayList<GlobalDropModel> dropModelArrayList;

    private DatabaseReference fbDropsReference;

    private static MainActivity instance;

    private static Context context;

    private static String searchTerm = "";

    private static boolean showServerDrops = true;
    private static boolean showLocalDrops = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        MainActivity.context = getApplicationContext();

        initializeFirebase();

        sqldbHelper = SQLiteDataBaseHelper.getHelper(MainActivity.this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        tv_no_results = findViewById(R.id.tv_no_results);
        tv_no_results.setVisibility(View.GONE);

        dropModelArrayList = new ArrayList<>();
        DropClickListener dropClickListener = new DropClickListener(recyclerView, dropModelArrayList);

        adapter = new DropAdapter(this, dropModelArrayList, dropClickListener);
        recyclerView.setAdapter(adapter);
        updateListData();

        fab_add = findViewById(R.id.fab_add);
        fab_add.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddActivity.class);
            startActivity(i);
        });

        initializeSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListData();
    }

    private void initializeSearch() {
        TextView tv_search_term = findViewById(R.id.tv_search_term);

        cl_search_label = findViewById(R.id.cl_search_label);
        if (searchTerm.isEmpty()) {
            cl_search_label.setVisibility(View.GONE);
        } else {
            String searchLabel = getResources().getString(R.string.searchLabel) + " \"" + searchTerm + "\"";
            tv_search_term.setText(searchLabel);
        }

        cl_search_label.setOnClickListener(v -> {
            cl_search_label.setVisibility(View.GONE);
            searchTerm = "";
            updateListData();
        });

        fab_search = findViewById(R.id.fab_search);

        fab_search.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_search, null);

            EditText searchField = view.findViewById(R.id.et_search);
            Button searchBtn = view.findViewById(R.id.btn_search);
            CheckBox showServerCheck = view.findViewById(R.id.cb_show_server);
            CheckBox showLocalCheck = view.findViewById(R.id.cb_show_local);

            showServerCheck.setChecked(showServerDrops);
            showLocalCheck.setChecked(showLocalDrops);

            builder.setView(view);
            AlertDialog dialog = builder.create();
            dialog.show();

            searchBtn.setOnClickListener(v1 -> {
                if (showServerCheck.isChecked()) {
                    if (!showServerDrops) showServerDrops = true;
                } else {
                    if (showServerDrops) showServerDrops = false;
                }

                if (showLocalCheck.isChecked()) {
                    if (!showLocalDrops) showLocalDrops = true;
                } else {
                    if (showLocalDrops) showLocalDrops = false;
                }

                if (!searchField.getText().toString().isEmpty()) {
                    searchTerm = searchField.getText().toString();
                    cl_search_label.setVisibility(View.VISIBLE);
                    String searchLabel = getResources().getString(R.string.searchLabel) +  " \"" + searchTerm + "\"";
                    tv_search_term.setText(searchLabel);

                } else {
                    cl_search_label.setVisibility(View.GONE);
                    searchTerm = "";
                }

                dialog.dismiss();

                updateListData();
            });
        });
    }

    private ArrayList<FirebaseDropModel> collectFirebaseDrops(Object snapshotValue) {
        ArrayList<FirebaseDropModel> drops = new ArrayList<>();

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(snapshotValue);
        FirebaseDropList dropList = gson.fromJson(jsonElement, FirebaseDropList.class);

        Iterator<Map.Entry<String, FirebaseDropModel>> it = dropList.getDrops().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, FirebaseDropModel> pair = (Map.Entry<String, FirebaseDropModel>)it.next();
            FirebaseDropModel drop = (FirebaseDropModel) pair.getValue();
            drops.add(drop);
            it.remove();
        }

        return drops;
    }

    private void initializeFirebase() {
        fbDropsReference = FirebaseDatabase.getInstance().getReference();

        // Read from the database
        fbDropsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) { return; }
                firebaseDropModelArrayList = collectFirebaseDrops(dataSnapshot.getValue());
                updateListData();
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

    private void updateListData() {
        dropModelArrayList.clear();

        List<SQLiteDropModel> sqLiteDropModels = sqldbHelper.getAllDrops();

        long day = 86400L;
        long now = Instant.now().getEpochSecond() * 1000L;

        if (showLocalDrops) {
            for (SQLiteDropModel sqLiteDropModel : sqLiteDropModels) {
                if (
                        searchTerm.isEmpty() ||
                        sqLiteDropModel.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        sqLiteDropModel.getNote().toLowerCase().contains(searchTerm.toLowerCase())
                ) {
                    if (sqLiteDropModel.getDate() <= now - (day * 5)) {
                        sqldbHelper.deleteDrop(sqLiteDropModel.getId()); // Delete drop if it released over five days ago
                    } else {
                        dropModelArrayList.add(new GlobalDropModel(sqLiteDropModel));
                    }
                }
            }
        }

        if (showServerDrops) {
            for (FirebaseDropModel firebaseDropModel : firebaseDropModelArrayList) {
                if (
                        searchTerm.isEmpty() ||
                        firebaseDropModel.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        firebaseDropModel.getNote().toLowerCase().contains(searchTerm.toLowerCase())
                ) {
                    if (firebaseDropModel.getDate() > now - (day * 5)) {
                        dropModelArrayList.add(new GlobalDropModel(firebaseDropModel)); // Don't show drops that released over five days ago
                    }
                }
            }
        }

        if (dropModelArrayList.size() <= 0) {
            Log.d("FLIEPFLAP", "updateListData: " + tv_no_results.getText());
            tv_no_results.setVisibility(View.VISIBLE);
        } else if (tv_no_results.getVisibility() == View.VISIBLE) {
            tv_no_results.setVisibility(View.GONE);
        }

        Collections.sort(dropModelArrayList);

        adapter.notifyDataSetChanged();
    }

}