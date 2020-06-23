package com.taofe.dicomviewer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import org.dcm4che3.android.Raster;
import org.dcm4che3.data.Tag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import org.dcm4che3.android.RasterUtil;
import org.dcm4che3.android.imageio.dicom.DicomImageReader;

import static com.taofe.dicomviewer.ProgressHandler.HIDE_PROGRESS;
import static com.taofe.dicomviewer.ProgressHandler.MAX_PROGRSS;
import static com.taofe.dicomviewer.ProgressHandler.SHOW_PROGRESS;
import static com.taofe.dicomviewer.ProgressHandler.UPDATE_PROGRESS;

public class DicomViewerActivity extends AppCompatActivity{
    final static String TAG = "Dicomviewer";
    final static String STATE_LIST = "seriesList";
    final static String STATE_SELECT = "selectitem";
    final static String STATE_SELECT2 = "selectitem2";
    final static String STATE_MAX = "verticalMax";
    final static String STATE_PROGRESS = "verticalprogress";
    final static String STATE_THUMBBAR = "thumbexpand";

    public  ArrayList<SeriesList> seriesList = new ArrayList<SeriesList>();
    public  int thumbSelectedItem = -1;
    public  int thumbSelectedItem2 = -1;
    public  int verticalProgress = -1;
    public  int verticalMax = -1;
    public  boolean thumbBarExpend = false;

    private boolean windowShowTagsMode = false;
    private boolean windownFullScreenMode = false;

    private int maxValue = 0;
    private int minValue = 0;

    private TextView toggleThumb;
    private TextView toggleTags;
    private TextView toggleTools;

    private View photoView;
    public  ArrayList<FolderPhotoView> dicomContainerlist = new ArrayList<FolderPhotoView>();
    public  VerticalSeekBar verticalSeekBar;
    public  View verticalBarContainer;

    private View tagRVContainer;
    private RecyclerView tagRecycleView;
    private View tagItem;
    private ArrayList<TypedViewData> tagData = new ArrayList<TypedViewData>();

    private FolderLinearLayout thumbBar;
    private ImageView clearThumb;
    public  ViewPager thumbViewpager;
    public  NativeViewPagerDicomAdapter adapterThumb;

    public  RecyclerView toolbarRecycleView;
    private View toolbarItem;
    private ArrayList<TypedViewData> toolbarData = new ArrayList<TypedViewData>();

    public FloatWindowManager windowManager;
    public View windowHistogramView;
    public View windowImageProcessView;
    public View windowImageSaveView;
    public View windowCTValue;
    public View windowDrawerSelect;
    //windowHistogram
    private HistogramImageView histogramView;
    private TextView HistTextMin;
    private TextView HistTextMax;
    //windowAutoPlay
    private Timer timer = new Timer();
    private TimerTask autoPlayTask = null;
    //windowAdjust
    private boolean windowAdjustMode = false;
    //windowBinary
    private boolean windowBinaryMode = false;
    //windowProcess
    private SeekBar dicomprocess_rotate;
    private View dicomprocess_mirror_horizontal;
    private View dicomprocess_mirror_vertical;
    private View dicomprocess_filter;
    private View dicomprocess_invert;
    //windowCrop
    //windowDrawerSelect
    private View drawer_line;
    private View drawer_rectangle;
    private View drawer_oval;
    private View drawer_erase;
    //windowShowCTValue
    private TextView textCTValue;
    private InnerTouchListener onTouchListener;
    //windowSave
    private Spinner saveBehavior;
    private Spinner saveType;
    private CheckBox saveOption;
    private TextView saveConfirm;
    private ProgressBar saveProgress;
    //about
    AlertDialog aboutDialog = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
           seriesList = savedInstanceState.getParcelableArrayList(STATE_LIST);
           thumbSelectedItem = savedInstanceState.getInt(STATE_SELECT, thumbSelectedItem);
           thumbSelectedItem2 = savedInstanceState.getInt(STATE_SELECT2, thumbSelectedItem2);
           verticalProgress = savedInstanceState.getInt(STATE_PROGRESS, verticalProgress);
           verticalMax = savedInstanceState.getInt(STATE_MAX,verticalMax);
           thumbBarExpend = savedInstanceState.getBoolean(STATE_THUMBBAR, false);
        }
        setContentView(R.layout.activity_dicom_viewer);
        InitView();
        if (savedInstanceState!=null) {
            thumbViewpager.post(new Runnable() {
                @Override
                public void run() {
                    adapterThumb.notifyDataSetChanged();
                    adapterThumb.setItemBorder(thumbSelectedItem);
                    thumbBar.setExpand(thumbBarExpend);
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.isActive()) {
                            if (thumbSelectedItem >=0 ) {
                                view.setSeriesList(seriesList.get(thumbSelectedItem));
                            }
                        }else{
                            if (thumbSelectedItem2 >= 0) {
                                view.setSeriesList(seriesList.get(thumbSelectedItem2));
                            }
                        }
                    }
                    if (verticalMax >= 0) {
                        verticalSeekBar.setMax(verticalMax);
                    }
                    if (verticalProgress >= 0) {
                        verticalSeekBar.setProgress(verticalProgress);
                    }
                }
            });
        }
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);

        //open from other app
        if (getIntent().getData()!= null) {
            thumbViewpager.post(new Runnable() {
                @Override
                public void run() {
                    onActivityResult(1, RESULT_OK, getIntent());
                    getIntent().setData(null);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_LIST, seriesList);
        if (verticalSeekBar!=null){
            outState.putInt(STATE_PROGRESS, verticalSeekBar.getProgress());
            outState.putInt(STATE_MAX,verticalSeekBar.getMax());
        }
        if (thumbBar!=null){
            outState.putBoolean(STATE_THUMBBAR, thumbBar.isExpand());
        }
        if (seriesList!=null){
            for (FolderPhotoView view:dicomContainerlist){
                if (view.isActive()) {
                    if (!view.getSeriesList().isEmpty()) {
                        for (int i = 0; i != seriesList.size(); i++) {
                            if (view.getSeriesList() == seriesList.get(i)) {
                                outState.putInt(STATE_SELECT, i);
                                break;
                            }
                        }
                    }
                }else{
                    if (!view.getSeriesList().isEmpty()) {
                        for (int i = 0; i != seriesList.size(); i++) {
                            if (view.getSeriesList() == seriesList.get(i)){
                                outState.putInt(STATE_SELECT2, i);
                                break;
                            }
                        }
                    }
                }
            }
        }
        super.onSaveInstanceState(outState);
    }

    private void checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 24) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {permission};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        windowManager.removeAllViews(true);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        windowManager.hideAllFloatView();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void InitView(){
        //float window of histogram
        windowManager = new FloatWindowManager(this);
        //top toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.dicomviewer_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //Tag recycleview
        tagRVContainer = findViewById(R.id.dicomviewer_rv_container);
        tagItem = getLayoutInflater().inflate(R.layout.rv_dicomtagitem, null);
        tagRecycleView = (RecyclerView)findViewById(R.id.dicomviewer_rv_tags);
        tagRecycleView.setLayoutManager(new LinearLayoutManager(this));
        NativeRecyclerViewAdapter adapterTag = new NativeRecyclerViewAdapter(tagData, this);
        tagRecycleView.setAdapter(adapterTag);

        photoView = findViewById(R.id.dicomviewer_photoview);
        FolderPhotoView viewContainer0 = (FolderPhotoView)findViewById(R.id.dicomviewer_image_container1);
        FolderPhotoView viewContainer1 = (FolderPhotoView)findViewById(R.id.dicomviewer_image_container2);
        viewContainer0.addFriendView(viewContainer1);
        viewContainer1.addFriendView(viewContainer0);
        viewContainer0.setKeepSync(true);
        viewContainer1.setKeepSync(true);
        viewContainer0.setActive(true);
        dicomContainerlist.add(viewContainer0);
        dicomContainerlist.add(viewContainer1);
        viewContainer0.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                dicomContainerlist.get(1).setMaxHeight(photoView.getHeight()/2);
                dicomContainerlist.get(1).setMinHeight(4);
            }
        });
        viewContainer1.setOnAnimeUpdateListener(new OnAnimeUpdateListener() {
            @Override
            public void onUpdate() {
            }
            public void onFinish(){
                if (windowCTValue.getVisibility() == View.VISIBLE ){
                    for (final FolderPhotoView view:dicomContainerlist) {
                        if (view.isActive()) {
                            Rect rt = new Rect();
                            view.getGlobalVisibleRect(rt);
                            if (onTouchListener != null) {
                                onTouchListener.setRelativePosition(rt.width() / 2, rt.height() / 2);
                            }
                            break;
                        }
                    }
                }
            }
        });
        for (final FolderPhotoView view:dicomContainerlist){
            view.setOnShapeDataChangeListener(new OnShapeDataChangeListener() {
                @Override
                public void onChange(short[] data) {
                    if (view.isActive()){
                        setHistogramData(view);
                    }
                }
            });
        }

        //right seekbar to control dcm sequence
        verticalBarContainer = findViewById(R.id.dicomviewer_seekbar_container);
        verticalBarContainer.setVisibility(View.INVISIBLE);
        verticalSeekBar = (VerticalSeekBar)findViewById(R.id.dicomviewer_seekbar);
        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    for (FolderPhotoView view:dicomContainerlist){
                        if (!view.getSeriesList().isEmpty()) {
                            if (progress <= view.getSeriesList().size() - 1) {
                                view.setDcm(view.getSeriesList().get(progress), progress);
                            } else {
                                view.setDcm(view.getSeriesList().get(view.getSeriesList().size() - 1), view.getSeriesList().size() - 1);
                            }
                        }
                        if (view.isActive()){
                            //prepare histogram
                            resetHistogramView();
                            setHistogramData(view);
                            NotifyTagListChange(view.getTagInfo());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        //contianer of thumbpager
        thumbBar = (FolderLinearLayout)findViewById(R.id.dicomviewer_thumbbar);
        thumbBar.setOnAnimeUpdateListener(new OnAnimeUpdateListener() {
            @Override
            public void onUpdate() {
                if (dicomContainerlist.get(1).isExpand()) {
                    dicomContainerlist.get(1).setViewHeight(photoView.getHeight() / 2);
                }else{
                    dicomContainerlist.get(1).setViewHeight(4);
                }
            }
            public void onFinish(){
                if (windowCTValue.getVisibility() == View.VISIBLE ){
                    for (final FolderPhotoView view:dicomContainerlist) {
                        if (view.isActive()) {
                            Rect rt = new Rect();
                            view.getGlobalVisibleRect(rt);
                            if (onTouchListener != null) {
                                onTouchListener.setRelativePosition(rt.width() / 2, rt.height() / 2);
                            }
                            break;
                        }
                    }
                }
            }
        });
        //bottom thumbpager
        thumbViewpager = (ViewPager)findViewById(R.id.dicomviewer_pager_thumb);
        adapterThumb = new NativeViewPagerDicomAdapter(this,seriesList);
        thumbViewpager.setAdapter(adapterThumb);
        thumbViewpager.setOffscreenPageLimit(7);
        //bottom thumbpager clear btn
        clearThumb = (ImageView)findViewById(R.id.dicomviewer_empty);
        clearThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seriesList.clear();
                adapterThumb.notifyDataSetChanged();

                for (FolderPhotoView view:dicomContainerlist){
                    view.clearDcm();
                    verticalSeekBar.setMax(0);
                }
                resetHistogramView();
                verticalBarContainer.setVisibility(View.INVISIBLE);
            }
        });
        //toggle thumbpager btn
        toggleThumb = (TextView)findViewById(R.id.dicomviewer_toggle_thumbbar);
        toggleThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thumbBar.toggleExpand();
            }
        });
        toggleTags = (TextView)findViewById(R.id.dicomviewer_toggle_tags);
        toggleTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (seriesList.isEmpty()){
                    Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                }else {
                    if (tagRVContainer.getVisibility() == View.VISIBLE) {
                        windowShowTagsMode = false;
                        tagRVContainer.setVisibility(View.GONE);
                    } else {
                        windowShowTagsMode = true;
                        tagRVContainer.setVisibility(View.VISIBLE);
                    }
                    for (FolderPhotoView view:dicomContainerlist){
                        view.setWindowShowTagsMode(windowShowTagsMode);
                        if (view.isActive()){
                            NotifyTagListChange(view.getTagInfo());
                            break;
                        }
                    }
                }
            }
        });
        toggleTools = (TextView)findViewById(R.id.dicomviewer_toggle_toolbar);
        toggleTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolbarRecycleView.getVisibility() == View.GONE){
                    toolbarRecycleView.setVisibility(View.VISIBLE);
                }else{
                    toolbarRecycleView.setVisibility(View.GONE);
                }
            }
        });
        toolbarRecycleView = (RecyclerView)findViewById(R.id.dicomviewer_rv_toolbar);
        toolbarRecycleView.setLayoutManager(new LinearLayoutManager(this));
        toolbarItem = getLayoutInflater().inflate(R.layout.rv_dicomtoolbaritem, null);
        for (int i=0;i!=10;i++) {
            toolbarData.add(new TypedViewData(toolbarItem, 4, null, null, null, null));
        }
        NativeRecyclerViewAdapter adapterTool= new NativeRecyclerViewAdapter(toolbarData, this);
        toolbarRecycleView.setAdapter(adapterTool);
        adapterTool.setOnToolItemClickListener(new OnToolItemClickListener() {
            @Override
            public void OnClick(RecyclerView.ViewHolder holder, int position){
                switch (position) {
                    case 0: {
                        //adjust
                        if (!seriesList.isEmpty()) {
                            if (((NativeRecyclerViewAdapter.DicomToolHolder)holder).getImageBorder().getVisibility() == View.VISIBLE) {
                                windowAdjustMode = true;
                                Toast.makeText(DicomViewerActivity.this, "touch screen to adjust window",  Toast.LENGTH_SHORT).show();
                            } else {
                                windowAdjustMode = false;
                            }
                            for (FolderPhotoView view:dicomContainerlist){
                                view.setWindowAdjustMode(windowAdjustMode);
                            }
                        }else{
                            Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case 1: {
                        //binary
                        if (!seriesList.isEmpty()) {
                            if (((NativeRecyclerViewAdapter.DicomToolHolder) holder).getImageBorder().getVisibility() == View.VISIBLE) {
                                windowBinaryMode = true;
                                Toast.makeText(DicomViewerActivity.this, "touch screen to change threshold",  Toast.LENGTH_SHORT).show();
                            } else { ;
                                windowBinaryMode = false;
                            }
                            for (FolderPhotoView view:dicomContainerlist) {
                                view.setWindowBinaryMode(windowBinaryMode);
                            }
                        }else{
                            Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case 2: {
                        //crop
                        if (!seriesList.isEmpty()){
                            for (FolderPhotoView view:dicomContainerlist) {
                                if (view.isActive()){
                                    createImageFile(new File(view.getCurrentPath()), "crop", "jpg", view.getPhotoView().getVisibleRectangleBitmap());
                                    ((NativeRecyclerViewAdapter.DicomToolHolder)holder).getImageBorder().setVisibility(View.GONE);
                                    Toast.makeText(DicomViewerActivity.this, "crop image have been saved", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }else{
                            Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case 3: {
                        //autoplay
                        if (autoPlayTask!=null){
                            autoPlayTask.cancel();
                            autoPlayTask = null;
                            ((NativeRecyclerViewAdapter.DicomToolHolder)holder).getImageTool().setImageResource(R.mipmap.auto_play);
                            Toast.makeText(DicomViewerActivity.this, "Stop Play", Toast.LENGTH_SHORT).show();
                        }else{
                            if (!seriesList.isEmpty()) {
                                autoPlayTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        int count = verticalSeekBar.getProgress();
                                        count++;
                                        if (count > verticalSeekBar.getMax()) {
                                            count = 0;
                                        }
                                        verticalSeekBar.setProgress(count);
                                    }
                                };
                                timer.schedule(autoPlayTask, 0, 100);
                                ((NativeRecyclerViewAdapter.DicomToolHolder)holder).getImageTool().setImageResource(R.mipmap.pause);
                                Toast.makeText(DicomViewerActivity.this, "Start Play", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    case 4:{
                        dicomContainerlist.get(1).toggleExpand();
                        break;
                    }
                    case 5:{
                        windowManager.toggleView(windowCTValue);
                        for (final FolderPhotoView view:dicomContainerlist){
                            if (windowCTValue.getVisibility() == View.VISIBLE) {
                                if(view.getPhotoView()!=null) {
                                    view.getPhotoView().setScale(1.0f);
                                    view.getPhotoView().setZoomable(false);
                                }
                            }else{
                                if(view.getPhotoView()!=null) {
                                    view.getPhotoView().setZoomable(true);
                                }
                            }
                            if (view.isActive()){
                                Rect rt = new Rect();
                                view.getGlobalVisibleRect(rt);

                                onTouchListener = new InnerTouchListener(DicomViewerActivity.this, view, windowCTValue, windowManager.getLayoutParam(windowCTValue), windowManager.getWindowManager());
                                onTouchListener.setOnInnerTouchListener(new OnInnerTouchListener() {
                                    @Override
                                    public void onTouch(int posX, int posY, RectF rt) {
                                        if (!rt.isEmpty()) {
                                            int x = (int) (view.getRaster().getWidth() * (posX - rt.left) / rt.width());
                                            int y = (int) (view.getRaster().getHeight() * (posY - rt.top) / rt.height());

                                            int pos = y * view.getRaster().getWidth() + x;
                                            short[] data = view.getRaster().getShortData();
                                            int value = data[pos];
                                            textCTValue.setText(String.valueOf(value));
                                        }
                                    }
                                });
                                windowCTValue.setOnTouchListener(onTouchListener);
                                onTouchListener.setRelativePosition(rt.width()/2, rt.height()/2);
                            }
                        }
                        break;
                    }
                    case 6:{
                        windowManager.toggleView(windowDrawerSelect);
                        ModifyTouchListener onTouchListener = new ModifyTouchListener(windowManager.getLayoutParam(windowDrawerSelect), windowManager.getWindowManager());
                        windowDrawerSelect.setOnTouchListener(onTouchListener);
                        for (FolderPhotoView view:dicomContainerlist){
                            if (view.isActive()){
                                if (windowDrawerSelect.getVisibility() == View.VISIBLE){
                                    if (view.getPhotoView()!=null){
                                        view.getPhotoView().setScale(1.0f);
                                        view.getPhotoView().setZoomable(true);
                                    }
                                    view.showCanvas(true);
                                }else{
                                    if(view.getPhotoView()!=null) {
                                        view.getPhotoView().setZoomable(false);
                                    }
                                    view.showCanvas(false);
                                }
                            }
                        }
                        break;
                    }
                    case 7: {
                        //process
                        windowManager.toggleView(windowImageProcessView);
                        ModifyTouchListener onTouchListener = new ModifyTouchListener(windowManager.getLayoutParam(windowImageProcessView), windowManager.getWindowManager());
                        windowImageProcessView.setOnTouchListener(onTouchListener);
                        break;
                    }
                    case 8: {
                        //analyse
                        windowManager.toggleView(windowHistogramView);
                        ModifyTouchListener onTouchListener = new ModifyTouchListener(windowManager.getLayoutParam(windowHistogramView), windowManager.getWindowManager());
                        windowHistogramView.setOnTouchListener(onTouchListener);
                        break;
                    }
                    case 9: {
                        //save
                        windowManager.toggleView(windowImageSaveView);
                        ModifyTouchListener onTouchListener = new ModifyTouchListener(windowManager.getLayoutParam(windowImageSaveView), windowManager.getWindowManager());
                        windowImageSaveView.setOnTouchListener(onTouchListener);
                    }
                }
            }
        });
        //add processview to windowmanager
        windowImageProcessView = LayoutInflater.from(this).inflate(R.layout.dicomviewer_imageprocess, null);
        windowImageProcessView.setVisibility(View.GONE);
        windowManager.addView(windowImageProcessView, 0, 0, 180, 180);
        //process subview
        dicomprocess_rotate = (SeekBar)windowImageProcessView.findViewById(R.id.dicomprocess_rotate);
        dicomprocess_rotate.setMax(360);
        dicomprocess_rotate.setProgress(0);
        dicomprocess_rotate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!seriesList.isEmpty()) {
                    for (FolderPhotoView view : dicomContainerlist) {
                        if (view.isActive()) {
                            view.setRotationTo(progress);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onStartTrackingTouch (SeekBar seekBar){
            }
            @Override
            public void onStopTrackingTouch (SeekBar seekBar){
            }
        });
        dicomprocess_mirror_horizontal = windowImageProcessView.findViewById(R.id.dicomprocess_mirror_horizontal);
        dicomprocess_mirror_horizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!seriesList.isEmpty()) {
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.isActive()){
                            setHistogramData(view);
                            view.setProcess_horizontal(verticalSeekBar.getProgress());
                            break;
                        }
                    }
                }else{
                    Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dicomprocess_mirror_vertical = windowImageProcessView.findViewById(R.id.dicomprocess_mirror_vertical);
        dicomprocess_mirror_vertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!seriesList.isEmpty()) {
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.isActive()){
                            setHistogramData(view);
                            view.setProcess_vertical(verticalSeekBar.getProgress());
                            break;
                        }
                    }
                }else{
                    Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dicomprocess_filter = windowImageProcessView.findViewById(R.id.dicomprocess_filter);
        dicomprocess_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!seriesList.isEmpty()){
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.isActive()){
                            setHistogramData(view);
                            view.setProcess_filter(verticalSeekBar.getProgress());
                            break;
                        }
                    }
                }else{
                    Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dicomprocess_invert = windowImageProcessView.findViewById(R.id.dicomprocess_invert);
        dicomprocess_invert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!seriesList.isEmpty()){
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.isActive()){
                            setHistogramData(view);
                            view.setProcess_invert(verticalSeekBar.getProgress(),maxValue,minValue);
                            break;
                        }
                    }
                }else{
                    Toast.makeText(DicomViewerActivity.this, "No dicom image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //add histogramview to windowmanager
        windowHistogramView = LayoutInflater.from(this).inflate(R.layout.dicomviewer_histogram, null);
        windowHistogramView.setVisibility(View.GONE);
        windowManager.addView(windowHistogramView, 0, 0, 180, 180);
        //histogramsubview
        histogramView = (HistogramImageView)windowHistogramView.findViewById(R.id.histogram_view);
        histogramView.setOnHistogramListener(new OnHistogramListener() {
            @Override
            public void OnHistogramPrepare(int min, int max) {
                maxValue = max;
                minValue = min;
                HistTextMax.setText(String.valueOf(max));
                HistTextMin.setText(String.valueOf(min));
            }
        });
        HistTextMin = (TextView)windowHistogramView.findViewById(R.id.hist_min_value);
        HistTextMax = (TextView)windowHistogramView.findViewById(R.id.hist_max_value);
        //add imagesaveview to manager
        windowImageSaveView = LayoutInflater.from(this).inflate(R.layout.dicomviewer_save, null);
        windowImageSaveView.setVisibility(View.GONE);
        windowManager.addView(windowImageSaveView, 0, 0, 180, 180);
        saveBehavior = (Spinner)windowImageSaveView.findViewById(R.id.save_behavior);
        saveType = (Spinner)windowImageSaveView.findViewById(R.id.save_type);
        saveOption = (CheckBox)windowImageSaveView.findViewById(R.id.save_option);
        saveProgress = (ProgressBar)windowImageSaveView.findViewById(R.id.save_progress);
        saveConfirm = (TextView)windowImageSaveView.findViewById(R.id.save_confirm);
        saveConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String behavior = saveBehavior.getSelectedItem().toString();
                final String type = saveType.getSelectedItem().toString();
                final boolean applyProcess = saveOption.isChecked();
                final ProgressHandler handler = new ProgressHandler(saveProgress);
                handler.setOnProgressOverListener(new OnProgressOverListener() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(DicomViewerActivity.this, "image have been saved", Toast.LENGTH_SHORT).show();
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int nProgress = 0;
                        handler.sendMessage(SHOW_PROGRESS, 0);
                        for (FolderPhotoView view:dicomContainerlist){
                            if (view.isActive()){
                                if (behavior.indexOf("single")!=-1){
                                    handler.sendMessage(MAX_PROGRSS, 1);
                                    if (applyProcess) {
                                        createImageFile(new File(view.getCurrentPath()), type, type, view.getCurrentBitmap());
                                    }else{
                                        //
                                        DicomImageReader reader = new DicomImageReader();
                                        try {
                                            File file = new File(view.getCurrentPath());
                                            reader.open(file);
                                            Raster raster = RasterProcessUtil.applyWindowCenter(reader, reader.readRaster(0), view.getWindowWidthCenter()[0], view.getWindowWidthCenter()[1]);
                                            createImageFile(file, type, type, RasterUtil.rasterToBitmap(raster));
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                    nProgress++;
                                    handler.sendMessage(UPDATE_PROGRESS, nProgress);
                                }else{
                                    DicomImageReader reader = new DicomImageReader();
                                    handler.sendMessage(MAX_PROGRSS, view.getSeriesList().size());
                                    for (String path:view.getSeriesList().getArrayList()){
                                        try {
                                            if (applyProcess){
                                                File file = new File(path);
                                                reader.open(file);
                                                Raster raster = reader.readRaster(0);
                                                if (view.isProcess_horizontal()){
                                                    raster = RasterProcessUtil.MirrorH(raster, reader.getWidth(), reader.getHeight());
                                                }
                                                if (view.isProcess_vertical()){
                                                    raster = RasterProcessUtil.MirrorV(raster, reader.getWidth(), reader.getHeight());
                                                }
                                                if (view.isProcess_filter()){
                                                    raster = RasterProcessUtil.Filter(raster, reader.getWidth(), reader.getHeight());
                                                }
                                                if (view.isProcess_invert()){
                                                    raster = RasterProcessUtil.Invert(raster, reader.getWidth(), reader.getHeight(),HistogramUtil.sortList(raster.getShortData())[1], HistogramUtil.sortList(raster.getShortData())[0]);
                                                }
                                                Raster destRaster = RasterProcessUtil.applyWindowCenter(reader, raster, view.getWindowWidthCenter()[0], view.getWindowWidthCenter()[1]);
                                                createImageFile(file, type, type, RasterUtil.rasterToBitmap(destRaster));
                                            }else{
                                                File file = new File(path);
                                                reader.open(file);
                                                Raster raster = RasterProcessUtil.applyWindowCenter(reader, reader.readRaster(0), view.getWindowWidthCenter()[0], view.getWindowWidthCenter()[1]);
                                                createImageFile(file, type, type, RasterUtil.rasterToBitmap(raster));
                                            }
                                            nProgress++;
                                            handler.sendMessage(UPDATE_PROGRESS, nProgress);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                handler.sendMessage(HIDE_PROGRESS, 0);
                            }
                        }
                    }
                }).start();
            }
        });
        //show ct vaule
        windowCTValue = LayoutInflater.from(this).inflate(R.layout.dicomviewer_ct_value, null);
        windowCTValue.setVisibility(View.GONE);
        windowManager.addView(windowCTValue, 0, 0, 36, 30);
        textCTValue = (TextView)windowCTValue.findViewById(R.id.dicomviewer_ct_value);
        //drawer select
        windowDrawerSelect = LayoutInflater.from(this).inflate(R.layout.dicomviewer_drawer_select, null);
        windowDrawerSelect.setVisibility(View.GONE);
        windowManager.addView(windowDrawerSelect, 0, 0, 180, 180);
        drawer_line = windowDrawerSelect.findViewById(R.id.drawer_line);
        drawer_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (FolderPhotoView view:dicomContainerlist){
                    if (view.isActive()){
                        view.addDrawer(Shape.SHAPE_LINE);
                        break;
                    }
                };
            }
        });
        drawer_rectangle = windowDrawerSelect.findViewById(R.id.drawer_rectangle);
        drawer_rectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (FolderPhotoView view:dicomContainerlist){
                    if (view.isActive()){
                        view.addDrawer(Shape.SHAPE_RECTANGLE);
                        break;
                    }
                };
            }
        });
        drawer_oval = windowDrawerSelect.findViewById(R.id.drawer_oval);
        drawer_oval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (FolderPhotoView view:dicomContainerlist){
                    if (view.isActive()){
                        view.addDrawer(Shape.SHAPE_OVAL);
                        break;
                    }
                };
            }
        });
        drawer_erase = windowDrawerSelect.findViewById(R.id.drawer_erase);
        drawer_erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (FolderPhotoView view:dicomContainerlist){
                    if (view.isActive()){
                        view.deleteDrawer();
                        break;
                    }
                };
            }
        });
    }

    private void createImageFile(File file, String subDir, String type, Bitmap bitmap){
        String storageDir = file.getParent()+ "/" + subDir;
        File fileDir = new File(storageDir);
        if (!fileDir.exists()){
            fileDir.mkdir();
        }
        String filePath = storageDir + "/" + file.getName().replaceAll("dcm", type);
        try {
            File fileImage = new File(filePath);
            FileOutputStream out = new FileOutputStream(fileImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(fileImage);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyHideBorder(View v){
        int index = -1;
        if(v == windowCTValue){
            index = 5;
        }else if(v == windowDrawerSelect){
            index = 6;
        }else if(v == windowImageProcessView){
            index = 7;
        }else if(v == windowHistogramView){
            index = 8;
        }else if(v == windowImageSaveView){
            index = 9;
        }

        if (index>=0) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) toolbarRecycleView.getLayoutManager();
            int firstPosition = layoutManager.findFirstVisibleItemPosition();
            int lastPosition = layoutManager.findLastVisibleItemPosition();
            if (index - firstPosition >= 0&& index - lastPosition <= 0) {
                ((NativeRecyclerViewAdapter.DicomToolHolder) toolbarRecycleView.getChildViewHolder(layoutManager.findViewByPosition(index))).getImageBorder().setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dicomviewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.dicomviewer_option1:{
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }
                break;
            }
            case R.id.dicomviewer_option2:{
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 2);
                }
                break;
            }
            case R.id.dicom_mpr:{
                Intent intent = new Intent(DicomViewerActivity.this, DicomMPRActivity.class);
                for (FolderPhotoView view:dicomContainerlist){
                    if(view.isActive()&&!view.getSeriesList().isEmpty()){
                        intent.putExtra("serieslist", view.getSeriesList());
                        startActivity(intent);
                        break;
                    }
                }
                break;
            }
            case R.id.dicom_3d:{
                Intent intent = new Intent(DicomViewerActivity.this, Dicom3DActivity.class);
                for (FolderPhotoView view:dicomContainerlist){
                    if(view.isActive()){
                        if (!view.getSeriesList().isEmpty()) {
                            intent.putExtra("serieslist", view.getSeriesList());
                            intent.putExtra("windowCenter", view.getWindowWidthCenter()[1]);
                            startActivity(intent);
                        }
                        break;
                    }
                }
                break;
            }
            case R.id.dicomviewer_about:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(getLayoutInflater().inflate(R.layout.dicomviewer_about_dialog, null));
                aboutDialog = builder.create();
                aboutDialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 0:{
                if (Build.VERSION.SDK_INT>= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        Log.d(TAG, "overlay enable");
                    }else{
                        Log.d(TAG, "overlay disable");
                    }
                }
               break;
            }
            case 1:{
                if (resultCode == RESULT_OK){
                    String path = "";
                    //ArrayList<String> list = new ArrayList<String>();
                    Parcel parcel = Parcel.obtain();
                    SeriesList list = SeriesList.CREATOR.createFromParcel(parcel);
                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        path = PathFromUri.getPath(this, data.getData());
                    }else{
                        path = PathFromUri.getFPUriToPath(this, data.getData());
                    }
                    File file = new File(path);
                    if (!file.exists()){
                        Toast.makeText(this, "file not exist", Toast.LENGTH_SHORT).show();
                        return;
                    }else if(file.getName().indexOf("dcm") == -1){
                        Toast.makeText(this, "wrong format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    list.add(path);
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.isActive()){
                            view.setSeriesList(list);
                            break;
                        }
                    }
                    seriesList.add(list);
                    adapterThumb.notifyDataSetChanged();
                    adapterThumb.setItemBorder(thumbViewpager.getChildCount() -1);
                    thumbViewpager.setCurrentItem(thumbViewpager.getChildCount()-1);
                    int max = dicomContainerlist.get(0).getSeriesList().size();
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.getSeriesList().size() > max){
                            max = view.getSeriesList().size();
                        }
                    }
                    verticalSeekBar.setMax(max);
                    verticalSeekBar.setProgress(0);
                    verticalBarContainer.setVisibility(View.VISIBLE);
                    break;
                }
            }
            case 2:{
                if (resultCode == RESULT_OK){
                    String path = "";
                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        path = PathFromUri.getPath(this, data.getData());
                    }else{
                        path = PathFromUri.getFPUriToPath(this, data.getData());
                    }
                    String[] subStrings = path.split("/");
                    String topPath = "";
                    for(int i=0;i!=subStrings.length-1;i++){
                        topPath += "/"+ subStrings[i];
                    }
                    File file = new File(topPath);
                    File[] subFiles = file.listFiles();
                    ArrayList<DicomListItem> dicomlist = new ArrayList<DicomListItem>();
                    for (File subFile:subFiles){
                        if (subFile.getName().indexOf("dcm")!= -1){
                            dicomlist.add(new DicomListItem(subFile.getAbsolutePath(), getDicomInstanceNum(subFile.getAbsolutePath())));
                        }
                    }
                    if (dicomlist.isEmpty()){
                        Toast.makeText(this, "directory has no dicom file", Toast.LENGTH_SHORT).show();
                    }
                    DicomListSorter sorter = new DicomListSorter(dicomlist);
                    ArrayList<DicomListItem> sortList = sorter.getSortedListByID();

                    //ArrayList<String> list = new ArrayList<String>();
                    Parcel parcel = Parcel.obtain();
                    SeriesList list = SeriesList.CREATOR.createFromParcel(parcel);
                    for (int i=0;i!=sortList.size();i++){
                        list.add(sortList.get(i).getFilePath());
                    }
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.isActive()){
                            view.setSeriesList(list);
                            break;
                        }
                    }
                    seriesList.add(list);
                    adapterThumb.notifyDataSetChanged();
                    adapterThumb.setItemBorder(thumbViewpager.getChildCount() -1);
                    thumbViewpager.setCurrentItem(thumbViewpager.getChildCount()-1);
                    int max = dicomContainerlist.get(0).getSeriesList().size();
                    for (FolderPhotoView view:dicomContainerlist){
                        if (view.getSeriesList().size() > max){
                            max = view.getSeriesList().size();
                        }
                    }
                    verticalSeekBar.setMax(max);
                    verticalSeekBar.setProgress(0);
                    verticalBarContainer.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private int getDicomInstanceNum(String filepath){
        try {
            File file = new File(filepath);
            DicomImageReader reader = new DicomImageReader();
            reader.open(file);
            return reader.getAttributes().getInt(Tag.InstanceNumber, 0);
        }catch (Exception e){
            return -1;
        }
    }

    public void NotifyTagListChange(String  ds){
        parseTAG(ds);
        tagRecycleView.getAdapter().notifyDataSetChanged(); //recycleview refresh
    }

    private void parseTAG(String tags){
        tagData.clear();
        String[] Tags = tags.split("\n");
        for (String Tag:Tags){
            tagData.add(new TypedViewData(tagItem, 3, null, Tag, null, null));
        }
    }

    public void resetHistogramView(){
        histogramView.resetHistView();
    }

    private void setHistogramData(FolderPhotoView view){
        if (windowDrawerSelect.getVisibility() == View.VISIBLE && view.hasShape()){
            histogramView.setPixelData(view.getShapeData(), view.getShapeType());
        }else {
            histogramView.setPixelData(view.getRaster().getShortData(), HistogramImageView.TYPE_CLOSE);
        }
   }
}
