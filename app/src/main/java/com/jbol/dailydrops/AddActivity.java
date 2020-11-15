package com.jbol.dailydrops;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.ImageService;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {
    private LinearLayout ll_add_drop;
    private TextInputLayout til_title, til_note, til_date;
    private ImageButton ib_add_image, ib_remove_image;
    private EditText et_date;
    private ImageView iv_image, iv_back_btn;
    private TextView tv_no_image;


    final Calendar dateCalendar = Calendar.getInstance();

    //Request code gallery
    private static final int GALLERY_REQUEST = 9;
    //Request code for camera
    private static final int CAMERA_REQUEST = 11;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    SQLiteDataBaseHelper sqldbHelper;

    private Bitmap selectedImageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ll_add_drop = findViewById(R.id.ll_add_drop);
        til_title = findViewById(R.id.til_title);
        til_note = findViewById(R.id.til_note);

        sqldbHelper = SQLiteDataBaseHelper.getHelper(AddActivity.this);

        initializeDatePicker();
        initializeImage();
        initializeAddBtn();
        initializeBackBtn();
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
                new DatePickerDialog(AddActivity.this, date, dateCalendar.get(Calendar.YEAR),
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
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
            tv_no_image.setVisibility(View.GONE);
            iv_image.setVisibility(View.VISIBLE);
            ib_remove_image.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.ll_image_btn_container).getLayoutParams();
            params.topMargin = 0;
        }
    }

    private void initializeAddBtn() {
        ll_add_drop.setOnClickListener(v -> {
            // Handle title
            String title = "";
            EditText et_title = til_title.getEditText();
            if (et_title != null && !TextUtils.isEmpty(et_title.getText())) {
                title = et_title.getText().toString();
            }
            if (title.isEmpty()) {
                til_title.setError(getString(R.string.errorTitleEmpty));
                return;
            } else if (title.length() > til_title.getCounterMaxLength()) {
                til_title.setError(getString(R.string.errorTitleCharacterLimit));
                return;
            }
            if (til_title.getError() != null) til_title.setError(null);

            // Handle note
            String note = "";
            EditText et_note = til_note.getEditText();
            if (et_note != null && !TextUtils.isEmpty(et_note.getText())) {
                note = et_note.getText().toString();
            }
            if (note.length() > til_note.getCounterMaxLength()) {
                til_note.setError(getString(R.string.errorNoteCharacterLimit));
                return;
            }
            if (til_note.getError() != null) til_note.setError(null);

            // Handle date
            long date;
            try {
                date = DateService.dateStringToEpochMilli(AddActivity.this, et_date.getText().toString());
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

            SQLiteDataBaseHelper mSQLiteDataBaseHelper = SQLiteDataBaseHelper.getHelper(AddActivity.this);

            boolean success = mSQLiteDataBaseHelper.addDrop(drop);
            if (!success) {
                Toast.makeText(AddActivity.this, "Error creating drop", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasImage) {
                int lastId = mSQLiteDataBaseHelper.getLastInsertedDropId();
                ImageService.saveImageToInternalStorage(this, selectedImageBitmap, lastId);
            }
        });
    }

    private void initializeBackBtn() {
        iv_back_btn = findViewById(R.id.iv_back_btn);

        iv_back_btn.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            this.startActivity(i);
        });
    }
}

