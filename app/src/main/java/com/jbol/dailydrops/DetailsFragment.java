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
import android.widget.RelativeLayout;
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
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.ImageService;
import com.jbol.dailydrops.services.AsyncURLService;
import java.time.format.FormatStyle;

public class DetailsFragment extends Fragment {
    private TextView tv_title, tv_date, tv_note, tv_likes;
    private ImageButton ib_delete, ib_edit, ib_like;
    private FloatingActionButton fab_bookmark;
    private ImageView iv_image, iv_back_btn;
    private RelativeLayout rl_root;
    private ConstraintLayout cl_top_bar;
    private LinearLayout ll_title_wrapper;

    private GlobalDropModel drop;

    private SQLiteDatabaseHelper sqldbHelper;

    long likes;

    private DatabaseReference fbLikesRef;
    
    private Activity activity;

    public static DetailsFragment newInstance(GlobalDropModel drop) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("drop", drop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        rl_root = v.findViewById(R.id.rl_root);
        tv_title = v.findViewById(R.id.tv_title);
        tv_note = v.findViewById(R.id.tv_note);
        tv_likes = v.findViewById(R.id.tv_likes);
        ib_like = v.findViewById(R.id.ib_like);
        iv_image = v.findViewById(R.id.iv_image);
        tv_date = v.findViewById(R.id.tv_date);
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
            drop = (GlobalDropModel) getArguments().getSerializable("drop");
        }

        if (drop == null) {
            //todo handle error
            return;
        }

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
        initializeDate();
        initializeDeleteBtn();
        initializeEditBtn();
        initializeImage();
        initializeLikes();
        initializeCollection();

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initializeFirebase() {
        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) return;

        fbLikesRef = FirebaseDatabase.getInstance().getReference().child("drops").child(drop.getId()).child("likes");

        // Read from the database
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
        fab_bookmark.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark)));
        fab_bookmark.setOnClickListener(v -> {
            sqldbHelper.addDropToCollection(drop.getId(), drop.getType());
            Toast.makeText(activity, "Added drop to My Collection", Toast.LENGTH_SHORT).show();
            collectionBtnToRemoveFromCollection();
        });
    }

    private void collectionBtnToRemoveFromCollection() {
        fab_bookmark.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorLightRed)));
        fab_bookmark.setOnClickListener(v -> {
            sqldbHelper.deleteDropFromCollection(drop.getId(), drop.getType());
            Toast.makeText(activity, "Removed drop from My Collection", Toast.LENGTH_SHORT).show();
            collectionBtnToAddToCollection();

            // If drop is removed from collection while My Collection is active, update the list view
            if (MainActivity.getInstance().getActiveListType() == MainActivity.collectionListType) {
                MainActivity.getInstance().updateListData();
            }
        });
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
        ib_like.setOnClickListener(v -> {
            fbLikesRef.setValue(likes + 1);
            boolean success = sqldbHelper.addDropToLikes(drop.getId());
            if (!success) {
                Toast.makeText(activity, "Error occurred while liking drop. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            disableLikeButton();
        });
    }

    private void disableLikeButton() {
        ib_like.setBackground(ContextCompat.getDrawable(activity, R.drawable.roundcorner_grey));
        if (ib_like.hasOnClickListeners()) ib_like.setOnClickListener(null);
    }

    private void initializeImage() {
        if (drop.getImage() == null) {
            iv_image.setVisibility(View.INVISIBLE);
            iv_image.setMaxHeight(100);
            return;
        }

        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) { // Image is stored locally, so retrieve it from storage
            Bitmap image = ImageService.loadImageFromStorage(activity, Integer.parseInt(drop.getImage()));
            iv_image.setImageBitmap(image);
        } else if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) { // Image is stored as a link, so retrieve it from internet
            new AsyncURLService(output ->
                    iv_image.setImageBitmap(output)).execute(drop.getImage());
        }
    }

    private void initializeDate() {
        tv_date.setText(DateService.epochMilliToFormatDateString(drop.getDate(), FormatStyle.FULL));
    }

    // Can only be executed if it's a local drop
    private void initializeDeleteBtn() {
        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ib_delete.setVisibility(View.GONE);
            return;
        }
        ib_delete.setOnClickListener(v -> {
            SQLiteDatabaseHelper sqldbHelper = SQLiteDatabaseHelper.getHelper(activity);
            boolean success = sqldbHelper.deleteDropFromLocal(Integer.parseInt(drop.getId()));
            if (!success) {
                Toast.makeText(activity, "Drop couldn't be deleted. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            ImageService.deleteImageFromStorage(activity, Integer.parseInt(drop.getId()));
            Intent i = new Intent(activity, MainActivity.class);
            DetailsFragment.this.startActivity(i);
        });
    }

    private void initializeBackBtn() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        iv_back_btn.setOnClickListener(v ->
                fm.beginTransaction().remove(this).commit()
        );

        // For some reason this part of the fragment is 'transparent',
        // meaning that the user could click through it. This avoids that.
        cl_top_bar.setSoundEffectsEnabled(false);
        cl_top_bar.setOnClickListener(v -> { });
    }

    private void initializeEditBtn() {
        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ib_edit.setVisibility(View.GONE);
            return;
        }
        ib_edit.setOnClickListener(v -> {
            Intent i = new Intent(activity, AddUpdateActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("drop", drop);
            DetailsFragment.this.startActivity(i);
        });
    }
}
