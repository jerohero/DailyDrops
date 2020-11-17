package com.jbol.dailydrops;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.jbol.dailydrops.database.SQLiteDataBaseHelper;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.ImageService;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

public class AddUpdateActivity extends AppCompatActivity {
    private LinearLayout ll_save_drop;
    private TextInputLayout til_title, til_note, til_date;
    private ImageButton ib_add_image, ib_remove_image;
    private EditText et_date, et_title, et_note;
    private ImageView iv_image, iv_back_btn;
    private TextView tv_no_image, tv_save_drop_label, tv_activity_title;


    final Calendar dateCalendar = Calendar.getInstance();

    //Request code gallery
    private static final int GALLERY_REQUEST = 9;
    //Request code for camera
    private static final int CAMERA_REQUEST = 11;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    private SQLiteDataBaseHelper sqldbHelper;

    private Bitmap selectedImageBitmap;

    private GlobalDropModel drop;

    private boolean editMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update);

        Intent intent = getIntent();
        drop = (GlobalDropModel) intent.getSerializableExtra("drop");

        editMode = drop != null; // Decides whether user is editing or adding a drop

        tv_activity_title = findViewById(R.id.tv_activity_title);
        ll_save_drop = findViewById(R.id.ll_save_drop);
        tv_save_drop_label = findViewById(R.id.tv_save_drop_label);
        til_title = findViewById(R.id.til_title);
        et_title = findViewById(R.id.et_title);
        til_note = findViewById(R.id.til_note);
        et_note = findViewById(R.id.et_note);

        sqldbHelper = SQLiteDataBaseHelper.getHelper(AddUpdateActivity.this);

        initializeDatePicker();
        initializeImage();
        initializeBackBtn();

        if (editMode) {
            initializeValues();
            initializeSaveEditBtn();
            tv_activity_title.setText(R.string.editDrop);
        } else {
            initializeAddDropBtn();
        }
    }

    private void initializeValues() {
        et_title.setText(drop.getTitle());
        et_note.setText(drop.getNote());
        et_date.setText(sdf.format(drop.getDate()));
    }

    private void initializeDatePicker() {
        til_date = findViewById(R.id.til_date);
        et_date = findViewById(R.id.et_date);
        DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
            dateCalendar.set(Calendar.YEAR, year);
            dateCalendar.set(Calendar.MONTH, month);
            dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            et_date.setText(sdf.format(dateCalendar.getTime()));
        };

        et_date.setOnClickListener(v ->
                new DatePickerDialog(AddUpdateActivity.this, date, dateCalendar.get(Calendar.YEAR),
                        dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DAY_OF_MONTH))
                        .show());
    }

    private void initializeImage() {
        iv_image = findViewById(R.id.iv_image);
        ib_add_image = findViewById(R.id.ib_add_image);
        ib_remove_image = findViewById(R.id.ib_remove_image);
        tv_no_image = findViewById(R.id.tv_no_image);

        ib_remove_image.setVisibility(View.GONE);
        iv_image.setVisibility(View.GONE);

        ib_add_image.setOnClickListener(v -> showImageOptionDialog());
        ib_remove_image.setOnClickListener(v -> removeImage());

        if (editMode) {
            if (drop.getImage() == null) {
                return;
            }
            Bitmap image = ImageService.loadImageFromStorage(this, Integer.parseInt(drop.getImage()));
            iv_image.setImageBitmap(image);
            showImageElements();
        }
    }

    private void removeImage() {
        selectedImageBitmap = null;
        tv_no_image.setVisibility(View.VISIBLE);
        iv_image.setVisibility(View.GONE);
        ib_remove_image.setVisibility(View.GONE);
        // Set top margin of button container back to 70dp
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.ll_image_btn_container).getLayoutParams();
        params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics()); // Convert 70dp to px
    }

    private void showImageOptionDialog(){
        final String[] options = getResources().getStringArray(R.array.image_options);
        AlertDialog.Builder builder = new AlertDialog.Builder(AddUpdateActivity.this);
        builder.setTitle(R.string.dialog_image_options)
                .setItems(options, (dialog, which) -> {
                    switch (which){
                        case 0:
                            getImageFromGallery();
                            break;
                        case 1:
                            capturePictureFromCamera();
                            break;
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void capturePictureFromCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle gallery request
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();

            try {
                selectedImageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), selectedImage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Handle camera request
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null){
            selectedImageBitmap = (Bitmap) data.getExtras().get("data");
        }
        iv_image.setImageBitmap(selectedImageBitmap);

        if (iv_image.getVisibility() != View.VISIBLE) {
            showImageElements();
        }
    }

    private void showImageElements() {
        tv_no_image.setVisibility(View.GONE);
        iv_image.setVisibility(View.VISIBLE);
        ib_remove_image.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.ll_image_btn_container).getLayoutParams();
        params.topMargin = 0;
    }

    private void initializeAddDropBtn() {
        ll_save_drop.setOnClickListener(v -> {
            // Handle title
            String title = et_title.getText().toString();
            if (title.isEmpty()) {
                til_title.setError(getString(R.string.errorTitleEmpty));
                return;
            } else if (title.length() > til_title.getCounterMaxLength()) {
                til_title.setError(getString(R.string.errorTitleCharacterLimit));
                return;
            }
            if (til_title.getError() != null) til_title.setError(null);

            // Handle note
            String note = et_note.getText().toString();
            if (note.length() > til_note.getCounterMaxLength()) {
                til_note.setError(getString(R.string.errorNoteCharacterLimit));
                return;
            }
            if (til_note.getError() != null) til_note.setError(null);

            // Handle date
            long date;
            try {
                date = DateService.dateStringToEpochMilli(AddUpdateActivity.this, et_date.getText().toString());
            } catch (ParseException e) {
                til_date.setError(getString(R.string.errorDateEmpty));
                return;
            }
            long now = Instant.now().getEpochSecond() * 1000L;
            if (date < now) {
                til_date.setError(getString(R.string.errorInvalidDate));
                return;
            }
            if (til_date.getError() != null) til_date.setError(null);

            // Store drop
            SQLiteDropModel drop;
            boolean hasImage = false;
            if (selectedImageBitmap != null) {
                hasImage = true;
            }

            drop = new SQLiteDropModel(-1, title, note, date, hasImage);

            SQLiteDataBaseHelper mSQLiteDataBaseHelper = SQLiteDataBaseHelper.getHelper(AddUpdateActivity.this);

            boolean success = mSQLiteDataBaseHelper.addDropToLocal(drop);
            if (!success) {
                Toast.makeText(AddUpdateActivity.this, "Error creating drop", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasImage) {
                int lastId = mSQLiteDataBaseHelper.getLastInsertedDropIdFromLocal();
                ImageService.saveImageToInternalStorage(this, selectedImageBitmap, lastId);
            }

            backToMain();
        });
    }

    private void initializeSaveEditBtn() {
        tv_save_drop_label.setText(getString(R.string.saveDrop));

        ll_save_drop.setOnClickListener(v -> {
            drop.setTitle(et_title.getText().toString());
            drop.setNote(et_note.getText().toString());
            try {
                drop.setDate(DateService.dateStringToEpochMilli(this, et_date.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (selectedImageBitmap != null && drop.getImage() == null) {
                drop.setImage(drop.getId());
            } else if (selectedImageBitmap == null && drop.getImage() != null) {
                drop.setImage(null);
            }

            boolean success = false;
            try {
                success = SQLiteDataBaseHelper.getHelper(this).updateDropFromLocal(drop);
                Toast.makeText(this, drop.toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error updating drop", Toast.LENGTH_SHORT).show();
            }

            if (!success) {
                backToDetails();
                return;
            }

            if (drop.getImage() != null) {
                ImageService.saveImageToInternalStorage(this, selectedImageBitmap, Integer.parseInt(drop.getId()));
            }

            backToDetails();
        });
    }

    private void initializeBackBtn() {
        iv_back_btn = findViewById(R.id.iv_back_btn);

        iv_back_btn.setOnClickListener(v ->
                super.onBackPressed());
    }

    private void backToDetails() {
        Intent i = new Intent(this, DetailsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("drop", drop);
        MainActivity.getContext().startActivity(i);
    }

    private void backToMain() {
        Intent i = new Intent(this, MainActivity.class);
        MainActivity.getContext().startActivity(i);
    }
}

