package com.jbol.dailydrops.views;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.jbol.dailydrops.R;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.services.DateService;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DropHolder extends RecyclerView.ViewHolder {

    private TextView txtTitle, txtNote, txtDate;
    private CardView cv_card;

    public DropHolder(View itemView) {
        super(itemView);
        txtTitle = itemView.findViewById(R.id.txtTitle);
        txtNote = itemView.findViewById(R.id.txtNote);
        txtDate = itemView.findViewById(R.id.txtDate);
        cv_card = itemView.findViewById(R.id.cv_card);
    }

    public void setDetails(GlobalDropModel drop) {
        txtTitle.setText(drop.getTitle());
        txtNote.setText(String.format(Locale.ENGLISH, "Note: %s", drop.getNote()));
        txtDate.setText(String.format(Locale.ENGLISH, "Date: %s", DateService.EpochMilliToDateString(drop.getDate(), FormatStyle.MEDIUM)));

        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            cv_card.setCardBackgroundColor(Color.parseColor("#B3D7FF"));
        } else {
            cv_card.setCardBackgroundColor(Color.parseColor("#74B6FF"));
        }
    }

}
