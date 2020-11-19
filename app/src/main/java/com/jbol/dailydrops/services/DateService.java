package com.jbol.dailydrops.services;

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
    private static SimpleDateFormat sdfDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static SimpleDateFormat sdfDDMM = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private static SimpleDateFormat sdfHHMMZ = new SimpleDateFormat("HH:mm'Z'", Locale.getDefault());
    private static SimpleDateFormat sdfHHMM = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static long fullDateStringToEpochMilli(String dateString) throws ParseException {
        Date date = sdfDDMMYYYY.parse(dateString);
        return date != null ? date.getTime() : 0L;
    }

    public static long timeStringToEpochMilli(String timeString) throws ParseException {
        Date time = sdfHHMM.parse(timeString);
        return time != null ? time.getTime() : 0L;
    }

    public static String epochMilliToHHMM(long epoch) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        return zdt.format(formatter);
    }

    public static String epochMilliToFullDateString(long epoch) {
        return sdfDDMMYYYY.format(epoch);
    }

    public static String epochMilliToFormatDateString(long epoch, FormatStyle formatStyle) {
        Instant instant = Instant.ofEpochMilli(epoch);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(formatStyle);
        return zdt.format(formatter);
    }

    public static String epochMilliToDDMM(long epoch) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(epoch);

        sdfDDMM.setTimeZone(TimeZone.getDefault());
        return sdfDDMM.format(cal.getTime());
    }

    public static long getDayInEpochMilli() {
        return 86400000L;
    }

    public static long getNowInEpochMilli() {
        return Instant.now().getEpochSecond() * 1000L;
    }

}
