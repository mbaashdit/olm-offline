package com.aashdit.olmoffline.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.aashdit.olmoffline.R;

import java.util.Objects;
import java.util.regex.Pattern;

public class ChangeDataDialog {


    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");
    private final Dialog ratingDialog;
    private final ImageView close;
    OnUpdateListener onUpdateListener;
    private EditText mEtOldValue, mEtNewValue;
    private Button mBtnUpdate;
    private TextView mTvLbl, mTvPasswordHint;
    private ProgressBar progressBar;

    public ChangeDataDialog(Context context, String type) {
        ratingDialog = new Dialog(context, R.style.AppTheme);
        View dialog = LayoutInflater.from(context).inflate(R.layout.change_dialog, null);
        ratingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(ratingDialog.getWindow()).setBackgroundDrawableResource(R.color.colorLightBlack);

        close = dialog.findViewById(R.id.iv_close);


        ratingDialog.setContentView(dialog);
        ratingDialog.setCancelable(true);
        ratingDialog.setCanceledOnTouchOutside(true);
        ratingDialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingDialog.dismiss();
            }
        });

        mEtOldValue = dialog.findViewById(R.id.et_old_value);
        mEtNewValue = dialog.findViewById(R.id.et_new_value);
        mBtnUpdate = dialog.findViewById(R.id.btn_update);
        mTvLbl = dialog.findViewById(R.id.tv_change_lbl);
        mTvPasswordHint = dialog.findViewById(R.id.tv_password_lbl);
        mTvPasswordHint.setVisibility(View.GONE);
        if (type.equals("PASSWORD")) {
            mTvPasswordHint.setVisibility(View.VISIBLE);
            mTvLbl.setText(context.getResources().getString(R.string.change_password));
            mEtOldValue.setHint(context.getResources().getString(R.string.old_password));
            mEtNewValue.setHint(context.getResources().getString(R.string.new_password));
        } else if (type.equals("EMAIL")) {
            mTvLbl.setText(context.getResources().getString(R.string.change_email));
            mEtOldValue.setHint(context.getResources().getString(R.string.old_email));
            mEtNewValue.setHint(context.getResources().getString(R.string.new_email));
        } else if (type.equals("MOBILE")) {
            mTvLbl.setText(context.getResources().getString(R.string.change_mobile));
            mTvPasswordHint.setVisibility(View.VISIBLE);
            mTvPasswordHint.setText(context.getResources().getString(R.string.mobile_no_val));
            mEtOldValue.setHint(context.getResources().getString(R.string.old_mobile));
            mEtNewValue.setHint(context.getResources().getString(R.string.new_mobile));
        }

        mBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldValue = mEtOldValue.getText().toString().trim();
                String newValue = mEtNewValue.getText().toString().trim();

                if (type.equals("MOBILE")) {
//                    if (TextUtils.isEmpty(oldValue) || TextUtils.isEmpty(newValue)) {
//                        Toast.makeText(context, "Please fill the field", Toast.LENGTH_SHORT).show();
//                    } else if (oldValue.length() > 10 || newValue.length() > 10) {
//                        Toast.makeText(context, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
//                    } else {
//                        onUpdateListener.updateDone(ratingDialog, type, oldValue, newValue);
//                    }
                    if (isValidPhoneNumber(oldValue) && isValidPhoneNumber(newValue)) {
                        onUpdateListener.updateDone(ratingDialog, type, oldValue, newValue);
                    } else {
                        Toast.makeText(context, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    }
                } else if (type.equals("PASSWORD")) {
//                    if (TextUtils.isEmpty(oldValue) || TextUtils.isEmpty(newValue)) {
//                        Toast.makeText(context, "Please fill the field", Toast.LENGTH_SHORT).show();
//                    } else if (oldValue.length() < 6 || newValue.length() < 6) {
//                        Toast.makeText(context, "Password length minimum 6 character", Toast.LENGTH_SHORT).show();
//                    } else {
//                        onUpdateListener.updateDone(ratingDialog, type, oldValue, newValue);
//                    }
                    if (TextUtils.isEmpty(oldValue)){
                        Toast.makeText(context, "Old Password is empty", Toast.LENGTH_SHORT).show();
                    }/*else if (!validatePassword(oldValue)){
                        Toast.makeText(context, "Password pattern not matched", Toast.LENGTH_SHORT).show();
                    }*/else if (TextUtils.isEmpty(newValue)){
                        Toast.makeText(context, "New Password is empty", Toast.LENGTH_SHORT).show();
                    }else if (!validatePassword(newValue)){
                        Toast.makeText(context, "Password pattern not matched", Toast.LENGTH_SHORT).show();
                    }else{
                        onUpdateListener.updateDone(ratingDialog, type, oldValue, newValue);
                    }
//                    if (validatePassword(newValue)) {
//                        onUpdateListener.updateDone(ratingDialog, type, oldValue, newValue);
//                    } else {
//                        Toast.makeText(context, "Password pattern not matched", Toast.LENGTH_SHORT).show();
//                    }
                } else if (type.equals("EMAIL")) {
                    if (TextUtils.isEmpty(oldValue) || TextUtils.isEmpty(newValue)) {
                        Toast.makeText(context, "Please fill the field", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!isValidEmail(oldValue)) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                        } else if (!isValidEmail(newValue)) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                        } else {
                            onUpdateListener.updateDone(ratingDialog, type, oldValue, newValue);
                        }
                    }
                }
            }
        });


    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validation of Phone Number
     */
    public final static boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || target.toString().isEmpty() || target.length() < 10 || target.length() > 10) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }

    }

    private boolean validatePassword(String passwordInput) {
//        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()) {
//            textInputPassword.setError("Field can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
//            textInputPassword.setError("Password too weak");
            return false;
        } else {
//            textInputPassword.setError(null);
            return true;
        }
    }

    public void hideCloseDialog() {
        ratingDialog.dismiss();
    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public interface OnUpdateListener {
        void updateDone(Dialog dialog, String type, String oldValue, String newValue);
    }

}




