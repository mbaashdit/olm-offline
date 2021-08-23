package com.aashdit.olmoffline.utils;

import android.net.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Manabendu on 09/06/20
 */
public class DateUtility {
    public static String formatDate(String date, String initDateFormat, String endDateFormat) throws ParseException {

        Date initDate;
        String parsedDate = "";
        try {
            initDate = new SimpleDateFormat(initDateFormat, Locale.getDefault()).parse(date);
            SimpleDateFormat formatter = new SimpleDateFormat(endDateFormat, Locale.getDefault());
            parsedDate = formatter.format(initDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return parsedDate;
    }

    public static String convertStringToData(String stringData)
            throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());//yyyy-MM-dd'T'HH:mm:ss
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date data = null;
        try {
            data = sdf.parse(stringData);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return output.format(data);
    }
}
