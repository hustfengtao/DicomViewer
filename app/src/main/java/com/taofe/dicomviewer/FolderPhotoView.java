package com.taofe.dicomviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.dcm4che3.android.Raster;
import org.dcm4che3.android.RasterUtil;
import org.dcm4che3.android.imageio.dicom.DicomImageReader;
import org.dcm4che3.data.Tag;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class FolderPhotoView extends FolderLinearLayout {
    private boolean isActive = false;
    private boolean windowAdjustMode = false;
    private boolean windowBinaryMode = false;
    private boolean windowShowTagsMode = false;
    private boolean keepSync = false;
    private int windowWidth = 4096;
    private int windowCenter = 2048;
    private int currentPos = 0;
    private int posX = 0;
    private int posY = 0;
    private int mCount = 0;
    private int maxValue = 0;
    private int minValue = 0;

    private boolean process_horizontal = false;
    private boolean process_vertical = false;
    private boolean process_filter = false;
    private boolean process_invert = false;

    private DicomImageReader reader = new DicomImageReader();
    private Raster raster = null;
    private Bitmap bitmap = null;
    private PhotoView photoView = null;
    private CanvasView photoCanvas = null;
    private TextView textPatientName = null;
    private TextView textPatientID = null;
    private TextView textPatientSex = null;
    private TextView textInstanceNum = null;
    private TextView textStudyDate = null;
    private TextView textCounts = null;
    private TextView textWLInfo = null;
    private SeriesList list = null;
    private String ds = "";
    private String path = "";
    private ArrayList<FolderPhotoView> friendView = new ArrayList<FolderPhotoView>();
    private Matrix matrix = new Matrix();
    private Matrix invmatrix = new Matrix();
    private short[] shapeData = null;
    private OnShapeDataChangeListener onShapeDataChangeListener = null;

    public FolderPhotoView(Context context) {
        this(context, null);
    }

    public FolderPhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Parcel parcel = Parcel.obtain();
        list = SeriesList.CREATOR.createFromParcel(parcel);
    }

    public void addFriendView(FolderPhotoView view){
        friendView.add(view);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect rtView = new Rect();
        getGlobalVisibleRect(rtView);
        if (rtView.contains((int) ev.getRawX(), (int) ev.getRawY())){
            setActive(true);
            if (friendView!=null) {
                for (FolderPhotoView view : friendView) {
                    view.setActive(false);
                }
            }
        }
        if(!windowShowTagsMode) {
            if (windowAdjustMode || windowBinaryMode) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        posX = (int) ev.getX();
                        posY = (int) ev.getY();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        int newPosX = (int) ev.getX();
                        int newPosY = (int) ev.getY();
                        int disX = newPosX - posX;
                        int disY = newPosY - posY;

                        if (!windowBinaryMode) {
                            windowWidth += disX;
                            windowCenter += disY;
                        } else if (!windowAdjustMode){
                            windowWidth = 1;
                            windowCenter += disY;
                        }
                        try {
                            setRaster(raster, currentPos);
                            if (friendView!=null){
                                for (FolderPhotoView view : friendView) {
                                    view.setWindowWidthCenter(windowWidth, windowCenter);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        posX = newPosX;
                        posY = newPosY;
                        break;
                    }
                }
                return true;
            }else{
                if (friendView!=null) {
                    for (FolderPhotoView view : friendView) {
                        if (view.getPhotoView() != null) {
                            if (keepSync) {
                                view.getPhotoView().dispatchTouchEvent(ev);
                            }
                        }
                    }
                }
                if (photoView!=null) {
                    matrix = photoView.getDisplayMatrix();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed){
            if (photoView!=null && photoCanvas!=null) {
                matrix = photoView.getDisplayMatrix();
                matrix.invert(invmatrix);
                RectF rt = photoView.getDisplayRect();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) photoCanvas.getLayoutParams();
                layoutParams.setMargins((int) ((photoView.getWidth() - rt.width()) / 2), (int) ((photoView.getHeight() - rt.height()) / 2), (int) ((photoView.getWidth() - rt.width()) / 2), (int) ((photoView.getHeight() - rt.height()) / 2));
            }
        }
        super.onLayout(changed, l, t, r, b);
    }

    public void setProcess_horizontal(int progress){
        process_horizontal = !process_horizontal;
        horizontal_tranform(progress);
    }
    private void horizontal_tranform(int progress){
        Raster raster = RasterProcessUtil.MirrorH(this.raster, this.raster.getWidth(), this.raster.getHeight());
        setRaster(raster, progress);
    }

    public void setProcess_vertical(int progress){
        process_vertical = !process_vertical;
        vertical_transform(progress);
    }
    private void vertical_transform(int progress){
        Raster raster = RasterProcessUtil.MirrorV(this.raster, this.raster.getWidth(), this.raster.getHeight());
        setRaster(raster, progress);
    }

    public void setProcess_filter(int progress){
        process_filter = !process_filter;
        filter_transform(progress);
    }
    private void filter_transform(int progress){
        Raster raster = RasterProcessUtil.Filter(this.raster, this.raster.getWidth(), this.raster.getHeight());
        setRaster(raster, progress);
    }

    public void setProcess_invert(int progress, int maxValue, int minValue){
        process_invert = !process_invert;
        this.maxValue = maxValue;
        this.minValue = minValue;
        invert_transform(progress);
    }
    private void invert_transform(int progress){
        Raster raster = RasterProcessUtil.Invert(this.raster, this.raster.getWidth(), this.raster.getHeight(),maxValue,minValue);
        setRaster(raster, progress);
    }

    public boolean isProcess_filter() {
        return process_filter;
    }

    public boolean isProcess_horizontal() { return process_horizontal; }

    public boolean isProcess_vertical() {
        return process_vertical;
    }

    public boolean isProcess_invert() {
        return process_invert;
    }

    public void setWindowAdjustMode(boolean adjustMode){
        windowAdjustMode = adjustMode;
    }

    public void setWindowBinaryMode(boolean binaryMode){
        windowBinaryMode = binaryMode;
    }

    public void setWindowShowTagsMode(boolean showTagsMode){
        windowShowTagsMode = showTagsMode;
    }

    public void setRotationTo(int degree){
        if (photoView!=null){
            photoView.setRotationTo(degree);
        }
    }

    public void setReader(DicomImageReader reader){
        this.reader = reader;
    }

    public Raster getRaster(){
        return raster;
    }

    public void setSeriesList(SeriesList list){
        this.list = list;
    }

    public SeriesList getSeriesList(){
        return list;
    }

    public void setKeepSync(boolean isKeep){
        keepSync = isKeep;
    }

    public void setActive(boolean active){
        isActive = active;
        super.drawBorder(active);
    }

    public boolean isActive() {
        return isActive;
    }

    public String getCurrentPath(){
        return path;
    }

    public Bitmap getCurrentBitmap(){
        return bitmap;
    }

    public PhotoView getPhotoView(){
        return photoView;
    }

    public void clearDcm(){
        list.clear();
        raster = new Raster(reader.getWidth(), reader.getHeight(), 1);

        if (textPatientName!=null){
            textPatientName.setVisibility(GONE);
        }
        if (textPatientID!=null){
            textPatientID.setVisibility(GONE);
        }
        if (textPatientSex!=null){
            textPatientSex.setVisibility(GONE);
        }
        if (textInstanceNum!=null){
            textInstanceNum.setVisibility(GONE);
        }
        if (textStudyDate!=null){
            textStudyDate.setVisibility(GONE);
        }
        if (textCounts!=null){
            textCounts.setVisibility(GONE);
        }
        if (textWLInfo!=null){
            textWLInfo.setVisibility(GONE);
        }
    }

    public void setDcm(String path, int nPos)throws Exception{
        this.path = path;
        File file = new File(path);
        reader.open(file);
        //if window has defined in TAG
        windowWidth = (int)reader.getAttributes().getFloat(Tag.WindowWidth, windowWidth);
        windowCenter = (int)reader.getAttributes().getFloat(Tag.WindowCenter, windowCenter);
        raster = reader.readRaster(0);
        currentPos = nPos;
        setRaster(raster, nPos);
        if (process_horizontal){
            horizontal_tranform(nPos);
        }
        if (process_vertical){
            vertical_transform(nPos);
        }
        if (process_filter){
            filter_transform(nPos);
        }
        if (process_invert){
            invert_transform(nPos);
        }
    }

    public int[] getWindowWidthCenter(){
        int[] arr = new int[2];
        arr[0] = windowWidth;
        arr[1] = windowCenter;
        return arr;
    }

    public void setWindowWidthCenter(int ww, int wc){
        windowWidth = ww;
        windowCenter = wc;
        setRaster(raster, currentPos);
    }

    public void setRaster(Raster raster, int nPos){
        this.raster =raster;
        Raster destRaster = RasterProcessUtil.applyWindowCenter(reader, raster, windowWidth, windowCenter, raster.getWidth(), raster.getHeight());
        bitmap = RasterUtil.rasterToBitmap(destRaster);
        setBitmap(bitmap);
        setPhotoInfo(photoView.getScale(), reader, nPos+1);
    }

    public void setBitmap(Bitmap bitmap){
        if (photoView == null) {
            photoView = (PhotoView) findViewById(R.id.dicomviewer_image);
            photoView.setOnScaleChangeListener(new PhotoViewAttacher.OnScaleChangeListener() {
                @Override
                public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                    setExtraInfo(windowWidth, windowCenter, photoView.getScale());
                    if (photoView!=null) {
                        matrix = photoView.getDisplayMatrix();
                    }
                }
            });

            photoCanvas = (CanvasView)findViewById(R.id.dicomviewer_canvas);
            photoCanvas.setVisibility(GONE);
            photoCanvas.setOnDrawerChangeListener(new OnDrawerChangeListener() {
                @Override
                public void onDrawerChange() {
                    Shape shape = photoCanvas.getShape();
                    Rect rt = new Rect();
                    photoCanvas.getLocalVisibleRect(rt);
                    float rateX = (float)raster.getWidth()/rt.width();
                    float rateY = (float)raster.getHeight()/rt.height();
                    if (shape!=null) {
                        switch (shape.getType()){
                            case Shape.SHAPE_RECTANGLE:{
                                int rasterHeight = (int)((shape.getPtEnd().y - shape.getPtStart().y)*rateY);
                                int rasterWidth = (int)((shape.getPtEnd().x - shape.getPtStart().x)*rateX);
                                if (rasterHeight>0 && rasterWidth>0) {
                                    int rasterX = (int)(shape.getPtStart().x* rateX);
                                    int rasterY = (int)(shape.getPtStart().y*rateY);
                                    shapeData = new short[rasterHeight * rasterWidth];
                                    for (int i = 0; i != rasterWidth; i++) {
                                        for (int j = 0; j != rasterHeight; j++) {
                                            shapeData[j * rasterWidth + i] = raster.getShortData()[(j + rasterY) * raster.getWidth() + rasterX + i];
                                        }
                                    }
                                }
                                break;
                            }
                            case Shape.SHAPE_OVAL:{
                                int rasterHeight = (int)((shape.getPtEnd().y - shape.getPtStart().y)*rateY);
                                int rasterWidth = (int)((shape.getPtEnd().x - shape.getPtStart().x)*rateX);
                                if (rasterHeight>0 && rasterWidth>0) {
                                    int rasterX = (int)(shape.getPtStart().x* rateX);
                                    int rasterY = (int)(shape.getPtStart().y*rateY);
                                    shapeData = new short[rasterHeight * rasterWidth];
                                    float a = rasterWidth * rasterWidth / 4;
                                    float b = rasterHeight * rasterHeight / 4;
                                    for (int i = 0; i != rasterWidth; i++) {
                                        for (int j = 0; j != rasterHeight; j++) {
                                            float x = i - rasterWidth / 2;
                                            float y = j - rasterHeight / 2;
                                            float re = (x * x) / a + (y * y) / b;
                                            if (re >= 1) {
                                                shapeData[j * rasterWidth + i] = raster.getShortData()[(j + rasterY) * raster.getWidth() + rasterX + i];
                                            } else {
                                                shapeData[j * rasterWidth + i] = 0;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            case Shape.SHAPE_LINE:{
                                int rasterX = (int)(shape.getPtStart().x* rateX);
                                int rasterY = (int)(shape.getPtStart().y* rateY);
                                int rasterHeight = (int)((shape.getPtEnd().y - shape.getPtStart().y)*rateY);
                                int rasterWidth = (int)((shape.getPtEnd().x - shape.getPtStart().x)*rateX);
                                int oldX = rasterX;
                                int oldY = rasterY;
                                int oldH = rasterHeight;
                                int oldW = rasterWidth;
                                if (rasterHeight <0){
                                    rasterY = (int)(shape.getPtEnd().y* rateY);
                                    rasterHeight = -rasterHeight;
                                }
                                if (rasterWidth <0){
                                    rasterX = (int)(shape.getPtEnd().x* rateX);
                                    rasterWidth = -rasterWidth;
                                }
                                ArrayList<Short> list = new ArrayList<Short>();
                                for (int i=rasterX;i!=rasterWidth+rasterX;i++) {
                                    for (int j = rasterY; j != rasterHeight + rasterY; j++) {
                                        float a = (float)(j - oldY) / oldH;
                                        float b = (float)(i - oldX) / oldW;
                                        if (Math.abs(b - a) <= 0.01){
                                            list.add(raster.getShortData()[j * raster.getWidth() +  i]);
                                        }
                                    }
                                }
                                Log.d("dicomviewer","point:" + list.size());
                                shapeData = new short[list.size()];
                                for (int i=0;i!=list.size();i++){
                                    shapeData[i] = list.get(i);
                                }
                                break;
                            }
                        }
                    }else{
                        shapeData = raster.getShortData();
                    }
                    if (onShapeDataChangeListener!=null){
                        onShapeDataChangeListener.onChange(shapeData);
                    }
                }
            });

            photoView.setImageBitmap(bitmap);

            matrix = photoView.getDisplayMatrix();
            matrix.invert(invmatrix);

            RectF rt = photoView.getDisplayRect();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) photoCanvas.getLayoutParams();
            layoutParams.setMargins((int) ((photoView.getWidth() - rt.width()) / 2), (int) ((photoView.getHeight() - rt.height()) / 2), (int) ((photoView.getWidth() - rt.width()) / 2), (int) ((photoView.getHeight() - rt.height()) / 2));
        }else{
            photoView.setImageBitmap(bitmap);

            Matrix fmatrix = new Matrix(invmatrix);
            fmatrix.postConcat(matrix);
            photoView.setDisplayMatrix(fmatrix);
        }
    }

    public void setOnShapeDataChangeListener(OnShapeDataChangeListener onShapeDataChangeListener){
        this.onShapeDataChangeListener = onShapeDataChangeListener;
    }

    public void showCanvas(boolean show){
        if (show){
            if (photoCanvas!=null) {
                photoCanvas.setVisibility(VISIBLE);
            }
        }else{
            if (photoCanvas!=null) {
                photoCanvas.setVisibility(GONE);
            }
        }
    }

    public void addDrawer(int type){
        if (photoCanvas!=null) {
            photoCanvas.addDrawer(type);
        }
    }
    public void deleteDrawer(){
        if (photoCanvas!=null) {
            photoCanvas.deleteDrawer();
        }
    }

    public boolean hasShape(){
        return photoCanvas.getShape()!=null;
    }

    public short[] getShapeData(){
        return shapeData;
    }

    public int getShapeType(){
        switch (photoCanvas.getShape().getType()){
            case Shape.SHAPE_LINE:{
                return HistogramImageView.TYPE_OPEN;
            }
            case Shape.SHAPE_OVAL:
            case Shape.SHAPE_RECTANGLE:{
                return HistogramImageView.TYPE_CLOSE;
            }
            default:
                return HistogramImageView.TYPE_CLOSE;
        }
    }

    public void setPhotoInfo(float scale, DicomImageReader reader, int pos) {
        textPatientName = ((TextView)findViewById(R.id.tag_patientname));
        if (textPatientName!=null) {
            textPatientName.setVisibility(VISIBLE);
            textPatientName.setText("PatinetName:" + reader.getAttributes().getString(Tag.PatientName, "Patient"));
        }
        textPatientID = ((TextView) findViewById(R.id.tag_patientid));
        if (textPatientID!=null) {
            textPatientID.setVisibility(VISIBLE);
            textPatientID.setText("PatinetID:" + reader.getAttributes().getString(Tag.PatientID, "0001"));
        }
        textPatientSex = ((TextView) findViewById(R.id.tag_patientsex));
        if (textPatientSex!=null) {
            textPatientSex.setVisibility(VISIBLE);
            textPatientSex.setText("PatinetSex:" + reader.getAttributes().getString(Tag.PatientSex, "male"));
        }
        textInstanceNum = ((TextView) findViewById(R.id.tag_instancenumber));
        if (textInstanceNum!=null) {
            textInstanceNum.setVisibility(VISIBLE);
            textInstanceNum.setText("InstanceNumber:" + reader.getAttributes().getString(Tag.InstanceNumber, "1"));
        }
        textStudyDate = ((TextView) findViewById(R.id.tag_studyadate));
        if (textStudyDate!=null) {
            textStudyDate.setVisibility(VISIBLE);
            textStudyDate.setText("StudyDate:" + reader.getAttributes().getString(Tag.StudyDate, "1993-03-14"));
        }
        textCounts = ((TextView) findViewById(R.id.info_series_counts));
        if (textCounts!=null) {
            textCounts.setVisibility(VISIBLE);
            textCounts.setText("[" + String.valueOf(pos) + "/" + String.valueOf(list.size()) + "]");
        }

        ds = reader.getAttributes().toString();
        setExtraInfo(windowWidth, windowCenter, scale);
    }

    public int getCount(){
        if (list != null){
            return list.size();
        }else{
            return mCount;
        }
    }

    public void setCount(int count){
        mCount = count;
    }

    public String getTagInfo(){
        return ds;
    }

    public void setExtraInfo(int ww, int wc, float scale){
        NumberFormat ddf1= NumberFormat.getNumberInstance() ;
        ddf1.setMaximumFractionDigits(2);
        String zoom = ddf1.format(scale);
        textWLInfo =  (TextView)findViewById(R.id.info_window_wl_zoom);
        if (textWLInfo!=null) {
            textWLInfo.setVisibility(VISIBLE);
            textWLInfo.setText("W:" + String.valueOf(ww) + "/L:" + String.valueOf(wc) + "/Z:" + zoom);
        }
    }
}
