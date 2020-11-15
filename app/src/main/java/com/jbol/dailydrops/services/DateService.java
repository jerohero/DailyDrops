package com.jbol.dailydrops.services;

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

public class DateService {
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    public static long dateStringToEpochMilli(Context ctx, String dateString) throws ParseException {
        Date date = sdf.parse(dateString);
        return date != null ? date.getTime() : 0L;
    }

    public static String EpochMilliToDateString(long epoch, FormatStyle formatStyle) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, DateService.getZoneId());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(formatStyle);
        return zdt.format(formatter);
    }

    public static ZoneId getZoneId() {
        return ZoneId.of("Europe/Amsterdam"); // https://en.m.wikipedia.org/wiki/List_of_tz_database_time_zones
    }
}
