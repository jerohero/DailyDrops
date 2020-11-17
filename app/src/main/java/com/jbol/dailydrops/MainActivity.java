package com.jbol.dailydrops;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//TODO
// Make add/edit activity a fragment
// Bookmark system
// Like system

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SQLiteDataBaseHelper sqldbHelper;

    private RecyclerView recyclerView;
    private FloatingActionButton fab_add, fab_search;
    private ConstraintLayout cl_search_label;
    private DrawerLayout dl_drawer_layout;
    private TextView tv_no_results;
    private MaterialToolbar tb_toolbar;

    private DropAdapter adapter;
    private ArrayList<FirebaseDropModel> firebaseDropModelArrayList = new ArrayList<>();
    private ArrayList<GlobalDropModel> dropModelArrayList;

    private DatabaseReference fbDropsReference;

    private static MainActivity instance;

    private static Context context;

    private static String searchTerm = "";

    private static boolean showServerDrops = true;
    private static boolean showLocalDrops = true;

    private int listType;

    public static final int defaultListType = 0;
    public static final int hotListType = 1;
    public static final int collectionListType = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        MainActivity.context = getApplicationContext();

        Intent intent = getIntent();
        listType = (int) intent.getIntExtra("listType", defaultListType);
        initializeListType();

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
            Intent i = new Intent(MainActivity.this, AddUpdateActivity.class);
            startActivity(i);
        });

        initializeSearch();
        initializeDrawer();
    }

    private void initializeListType() {
        tb_toolbar = findViewById(R.id.tb_toolbar);
        if (listType == hotListType) {
            showServerDrops = true;
            showLocalDrops = false;
            tb_toolbar.setTitle("Hot drops");
        } else if (listType == defaultListType) {
            showServerDrops = true;
            showLocalDrops = true;
        } else if (listType == collectionListType) {
            showServerDrops = true;
            showLocalDrops = true;
            tb_toolbar.setTitle("My Collection");
        }
    }

    private void initializeDrawer() {
        MaterialToolbar toolbar = findViewById(R.id.tb_toolbar);
        dl_drawer_layout = findViewById(R.id.dl_drawer_layout);
        NavigationView nav_view = findViewById(R.id.nav_view);

        nav_view.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle drawerToggle;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            drawerToggle = new ActionBarDrawerToggle(this, dl_drawer_layout, toolbar, R.string.drawerOpen, R.string.drawerClose);
            dl_drawer_layout.addDrawerListener(drawerToggle);
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.syncState();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent i;
        switch (menuItem.getItemId()){
            case R.id.nav_drops:
                i = new Intent(MainActivity.this, MainActivity.class);
                i.putExtra("listType", defaultListType);
                MainActivity.getContext().startActivity(i);
                break;
            case R.id.nav_bookmarks:
                i = new Intent(MainActivity.this, MainActivity.class);
                i.putExtra("listType", collectionListType);
                MainActivity.getContext().startActivity(i);
                break;
            case R.id.nav_hot:
                i = new Intent(MainActivity.this, MainActivity.class);
                i.putExtra("listType", hotListType);
                MainActivity.getContext().startActivity(i);
                break;
            case R.id.nav_contact:
            case R.id.nav_credits:
                getSupportFragmentManager().beginTransaction().replace(R.id.rl_content, new ContactFragment())
                        .commit();
                closeDrawer();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //Checks if the navigation drawer is open -- If so, close it
        if (dl_drawer_layout.isDrawerOpen(GravityCompat.START)) {
            dl_drawer_layout.closeDrawer(GravityCompat.START);
        }
        // If drawer is already close -- Do not override original functionality
        else {
            super.onBackPressed();
        }
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

            // If hotListType is active, only server drops are showed and this cannot be changed
            if (listType == MainActivity.hotListType) {
                showServerCheck.setChecked(true);
                showLocalCheck.setChecked(false);
                showServerCheck.setVisibility(View.GONE);
                showLocalCheck.setVisibility(View.GONE);
            }

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
            Map.Entry<String, FirebaseDropModel> pair = it.next();
            FirebaseDropModel drop = pair.getValue();
            drop.setId(pair.getKey());
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

        List<SQLiteDropModel> sqLiteDropModels = sqldbHelper.getAllDropsFromLocal();

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
                        sqldbHelper.deleteDropFromLocal(sqLiteDropModel.getId()); // Delete drop if it released over five days ago
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

        if (listType == defaultListType || listType == collectionListType) {
            Collections.sort(dropModelArrayList);
        } else if (listType == hotListType) {
            Comparator<GlobalDropModel> likesOrder =
                    (o1, o2) -> (int) (o1.getLikes() - o2.getLikes());
            Collections.sort(dropModelArrayList, Collections.reverseOrder(likesOrder));
        }

        adapter.notifyDataSetChanged();
    }

    private void closeDrawer(){
        if (dl_drawer_layout.isDrawerOpen(GravityCompat.START)){
            dl_drawer_layout.closeDrawer(GravityCompat.START);
        }
    }

}