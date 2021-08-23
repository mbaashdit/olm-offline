package com.aashdit.olmoffline.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static Context mContext;
    private static SharedPrefManager mInstance;

    public SharedPrefManager(Context context) {
        mContext = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null)
            mInstance = new SharedPrefManager(context);
        return mInstance;
    }

    public boolean getLogin(){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        return preferences.getBoolean(Constant.APP_LOGIN,false);
    }

//    public String getStoreToken() {
//        SharedPreferences preferences = mContext.getSharedPreferences(Constants.PREF_APP, Context.MODE_PRIVATE);
//        return preferences.getString(STORE_TOKEN, null);
//    }

    public void setIntData(String KEY, int value){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY, value).apply();
    }

    public int getIntData(String KEY){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        return preferences.getInt(KEY,-1);
    }

    public void setStringData(String KEY, String value){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY, value).apply();
    }
    public void setLongData(String KEY, Long value){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY, value).apply();
    }

    public String getStringData(String KEY){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        return preferences.getString(KEY,"");
    }
    public Long getLongData(String KEY){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        return preferences.getLong(KEY,0L);
    }

    public void setBoolData(String KEY, boolean value){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY, value).apply();
    }

    public boolean getBoolData(String KEY){
        SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY,false);
    }


    public void isOfficial(boolean status) {
        if (status) {
            SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constant.IS_OFICIAL, true).apply();
        } else {
            SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constant.IS_OFICIAL, false).apply();
        }

    }

    public void isLogin(boolean status) {
        if (status) {
            SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constant.APP_LOGIN, true).apply();
        } else {
            SharedPreferences preferences = mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constant.APP_LOGIN, false).apply();
        }

    }

    public void clear(){
        SharedPreferences preferences =mContext.getSharedPreferences(Constant.PREF_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

}
