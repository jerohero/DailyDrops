package com.jbol.dailydrops;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jbol.dailydrops.database.SQLiteDatabaseHelper;
import com.jbol.dailydrops.interfaces.DrawerLocker;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.ImageService;
import com.jbol.dailydrops.services.AsyncURLService;
import java.time.format.FormatStyle;
import java.util.HashMap;

import static com.jbol.dailydrops.MainActivity.COLLECTION_LIST_TYPE;

public class DetailsFragment extends Fragment {
    public static String DROP_SERIALIZABLE_STRING = "drop";

    private TextView tv_title, tv_date, tv_note, tv_likes, tv_time;
    private ImageButton ib_delete, ib_edit, ib_like;
    private FloatingActionButton fab_bookmark;
    private ImageView iv_image, iv_back_btn;
    private ConstraintLayout cl_top_bar;
    private LinearLayout ll_title_wrapper;

    private SQLiteDatabaseHelper sqldbHelper;
    private DatabaseReference fbLikesRef;
    private Activity activity;
    private GlobalDropModel drop;
    private long likes;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        tv_title = v.findViewById(R.id.tv_title);
        tv_note = v.findViewById(R.id.tv_note);
        tv_likes = v.findViewById(R.id.tv_likes);
        ib_like = v.findViewById(R.id.ib_like);
        iv_image = v.findViewById(R.id.iv_image);
        tv_date = v.findViewById(R.id.tv_date);
        tv_time = v.findViewById(R.id.tv_time);
        ib_delete = v.findViewById(R.id.ib_delete);
        iv_back_btn = v.findViewById(R.id.iv_back_btn);
        ib_edit = v.findViewById(R.id.ib_edit);
        fab_bookmark = v.findViewById(R.id.fab_bookmark);
        cl_top_bar = v.findViewById(R.id.cl_top_bar);
        ll_title_wrapper = v.findViewById(R.id.ll_title_wrapper);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();

        if (getArguments() != null) {
            drop = (GlobalDropModel) getArguments().getSerializable(DROP_SERIALIZABLE_STRING);
        }
        if (drop == null) {
            Toast.makeText(activity, "An error occurred while loading this drop", Toast.LENGTH_SHORT).show();
            exitFragment();
            return;
        }

        ((DrawerLocker) activity).setDrawerEnabled(false);

        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ll_title_wrapper.setBackgroundColor(getResources().getColor(R.color.colorOnline));
        } else {
            ll_title_wrapper.setBackgroundColor(getResources().getColor(R.color.colorLocal));
        }

        sqldbHelper = SQLiteDatabaseHelper.getHelper(activity);

        tv_title.setText(drop.getTitle());
        tv_note.setText(drop.getNote());

        initializeFirebase();
        initializeBackBtn();
        initializeDateAndTime();
        initializeDeleteBtn();
        initializeEditBtn();
        initializeImage();
        initializeLikes();
        initializeCollection();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static DetailsFragment newInstance(GlobalDropModel drop) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(DROP_SERIALIZABLE_STRING, drop);
        fragment.setArguments(args);
        return fragment;
    }

    private void deleteDrop() {
        SQLiteDatabaseHelper sqldbHelper = SQLiteDatabaseHelper.getHelper(activity);
        boolean success = sqldbHelper.deleteDropFromLocal(Integer.parseInt(drop.getId()));
        if (!success) {
            Toast.makeText(activity, "Drop couldn't be deleted. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        ImageService.deleteDropImageFromStorage(activity, Integer.parseInt(drop.getId()));
        Intent i = new Intent(activity, MainActivity.class);
        DetailsFragment.this.startActivity(i);
    }

    private void editDrop() {
        Intent i = new Intent(activity, AddUpdateActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(DROP_SERIALIZABLE_STRING, drop);
        DetailsFragment.this.startActivity(i);
    }

    private void likeDrop() {
        fbLikesRef.setValue(likes + 1);
        boolean success = sqldbHelper.addDropToLikes(drop.getId());
        if (!success) {
            Toast.makeText(activity, "Error occurred while liking drop. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        disableLikeButton();
    }

    // For reading value of drop's likes
    private void initializeFirebase() {
        // Only server drops have likes
        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) return;

        fbLikesRef = FirebaseDatabase.getInstance().getReference().child("drops").child(drop.getId()).child("likes");

        fbLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) { return; }
                likes = (long) dataSnapshot.getValue();
                updateLikes(likes);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("dev", "Failed to read value.", error.toException());
            }
        });
    }

    private void initializeCollection() {
        boolean inCollection = sqldbHelper.dropIsInCollection(drop.getId(), drop.getType());

        if (inCollection) {
            collectionBtnToRemoveFromCollection();
            return;
        }

        collectionBtnToAddToCollection();
    }

    private void collectionBtnToAddToCollection() {
        fab_bookmark.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorLightRed)));
        fab_bookmark.setOnClickListener(v ->
                addDropToCollection());
    }

    private void addDropToCollection() {
        sqldbHelper.addDropToCollection(drop.getId(), drop.getType());
        Toast.makeText(activity, "Added drop to My Collection", Toast.LENGTH_SHORT).show();
        collectionBtnToRemoveFromCollection();
    }

    private void collectionBtnToRemoveFromCollection() {
        fab_bookmark.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark)));
        fab_bookmark.setOnClickListener(v ->
                deleteDropFromCollection());
    }

    private void deleteDropFromCollection() {
        boolean success = sqldbHelper.deleteDropFromCollection(drop.getId(), drop.getType());
        if (!success) {
            Toast.makeText(activity, "Drop couldn't be removed from My Collection, please try again", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(activity, "Removed drop from My Collection", Toast.LENGTH_SHORT).show();
        collectionBtnToAddToCollection();

        // If drop is removed from collection while My Collection is active, update the list view
        if (MainActivity.getInstance().getActiveListType() == COLLECTION_LIST_TYPE) {
            MainActivity.getInstance().updateListData();
        }
    }

    private void updateLikes(long likes) {
        tv_likes.setText(String.valueOf(likes));
    }

    private void initializeLikes() {
        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) {
            ib_like.setVisibility(View.GONE);
            tv_likes.setVisibility(View.GONE);
            return;
        }

        boolean isLiked = sqldbHelper.dropIsLiked(drop.getId());

        if (isLiked) {
            disableLikeButton();
            return;
        }
        ib_like.setOnClickListener(v ->
                likeDrop());
    }

    private void disableLikeButton() {
        ib_like.setBackground(ContextCompat.getDrawable(activity, R.drawable.roundcorner_grey));
        if (ib_like.hasOnClickListeners()) ib_like.setOnClickListener(null);
    }

    private void initializeImage() {
        if (drop.getImage() == null) {
            iv_image.setVisibility(View.GONE);
            return;
        }

        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) { // Image is stored locally, so retrieve it from storage
            Bitmap image = ImageService.loadDropImageFromStorage(activity, Integer.parseInt(drop.getImage()));
            iv_image.setImageBitmap(image);
        } else if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) { // Image is stored as a link, so retrieve it from internet
            new AsyncURLService(output ->
                    iv_image.setImageBitmap(output)).execute(drop.getImage());
        }
    }

    private void initializeDateAndTime() {
        if (drop.getTime() <= 0) {
            tv_date.setText(DateService.epochMilliToUTCDateString(drop.getDate(), FormatStyle.FULL, "UTC"));
            tv_time.setVisibility(View.GONE);
        } else {
            long dateAndTime = drop.getDate() + drop.getTime();

            HashMap<String, String> dateOrTimeToString = DateService.dateAndTimeEpochMilliToDDMMYYYY_HHMM(dateAndTime, FormatStyle.SHORT);

            tv_date.setText(dateOrTimeToString.get("date"));
            tv_time.setText(dateOrTimeToString.get("time"));
        }
    }

    private void initializeDeleteBtn() {
        // Delete button only shows for local drops
        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ib_delete.setVisibility(View.GONE);
            return;
        }
        ib_delete.setOnClickListener(v ->
                deleteDrop());
    }

    private void initializeBackBtn() {
        iv_back_btn.setOnClickListener(v ->
                exitFragment());

        // For some reason this part of the fragment is 'transparent',
        // meaning that the user could click through it -- this avoids that
        cl_top_bar.setSoundEffectsEnabled(false);
        cl_top_bar.setOnClickListener(v -> { });
    }

    private void initializeEditBtn() {
        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ib_edit.setVisibility(View.GONE);
            return;
        }
        ib_edit.setOnClickListener(v ->
                editDrop());
    }

    private void exitFragment() {
        FragmentManager fm = MainActivity.getInstance().getSupportFragmentManager();
        fm.beginTransaction().remove(this).commit();
        MainActivity.getInstance().setDrawerEnabled(true);
    }

}
