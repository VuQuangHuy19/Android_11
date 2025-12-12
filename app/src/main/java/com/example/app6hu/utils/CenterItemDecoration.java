package com.example.app6hu.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;

public class CenterItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;
    private final int spacing;
    private final boolean includeEdge;

    public CenterItemDecoration(Context context, int spanCount) {
        this.spanCount = spanCount;
        this.spacing = context.getResources().getDimensionPixelSize(R.dimen.spacing_small);
        this.includeEdge = true;
    }

    public CenterItemDecoration(Context context, int spanCount, int spacingResId, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = context.getResources().getDimensionPixelSize(spacingResId);
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position < 0) return;

        int column = position % spanCount;

        if (includeEdge) {
            // Cách tính đơn giản hơn để không làm item bị co
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) { // top row
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            // Không bao gồm edge spacing
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;

            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}