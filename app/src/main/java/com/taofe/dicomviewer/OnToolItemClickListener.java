package com.taofe.dicomviewer;
import androidx.recyclerview.widget.RecyclerView;

public interface OnToolItemClickListener{
    void OnClick(RecyclerView.ViewHolder holder, int position);
}
