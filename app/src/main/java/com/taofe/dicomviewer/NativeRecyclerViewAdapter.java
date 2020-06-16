package com.taofe.dicomviewer;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;


public class NativeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static int TYPE_UNKNOWN = -1;
    public static int TYPE_DEFAULT = 0;
    public static int TYPE_PHOTO =1;
    public static int TYPE_PAGER =2;
    public static int TYPE_DICOMTAG =3;
    public static int TYPE_TOOLBAR = 4;
    //...
    private ArrayList<TypedViewData> mData;
    private Context mContext;
    private OnToolItemClickListener onItemClickListener = null;
    public NativeRecyclerViewAdapter(ArrayList<TypedViewData> data, Context context){
        mData = data;
        mContext = context;
    }

    public void setOnToolItemClickListener(OnToolItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public class DicomTagHolder extends RecyclerView.ViewHolder{
        private TextView textID;
        private TextView textVR;
        private TextView textValue;
        private TextView textDesc;
        public DicomTagHolder(View view){
            super(view);
            textID = (TextView)view.findViewById(R.id.tag_id);
            textVR = (TextView)view.findViewById(R.id.tag_vr);
            textValue = (TextView)view.findViewById(R.id.tag_value);
            textDesc = (TextView)view.findViewById(R.id.tag_desc);
        }
    }
    public class DicomToolHolder extends RecyclerView.ViewHolder{
        private ImageView imageBorder;
        private ImageView imageTool;
        public DicomToolHolder(View view){
            super(view);
            imageBorder = (ImageView)view.findViewById(R.id.item_toolbar_border);
            imageTool = (ImageView)view.findViewById(R.id.item_toobar_img);
        }
        public ImageView getImageTool() {
            return imageTool;
        }

        public ImageView getImageBorder() {
            return imageBorder;
        }
    }
    //...

    @Override
    public int getItemViewType(int position) {
        if(mData.get(position).getType() == 0){
            return TYPE_DEFAULT;
        }else if (mData.get(position).getType() == 1){
            return TYPE_PHOTO;
        }else if(mData.get(position).getType() ==2){
            return TYPE_PAGER;
        }else if(mData.get(position).getType() == 3){
            return TYPE_DICOMTAG;
        }else if(mData.get(position).getType() == 4){
            return TYPE_TOOLBAR;
        }
        //...
        return TYPE_UNKNOWN;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == TYPE_DICOMTAG) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dicomtagitem, parent, false);
            DicomTagHolder VH = new DicomTagHolder(view);
            return VH;
        }
        if (viewType == TYPE_TOOLBAR){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dicomtoolbaritem, parent, false);
            DicomToolHolder VH = new DicomToolHolder(view);
            return VH;
        }
        return null;
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof DicomTagHolder) {
            String[] tagParts = mData.get(position).getModifyString().split(" ");
            ((DicomTagHolder) holder).textID.setText(tagParts[0]);
            ((DicomTagHolder) holder).textValue.setText(tagParts[2]);
            ((DicomTagHolder) holder).textVR.setText(tagParts[1]);
            ((DicomTagHolder) holder).textDesc.setText(tagParts[3]);
        }
        if (holder instanceof DicomToolHolder){
            switch (position){
                case 0:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.adjust);
                    break;
                }
                case 1:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.binary);
                    break;
                }
                case 2:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.crop);
                    break;
                }
                case 3:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.auto_play);
                    break;
                }
                case 4:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.contrast);
                    break;
                }
                case 5:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.value);
                    break;
                }
                case 6:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.drawing);
                    holder.itemView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((DicomViewerActivity)mContext).windowManager.setParentAlign(holder.itemView, ((DicomViewerActivity)mContext).windowDrawerSelect, FloatWindowManager.ALIGN_RIGHT);
                        }
                    });
                    break;
                }
                case 7:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.process);
                    holder.itemView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((DicomViewerActivity)mContext).windowManager.setParentAlign(holder.itemView, ((DicomViewerActivity)mContext).windowImageProcessView, FloatWindowManager.ALIGN_RIGHT);
                        }
                    });
                    break;
                }
                case 8:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.analysis);
                    holder.itemView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((DicomViewerActivity)mContext).windowManager.setParentAlign(holder.itemView, ((DicomViewerActivity)mContext).windowHistogramView, FloatWindowManager.ALIGN_RIGHT);
                        }
                    });
                    break;
                }
                case 9:{
                    ((DicomToolHolder)holder).imageTool.setImageResource(R.mipmap.save);
                    holder.itemView.post(new Runnable() {
                        @Override
                        public void run() {
                            ((DicomViewerActivity)mContext).windowManager.setParentAlign(holder.itemView, ((DicomViewerActivity)mContext).windowImageSaveView, FloatWindowManager.ALIGN_RIGHT);
                        }
                    });
                    break;
                }
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((DicomToolHolder)holder).imageBorder.getVisibility() == View.VISIBLE){
                        ((DicomToolHolder)holder).imageBorder.setVisibility(View.GONE);
                    }else{
                        ((DicomToolHolder)holder).imageBorder.setVisibility(View.VISIBLE);
                    }
                    if (onItemClickListener!=null) {
                        onItemClickListener.OnClick(holder, position);
                    }
                }
            });
        }
    }

    public int getItemCount(){
        return mData.size();
    }

}
