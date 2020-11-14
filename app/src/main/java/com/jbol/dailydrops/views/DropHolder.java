package com.jbol.dailydrops.views;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jbol.dailydrops.MainActivity;
import com.jbol.dailydrops.R;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.services.AsyncURLService;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.ImageService;

import java.time.Instant;
import java.time.format.FormatStyle;

public class DropHolder extends RecyclerView.ViewHolder {

    private TextView txtTitle, txtDate;
    private CardView cv_card;
    private ImageView iv_image;

    public DropHolder(View itemView) {
        super(itemView);
        txtTitle = itemView.findViewById(R.id.tv_title);
        txtDate = itemView.findViewById(R.id.tv_date);

        cv_card = itemView.findViewById(R.id.cv_card);
    }

    public void setDetails(GlobalDropModel drop) {
        txtTitle.setText(drop.getTitle());
        txtDate.setText(DateService.EpochMilliToDateString(drop.getDate(), FormatStyle.MEDIUM));

        adjustDateStyling(drop);

        initializeImage(drop);

        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            cv_card.setCardBackgroundColor(Color.parseColor("#f8ffea"));
        } else {
            cv_card.setCardBackgroundColor(Color.parseColor("#eaf5ff"));
        }
    }

    private void adjustDateStyling(GlobalDropModel drop) {
        long day = 86400L;
        long now = Instant.now().getEpochSecond() * 1000L;
        if (drop.getDate() < now + day) {
            txtDate.setTextColor(Color.parseColor("#FF8688"));
        }
    }

    private void initializeImage(GlobalDropModel drop) {
        iv_image = itemView.findViewById(R.id.iv_image);

        if (drop.getImage() == null) {
            iv_image.setImageResource(R.drawable.dailydrops);
            iv_image.setImageAlpha(70);
            return;
        }
        iv_image.setImageAlpha(255);

        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) { // Image is stored locally, so retrieve it from storage
            Bitmap image = ImageService.loadImageFromStorage(MainActivity.getContext(), Integer.parseInt(drop.getImage()));
            iv_image.setImageBitmap(image);
        } else if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) { // Image is stored as a link, so retrieve it from internet
            new AsyncURLService(output -> iv_image.setImageBitmap(output)).execute(drop.getImage());
        }
    }

}
