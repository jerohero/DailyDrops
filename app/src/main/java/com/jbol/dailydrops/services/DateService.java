package com.jbol.dailydrops.services;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class DateService {
    private static SimpleDateFormat sdfHHMM = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static long fullDateStringToEpochMilli(String dateString, ZoneId timeZoneId) {
        ZonedDateTime zdt;
        LocalDateTime ldt = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        zdt = ldt.atZone(timeZoneId);

        long date = zdt.toInstant().toEpochMilli();
        return date != 0 ? date : 0L;
    }

    public static long timeStringToEpochMilli(String timeString) throws ParseException {
        ZoneId timeZoneId = ZoneId.systemDefault();
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        sdfHHMM.setTimeZone(timeZone);
        Date time = sdfHHMM.parse(timeString);

        long epochOffset = 0L;
        if (time != null) {
            epochOffset = timeZone.getOffset(time.getTime());
        }

        return time != null ? time.getTime() + epochOffset : 0L;
    }

    public static String epochMilliToDefTimeZoneDDMMYYYY(long epoch) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return zdt.format(formatter);
    }

    // Is used when no time is given to the drop -- avoids having different dates for some timezones
    public static String epochMilliToUTCDateString(long epoch, FormatStyle formatStyle, String timeZone) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone));
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(formatStyle);
        return zdt.format(formatter);
    }

    public static String epochMilliToDefTimeZoneDDMM(long epoch) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");


        return zdt.format(formatter);
    }

    public static String dateEpochMilliToUTCDDMMYYYY(long date) {
        Instant instant = Instant.ofEpochMilli(date);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return dateFormatter.format(zdt);
    }

    public static HashMap<String, String> dateAndTimeEpochMilliToDDMMYYYY_HHMM(long dateAndTime, FormatStyle dateFormatStyle) {
        HashMap<String, String> dateOrTimeToString = new HashMap<>();

        Instant instant = Instant.ofEpochMilli(dateAndTime);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

        dateOrTimeToString.put("date", zdt.format(DateTimeFormatter.ofLocalizedDate(dateFormatStyle)));
        dateOrTimeToString.put("time", zdt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));

        return dateOrTimeToString;
    }

    public static HashMap<String, String> dateAndTimeEpochMilliToCustomDDMMYYYY_HHMM(long dateAndTime, DateTimeFormatter dateFormatter, DateTimeFormatter timeFormatter) {
        HashMap<String, String> dateOrTimeToString = new HashMap<>();

        Instant instant = Instant.ofEpochMilli(dateAndTime);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

        dateOrTimeToString.put("date", dateFormatter.format(zdt));
        dateOrTimeToString.put("time", timeFormatter.format(zdt));

        return dateOrTimeToString;
    }

    public static long getDayInEpochMilli() {
        return 86400000L;
    }

    public static long getNowInEpochMilli() {
        return Instant.now().getEpochSecond() * 1000L;
    }

}
