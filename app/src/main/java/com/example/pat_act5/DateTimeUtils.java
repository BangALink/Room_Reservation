package com.example.pat_act5;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "dd MMM yyyy, HH:mm";

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDateForDisplay(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    public static String formatTimeForDisplay(String timeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date time = inputFormat.parse(timeString);
            return outputFormat.format(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return timeString;
        }
    }

    public static String formatDateTimeForDisplay(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale.getDefault());
            Date dateTime = inputFormat.parse(dateTimeString);
            return outputFormat.format(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeString;
        }
    }

    public static boolean isDateValid(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isTimeValid(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(timeString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isDateInFuture(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date inputDate = sdf.parse(dateString);
            Date currentDate = new Date();

            // Set time to start of day for comparison
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(inputDate);
            cal2.setTime(currentDate);

            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);

            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);

            return cal1.getTime().after(cal2.getTime()) || cal1.getTime().equals(cal2.getTime());
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isTimeAfterCurrent(String dateString, String timeString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());

            Date inputDate = dateFormat.parse(dateString);
            Date inputTime = timeFormat.parse(timeString);
            Date currentDate = new Date();

            Calendar inputCal = Calendar.getInstance();
            Calendar timeCal = Calendar.getInstance();
            Calendar currentCal = Calendar.getInstance();

            inputCal.setTime(inputDate);
            timeCal.setTime(inputTime);
            currentCal.setTime(currentDate);

            // Set the time to input calendar
            inputCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            inputCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            inputCal.set(Calendar.SECOND, 0);
            inputCal.set(Calendar.MILLISECOND, 0);

            return inputCal.getTime().after(currentCal.getTime());
        } catch (ParseException e) {
            return false;
        }
    }

    public static int compareTime(String time1, String time2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            Date t1 = sdf.parse(time1);
            Date t2 = sdf.parse(time2);
            return t1.compareTo(t2);
        } catch (ParseException e) {
            return 0;
        }
    }

    public static String addMinutesToTime(String timeString, int minutes) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            Date time = sdf.parse(timeString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            cal.add(Calendar.MINUTE, minutes);
            return sdf.format(cal.getTime());
        } catch (ParseException e) {
            return timeString;
        }
    }
}
