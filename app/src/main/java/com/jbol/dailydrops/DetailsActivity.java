package com.jbol.dailydrops;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

public class DetailsActivity extends AppCompatActivity {
    private TextView tv_title, tv_date, tv_note, tv_likes;
    private ImageButton ib_delete, ib_edit, ib_like;
    private ImageView iv_image, iv_back_btn;
    private RelativeLayout cl_root;

    private GlobalDropModel drop;

    private SQLiteDatabaseHelper sqldbHelper;

    long likes;

    private DatabaseReference fbLikesRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        sqldbHelper = SQLiteDatabaseHelper.getHelper(DetailsActivity.this);

        Intent intent = getIntent();
        drop = (GlobalDropModel) intent.getSerializableExtra("drop");

        cl_root = findViewById(R.id.cl_root);

        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            cl_root.setBackgroundColor(getResources().getColor(R.color.colorOnline));
        } else {
            cl_root.setBackgroundColor(getResources().getColor(R.color.colorLocal));
        }

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(drop.getTitle());

        tv_note = findViewById(R.id.tv_note);
        tv_note.setText(drop.getNote());

        tv_likes = findViewById(R.id.tv_likes);
        ib_like = findViewById(R.id.ib_like);
        iv_image = findViewById(R.id.iv_image);
        tv_date = findViewById(R.id.tv_date);
        ib_delete = findViewById(R.id.ib_delete);
        iv_back_btn = findViewById(R.id.iv_back_btn);
        ib_edit = findViewById(R.id.ib_edit);

        initializeFirebase();
        initializeBackBtn();
        initializeDate();
        initializeDeleteBtn();
        initializeEditBtn();
        initializeImage();
        initializeLikes();
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
                Toast.makeText(this, "Error occurred while liking drop. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            disableLikeButton();
        });
    }

    private void disableLikeButton() {
        ib_like.setBackground(ContextCompat.getDrawable(this, R.drawable.roundcorner_grey));
        if (ib_like.hasOnClickListeners()) ib_like.setOnClickListener(null);
    }

    private void initializeImage() {
        if (drop.getImage() == null) {
            iv_image.setVisibility(View.INVISIBLE);
            iv_image.setMaxHeight(100);
            return;
        }

        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) { // Image is stored locally, so retrieve it from storage
            Bitmap image = ImageService.loadImageFromStorage(this, Integer.parseInt(drop.getImage()));
            iv_image.setImageBitmap(image);
        } else if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) { // Image is stored as a link, so retrieve it from internet
            new AsyncURLService(output -> iv_image.setImageBitmap(output)).execute(drop.getImage());
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
            SQLiteDatabaseHelper sqldbHelper = SQLiteDatabaseHelper.getHelper(DetailsActivity.this);
            boolean success = sqldbHelper.deleteDropFromLocal(Integer.parseInt(drop.getId()));
            if (!success) {
                Toast.makeText(this, "Drop couldn't be deleted. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            ImageService.deleteImageFromStorage(this, Integer.parseInt(drop.getId()));
            Intent i = new Intent(DetailsActivity.this, MainActivity.class);
            DetailsActivity.this.startActivity(i);
        });
    }

    private void initializeBackBtn() {
        iv_back_btn.setOnClickListener(v ->
                super.onBackPressed());
    }

    private void initializeEditBtn() {
        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ib_edit.setVisibility(View.GONE);
            return;
        }
        ib_edit.setOnClickListener(v -> {
            Intent i = new Intent(DetailsActivity.this, AddUpdateActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("drop", drop);
            DetailsActivity.this.startActivity(i);
        });
    }
}
