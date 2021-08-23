package com.aashdit.olmoffline.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Manabendu on 25/05/20
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {


        outRect.left = space/2;
        outRect.top = space/2;
        outRect.right = space/2;
        outRect.bottom = space/2;

    }
}
