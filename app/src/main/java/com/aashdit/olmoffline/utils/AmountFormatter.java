package com.aashdit.olmoffline.utils;//package com.aashdit.olmmis.utils;
//
//import java.math.BigDecimal;
//import java.text.DecimalFormat;
//import java.text.Format;
//import java.util.Locale;
//
//public class AmountFormatter {
//    public static boolean foundScientificNotation(String numberString) {
//        try {
//            new BigDecimal(numberString);
//        } catch (NumberFormatException e) {
//            return false;
//        }
//        return numberString.toUpperCase().contains("E");
//    }
//
//    /**
//     * Don't forget to  implementation files('libs\\icu4j-69.1.jar')
//     * */
//    public static String convertAmountToINRFormat(String strInputAmount) {
//        if (strInputAmount.equals(""))
//            strInputAmount = "0.00";
//        String amountToBeConverted = "";
//        String finalFormattedAmount = "";
//        String decimalValue = "";
//
//        DecimalFormat df1 = new DecimalFormat("#");
//        df1.setMaximumFractionDigits(2);
//        strInputAmount = df1.format(Double.parseDouble(strInputAmount));
//
//        if (strInputAmount.indexOf(".") != -1) {
//            amountToBeConverted = strInputAmount.substring(0, strInputAmount.indexOf("."));
//            decimalValue = strInputAmount.substring(strInputAmount.indexOf(".") + 1);
//            if (decimalValue.length() == 0)
//                decimalValue = decimalValue + "00";
//            if (decimalValue.length() == 1)
//                decimalValue = decimalValue + "0";
//        } else {
//            amountToBeConverted = strInputAmount;
//            decimalValue = "00";
//        }
//
//        StringBuilder stringBuilder = new StringBuilder();
//        char amountArray[] = amountToBeConverted.toCharArray();
//        int length1 = 0, length2 = 0;
//        for (int i = amountArray.length - 1; i >= 0; i--) {
//            if (length1 < 3) {
//                stringBuilder.append(amountArray[i]);
//                length1++;
//            } else if (length2 < 2) {
//                if (length2 == 0) {
//                    stringBuilder.append(",");
//                    stringBuilder.append(amountArray[i]);
//                    length2++;
//                } else {
//                    stringBuilder.append(amountArray[i]);
//                    length2 = 0;
//                }
//            }
//        }
//
//        finalFormattedAmount = stringBuilder.reverse().append(".").append(decimalValue).toString();
//
//        if (foundScientificNotation(finalFormattedAmount)) {
//            finalFormattedAmount = new BigDecimal(finalFormattedAmount).toPlainString();
//            Format format = com.ibm.icu.text.NumberFormat.getCurrencyInstance(new Locale("en", "in"));
//            finalFormattedAmount = format.format(new BigDecimal(finalFormattedAmount));
//        }
//        if (finalFormattedAmount.contains("Rs."))
//            finalFormattedAmount = finalFormattedAmount.replaceAll("Rs.", "");
//        return finalFormattedAmount.trim();
//    }
//
//}
