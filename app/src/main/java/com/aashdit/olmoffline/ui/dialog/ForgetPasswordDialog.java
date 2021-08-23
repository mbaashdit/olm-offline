package com.aashdit.olmoffline.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.aashdit.olmoffline.R;

import java.util.Objects;

public class ForgetPasswordDialog {


    public OnRatingListener onRatingListener;
    private final Dialog ratingDialog;
    private EditText mEtOldPassword, mEtNewPassword, mEtCnfPassword;
    private final ImageView close;
    private ImageView map;
    private ProgressBar progressBar;
    public ForgetPasswordDialog(Context context) {
        ratingDialog = new Dialog(context, R.style.AppTheme);
        View dialog = LayoutInflater.from(context).inflate(R.layout.rating_dialog, null);
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
    }

    public void hideCloseDialog() {
        ratingDialog.dismiss();
    }

    public void setOnRatingListener(OnRatingListener onRatingListener) {
        this.onRatingListener = onRatingListener;
    }

    public interface OnRatingListener {
        void ratingDone(Dialog dialog, boolean isClicked, ProgressBar progressBar);
    }



}




