package com.taofe.dicomviewer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import org.dcm4che3.android.Raster;
import java.io.File;
import java.util.ArrayList;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.dcm4che3.android.imageio.dicom.DicomImageReader;

import static com.taofe.dicomviewer.ProgressHandler.HIDE_PROGRESS;
import static com.taofe.dicomviewer.ProgressHandler.MAX_PROGRSS;
import static com.taofe.dicomviewer.ProgressHandler.SHOW_PROGRESS;
import static com.taofe.dicomviewer.ProgressHandler.UPDATE_PROGRESS;

public class DicomMPRActivity extends AppCompatActivity {
    private boolean windowAdjustMode = false;
    private boolean windowTransformMode = false;
    private boolean windowHistogramMode = false;
    private boolean windowAutoPlayMode = false;
    private boolean windowSaveMode = false;

    private SeriesList seriesList = null;
    private int windowWidth = 4096;
    private int windowCenter = 2048;
    private ArrayList<Raster> rasterList = new ArrayList<Raster>();
    private SeekBar[] seekBar = new SeekBar[3];
    private ProgressBar loadProgress;
    private Raster[] raster = new Raster[3];
    private FolderPhotoView[] photo = new FolderPhotoView[3];
    private int posX;
    private int posY;
    private DicomImageReader reader = new DicomImageReader();
    private View viewCtrlPanel;
    private View viewAdjustWindow;
    private View viewMatrixTransform;
    private View viewShowHistogram;
    private View viewAutoPlay;
    private View viewSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpr);
        InitView();
        if(getIntent().getParcelableExtra("serieslist")!=null){
            seriesList = getIntent().getParcelableExtra("serieslist");
        }else{
            Toast.makeText(DicomMPRActivity.this, "Not a dicom series", Toast.LENGTH_SHORT).show();
        }
        if (seriesList != null) {
            final ProgressHandler handler = new ProgressHandler(loadProgress);
            handler.setOnProgressOverListener(new OnProgressOverListener() {
                @Override
                public void onFinish() {
                    viewCtrlPanel.setVisibility(View.VISIBLE);
                    seekBar[0].setVisibility(View.VISIBLE);
                    seekBar[1].setVisibility(View.VISIBLE);
                    seekBar[2].setVisibility(View.VISIBLE);
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int nProgress = 0;
                    handler.sendMessage(SHOW_PROGRESS, 0);
                    handler.sendMessage(MAX_PROGRSS, seriesList.size());
                    for (String s:seriesList.getArrayList()){
                        File file = new File(s);
                        try {
                            reader.open(file);
                            Raster raster = reader.readRaster(0);
                            rasterList.add(raster);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        nProgress++;
                        handler.sendMessage(UPDATE_PROGRESS, nProgress);
                    }
                    handler.sendMessage(HIDE_PROGRESS, 0);

                    if (!rasterList.isEmpty()){
                        seekBar[0].setMax(rasterList.size()-1);
                        seekBar[0].setProgress(rasterList.size()/2);
                        seekBar[1].setMax(reader.getWidth()-1);
                        seekBar[1].setProgress(reader.getWidth()/2);
                        seekBar[2].setMax(reader.getHeight()-1);
                        seekBar[2].setProgress(reader.getHeight()/2);

                        photo[0].setCount(rasterList.size());
                        photo[1].setCount(reader.getWidth());
                        photo[2].setCount(reader.getHeight());

                        photo[0].setActive(true);
                    }
                }
            }).start();
        }
    }

    public void setFriendView(int num){
        if (num > 3 || num < 0){
            return;
        }
        for (int i=0;i!=3;i++){
            if (photo[i]!=null) {
                if (i != num) {
                    photo[num].addFriendView(photo[i]);
                }
            }
        }
    }

    private void InitView(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.mpr_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        photo[0] = (FolderPhotoView)findViewById(R.id.mpr_image1);
        photo[1] = (FolderPhotoView)findViewById(R.id.mpr_image2);
        photo[2] = (FolderPhotoView)findViewById(R.id.mpr_image3);
        for (int i=0;i!=3;i++){
            setFriendView(i);
            photo[i].setReader(reader);
            photo[i].setKeepSync(false);
        }

        seekBar[0] = (SeekBar)findViewById(R.id.mpr_seekbar1);
        seekBar[0].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                raster[0] = rasterList.get(progress);
                photo[0].setRaster(raster[0], progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar[1] = (SeekBar)findViewById(R.id.mpr_seekbar2);
        seekBar[1].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                raster[1] = RasterProcessUtil.RasterT(rasterList, reader.getWidth(), reader.getHeight(), progress);
                photo[1].setRaster(raster[1], progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar[2] = (SeekBar)findViewById(R.id.mpr_seekbar3);
        seekBar[2].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                raster[2] = RasterProcessUtil.RasterS(rasterList, reader.getWidth(), reader.getHeight(), progress);
                photo[2].setRaster(raster[2], progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        viewCtrlPanel = findViewById(R.id.mpr_ctrl);
        viewAdjustWindow = findViewById(R.id.mpr_adjust);
        viewMatrixTransform = findViewById(R.id.mpr_transform);
        viewShowHistogram = findViewById(R.id.mpr_hisogram);
        viewAutoPlay = findViewById(R.id.mpr_autoplay);
        viewSave = findViewById(R.id.mpr_save);
        viewAdjustWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!windowAdjustMode) {
                    viewAdjustWindow.setBackgroundResource(R.color.darkgray);
                    windowAdjustMode = true;
                }else{
                    viewAdjustWindow.setBackgroundResource(R.color.trans_gray);
                    windowAdjustMode = false;
                }
                photo[0].setWindowAdjustMode(windowAdjustMode);
                photo[1].setWindowAdjustMode(windowAdjustMode);
                photo[2].setWindowAdjustMode(windowAdjustMode);
            }
        });
        viewMatrixTransform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!windowTransformMode) {
                    viewMatrixTransform.setBackgroundResource(R.color.darkgray);
                    windowTransformMode = true;
                }else{
                    viewMatrixTransform.setBackgroundResource(R.color.trans_gray);
                    windowTransformMode = false;
                }
            }
        });
        viewShowHistogram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!windowHistogramMode) {
                    viewShowHistogram.setBackgroundResource(R.color.darkgray);
                    windowHistogramMode = true;
                }else{
                    viewShowHistogram.setBackgroundResource(R.color.trans_gray);
                    windowHistogramMode = false;
                }
            }
        });
        viewAutoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!windowAutoPlayMode) {
                    viewAutoPlay.setBackgroundResource(R.color.darkgray);
                    windowAutoPlayMode = true;
                }else{
                    viewAutoPlay.setBackgroundResource(R.color.trans_gray);
                    windowAutoPlayMode = false;
                }
            }
        });
        viewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!windowSaveMode) {
                    viewSave.setBackgroundResource(R.color.darkgray);
                    windowSaveMode = true;
                }else{
                    viewSave.setBackgroundResource(R.color.trans_gray);
                    windowSaveMode = false;
                }
            }
        });

        loadProgress = (ProgressBar)findViewById(R.id.mpr_load_process);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
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
