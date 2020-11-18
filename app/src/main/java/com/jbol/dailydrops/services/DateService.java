package com.jbol.dailydrops.services;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateService {
    private static SimpleDateFormat sdfDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private static SimpleDateFormat sdfDDMM = new SimpleDateFormat("dd/MM", Locale.ENGLISH);

    private static String timeZone = "Europe/Amsterdam"; // https://en.m.wikipedia.org/wiki/List_of_tz_database_time_zones


    public static long fullDateStringToEpochMilli(String dateString) throws ParseException {
        Date date = sdfDDMMYYYY.parse(dateString);
        return date != null ? date.getTime() : 0L;
    }

    public static String epochMilliToFullDateString(long epoch) {
        return sdfDDMMYYYY.format(epoch);
    }

    public static String epochMilliToFormatDateString(long epoch, FormatStyle formatStyle) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone));
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(formatStyle);
        return zdt.format(formatter);
    }

    public static String epochMilliToDDMM(long epoch) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(epoch);

        sdfDDMM.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
        return sdfDDMM.format(cal.getTime());
    }

    public static long getDayInEpochMilli() {
        return 86400000L;
    }

    public static long getNowInEpochMilli() {
        return Instant.now().getEpochSecond() * 1000L;
    }
}
