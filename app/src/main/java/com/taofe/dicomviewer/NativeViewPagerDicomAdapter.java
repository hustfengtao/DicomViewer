package com.taofe.dicomviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.dcm4che3.android.Raster;
import org.dcm4che3.android.RasterUtil;
import org.dcm4che3.android.imageio.dicom.DicomImageReader;

import java.io.File;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class NativeViewPagerDicomAdapter extends PagerAdapter {
    private ArrayList<SeriesList> list;
    private Context context;
    private int mChildCount = 0;
    private int mSelectedChild = -1;
    private int windowWidth = 4096;
    private int windowCenter = 2048;
    public NativeViewPagerDicomAdapter(Context context, ArrayList<SeriesList> list){
        this.list = list;
        this.context = context;
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public float getPageWidth(int position) {
        return (float)0.333333333;
        //return super.getPageWidth(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if ( mChildCount >= 0) {
            mChildCount --;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public void notifyDataSetChanged() {
        mChildCount = ((DicomViewerActivity)context).thumbViewpager.getChildCount();
        super.notifyDataSetChanged();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.pager_item_dicomviewer, container, false);
        view.setTag(position);
        final ImageView photoThumb = (ImageView)view.findViewById(R.id.item_dicomimg);
        final ImageView photoDelete = (ImageView)view.findViewById(R.id.item_dicomimg_delete);
        try{
            File file = new File(list.get(position).get(0));
            final DicomImageReader reader = new DicomImageReader();
            reader.open(file);
            Raster raster = reader.applyWindowCenter(0, windowWidth, windowCenter);
            Bitmap bitmap = RasterUtil.rasterToBitmap(raster);
            photoThumb.setImageBitmap(bitmap);
            photoThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPhotoView(position);
                    setItemBorder(position);
                }
            });
            photoDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list.size()>1) {
                        if (mSelectedChild < position) {
                            //do nothing
                        } else if (mSelectedChild == position) {
                            if (mSelectedChild == list.size() - 1) {
                                mSelectedChild--;
                            }
                        } else if (mSelectedChild > position) {
                            mSelectedChild--;
                        }
                        refreshPhotoView(mSelectedChild);
                        setItemBorder(mSelectedChild);
                    }else{
                        for (FolderPhotoView view: ((DicomViewerActivity)context).dicomContainerlist){
                            view.clearDcm();
                            ((DicomViewerActivity)context).verticalSeekBar.setMax(0);
                        }
                        ((DicomViewerActivity)context).resetHistogramView();
                        ((DicomViewerActivity)context).verticalBarContainer.setVisibility(View.INVISIBLE);
                        mSelectedChild = -1;
                    }
                    ((DicomViewerActivity)context).seriesList.remove(position);
                    notifyDataSetChanged();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        container.addView(view);
        return view;
    }

    public void setItemBorder(int pos){
        if (pos<0||pos>getCount()){
            return;
        }
        clearAllBorder();
        ((DicomViewerActivity)context).thumbViewpager.findViewWithTag(pos).findViewById(R.id.item_dicomimg_border).setVisibility(View.VISIBLE);
        mSelectedChild = pos;
    }

    private void clearAllBorder(){
        for (int i=0;i!=getCount();i++){
            ((DicomViewerActivity)context).thumbViewpager.findViewWithTag(i).findViewById(R.id.item_dicomimg_border).setVisibility(View.INVISIBLE);
        }
    }

    private void refreshPhotoView(int position){
        if (position < 0){
            return;
        }
        if (!list.isEmpty()){
            for (FolderPhotoView view:((DicomViewerActivity)context).dicomContainerlist){
                if (view.isActive()){
                    view.setSeriesList(list.get(position));
                    try {
                        view.setDcm(list.get(position).get(0), 0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }else{
                    if (!view.getSeriesList().isEmpty()){
                        try {
                            view.setDcm(view.getSeriesList().get(0), 0);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        view.clearDcm();
                    }
                }
            }

            int max = ((DicomViewerActivity)context).dicomContainerlist.get(0).getSeriesList().size();
            for (FolderPhotoView view:((DicomViewerActivity)context).dicomContainerlist){
                if (view.getSeriesList().size() > max){
                    max = view.getSeriesList().size();
                }
            }
            ((DicomViewerActivity)context).verticalSeekBar.setMax(max);
            ((DicomViewerActivity)context).verticalSeekBar.setProgress(0);
        }
        mSelectedChild= position;
    }

    public int getSelectChild(){
        return mSelectedChild;
    }
}
