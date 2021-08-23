package com.aashdit.olmoffline.utils;

import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by Manabendu on 26/05/20
 */
public class Utility {

  public static boolean isStringValid(String value) {
    boolean isValid = false;

    if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
      isValid = true;
    }

    return isValid;
  }

  public static String getFileExtension(File file) {
    String fileName = "";
    if (file != null) {
      fileName = file.getName();
    }
    if (fileName.indexOf(".") != -1 && fileName.indexOf(".") != 0)
      return fileName.substring(fileName.indexOf(".") + 1);
    else
      return "";
  }
  public static String rupeeFormat(String value) {
//        value=value.replace(",","");
//        char lastDigit=value.charAt(value.length()-1);
//        String result = "";
//        int len = value.length()-1;
//        int nDigits = 0;
//
//        for (int i = len - 1; i >= 0; i--)
//        {
//            result = value.charAt(i) + result;
//            nDigits++;
//            if (((nDigits % 2) == 0) && (i > 0))
//            {
//                result = "," + result;
//            }
//        }


    Locale locale = new Locale("en", "IN");
    DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
    DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
    dfs.setCurrencySymbol("\u20B9");
    decimalFormat.setDecimalFormatSymbols(dfs);
    Double newDouble = Double.parseDouble(value);
    System.out.println(decimalFormat.format(newDouble));

    return decimalFormat.format(newDouble);

//        return (result+lastDigit);
  }


  public static String convertTO_dd_MMM_YYYY(String date) {
    List<String> startDate = Arrays.asList(date.split("/"));
    String day = startDate.get(0);
    String _month = startDate.get(1);
    String year = startDate.get(2);
    String month = Utility.convertMonthToWord(_month);

    return day + "-" + month + "-" + year;
  }


  public static String convertMonthToWord(String month) {

    if (month.equals("01") || month.equals("1")) {
      month = "JAN";
    } else if (month.equals("02") || month.equals("2")) {
      month = "FAB";
    } else if (month.equals("03") || month.equals("3")) {
      month = "MAR";
    } else if (month.equals("04") || month.equals("4")) {
      month = "APR";
    } else if (month.equals("05") || month.equals("5")) {
      month = "MAY";
    } else if (month.equals("06") || month.equals("6")) {
      month = "JUN";
    } else if (month.equals("07") || month.equals("7")) {
      month = "JUL";
    } else if (month.equals("08") || month.equals("8")) {
      month = "AUG";
    } else if (month.equals("09") || month.equals("9")) {
      month = "SEP";
    } else if (month.equals("10")) {
      month = "OCT";
    } else if (month.equals("11")) {
      month = "NOV";
    } else if (month.equals("12")) {
      month = "DEC";
    }else {
      month = "";
    }

    return month;
  }


  public static long stringDateToLong(String date) {
    long milliseconds = 0;
    SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
    try {
      Date d = f.parse(date);
      milliseconds = d.getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    Log.i("TAG", "stringDateToLong: ::::::::::::::::::::::::: " + milliseconds);
    return milliseconds;
  }

}
