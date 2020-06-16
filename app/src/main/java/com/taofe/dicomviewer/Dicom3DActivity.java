package com.taofe.dicomviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ZoomControls;
import org.dcm4che3.android.Raster;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import org.dcm4che3.android.imageio.dicom.DicomImageReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class Dicom3DActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    private GLMatrixUtil glMatrixUtil;
    private CardView rotateBtn;
    private ZoomControls zoomControls;
    private boolean rotateFlag = false;
    private ArrayList<Slice> sliceList = new ArrayList<Slice>();
    private SeriesList seriesList = null;
    private int threshold;
    private DicomImageReader reader = new DicomImageReader();
    private Raster raster = null;
    private float posX, posY, dis = 0;
    private float rotateX, rotateY, rotateZ = 0;
    private float transX, transY, transZ = 0;
    private float scale = 1.0f;
    private float ratePixel = 1000;
    private float rateTrans = 500;
    private float rateRotate = 25;
    private float rateScale = 1000;
    private Timer timer = new Timer();
    private TimerTask autoHideTask = null;
    private boolean hideFlag = false;
    private ProgressHandler handler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3d);
        if(getIntent().getParcelableExtra("serieslist")!=null){
            seriesList = getIntent().getParcelableExtra("serieslist");
            threshold = getIntent().getIntExtra("windowCenter", 0);
            InitView();
        }
    }

    @Override
    protected void onPause() {
        if(glSurfaceView!=null) {
            glSurfaceView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(glSurfaceView!=null) {
            glSurfaceView.onResume();
        }
        super.onResume();
    }

    private void InitView(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.view3d_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        glSurfaceView = (GLSurfaceView)findViewById(R.id.view3d_glview);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            private float[] mMVPMatrix= new float[16];
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                GLES20.glClearColor(0, 0,0, 1);
                float z = -seriesList.size()/2;
                for (String s:seriesList.getArrayList()) {
                    File file = new File(s);
                    try {
                        reader.open(file);
                        //if window has defined in TAG
                        raster = reader.readRaster(0);
                        if (raster.getWidth() > seriesList.size()){
                            ratePixel = raster.getWidth()/2;
                        }else{
                            ratePixel = seriesList.size()/2;
                        }
                        short[] data = raster.getShortData();
                        for (int i = 0; i != raster.getWidth(); i++) {
                            for (int j = 0; j != raster.getHeight(); j++) {
                                if (data[j * raster.getWidth() + i] > threshold) {
                                    data[j * raster.getWidth() + i] = 1;
                                } else {
                                    data[j * raster.getWidth() + i] = 0;
                                }
                            }
                        }
                        z++;
                        Slice slice = new Slice(data, raster.getWidth(), raster.getHeight(), z, ratePixel);
                        sliceList.add(slice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0,0, width, height);
                float ratio = (float)width/height;
                glMatrixUtil = new GLMatrixUtil();
                glMatrixUtil.frustum( -ratio, ratio, -1, 1, 1, 5);
                glMatrixUtil.setCamera(0, 0, 3, 0, 0, 0, 0, 1.0f, 1.0f);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                glMatrixUtil.pushMatrix();
                //平移
                glMatrixUtil.translate(transX, -transY, transZ);
                // 旋转
                glMatrixUtil.rotate(rotateZ, 0, 0, 1.0f);
                glMatrixUtil.rotate(rotateX, 0, 1.0f, 0);
                glMatrixUtil.rotate(rotateY, 1.0f, 0, 0);
                //缩放
                glMatrixUtil.scale(scale, scale, scale);

                System.arraycopy(glMatrixUtil.getFinalMatrix(), 0, mMVPMatrix, 0, mMVPMatrix.length);

                glMatrixUtil.popMatrix();
                for (Slice s : sliceList) {
                    s.draw(mMVPMatrix);
                }
            }
        });

        rotateBtn = (CardView)findViewById(R.id.view3d_rotate_flag);
        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!rotateFlag){
                    rotateFlag = true;
                    rotateBtn.setCardBackgroundColor(getResources().getColor(R.color.white));
                }else{
                    rotateFlag = false;
                    rotateBtn.setCardBackgroundColor(getResources().getColor(R.color.transparent));
                }
            }
        });
        zoomControls = (ZoomControls)findViewById(R.id.view3d_zoom_control);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scale += 0.01;
                setHideTask();
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scale -= 0.01;
                setHideTask();
            }
        });
        zoomControls.hide();
    }

    private void setHideTask(){
        if (autoHideTask == null){
            autoHideTask = new TimerTask() {
                @Override
                public void run() {
                    zoomControls.post(new Runnable() {
                        @Override
                        public void run() {
                            if (zoomControls.getVisibility() == View.VISIBLE) {
                                zoomControls.hide();
                                hideFlag = false;
                            }
                        }
                    });
                    autoHideTask.cancel();
                    autoHideTask = null;
                }
            };
            timer.schedule(autoHideTask,  5000);
        }else{
            autoHideTask.cancel();
            autoHideTask = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:{
                posX = ev.getX();
                posY = ev.getY();
                dis = 0;
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                if(ev.getPointerCount() == 1){
                    if (!rotateFlag) {
                        float x = (ev.getX() - posX) / rateTrans;
                        float y = (ev.getY() - posY) / rateTrans;

                        transX += x;
                        transY += y;
                    } else {
                        float x = (ev.getX() - posX) / rateRotate;
                        float y = (ev.getY() - posY) / rateRotate;

                        rotateX += x;
                        rotateY += y;

                    }
                }else if(ev.getPointerCount() == 2){
                    if (zoomControls.getVisibility() == View.GONE&&!hideFlag){
                        zoomControls.show();
                        hideFlag = true;
                        setHideTask();
                    }
                    float x = ev.getX(1) - ev.getX(0);
                    float y = ev.getY(1) - ev.getY(0);
                    float disNew = (float)Math.sqrt(x*x + y*y);
                    if (dis == 0){
                        dis = disNew;
                    }else{
                        scale += (disNew - dis)/rateScale;
                    }
                    dis = disNew;
                }
                posX = ev.getX();
                posY = ev.getY();
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
