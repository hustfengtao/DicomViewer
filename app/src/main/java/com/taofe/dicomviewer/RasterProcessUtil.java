package com.taofe.dicomviewer;
import org.dcm4che3.android.Raster;
import org.dcm4che3.android.image.LookupTable;
import org.dcm4che3.android.image.LookupTableFactory;
import org.dcm4che3.android.image.StoredValue;
import org.dcm4che3.android.imageio.dicom.DicomImageReadParam;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.android.imageio.dicom.DicomImageReader;

import java.util.ArrayList;

public class RasterProcessUtil {
    public static Raster MirrorH(Raster raster, int w, int h){
        Raster destRaster = new Raster(w, h, raster.getDataType());
        short[] data = raster.getShortData();
        for (int j=0;j!=h;j++){
            for (int i=0;i!=w;i++){
                destRaster.setVal(j*w + i, data[j*w + w -1 -i]);
            }
        }
        return destRaster;
    }

    public static Raster MirrorV(Raster raster, int w, int h){
        Raster destRaster = new Raster(w, h, raster.getDataType());
        short[] data = raster.getShortData();
        for (int j=0;j!=h;j++){
            for (int i=0;i!=w;i++){
                destRaster.setVal(j*w + i, data[(h-1 -j)*w + i]);
            }
        }
        return destRaster;
    }

    public static Raster Invert(Raster raster, int w, int h, int max, int min){
        short[] data = raster.getShortData();
        for (int i=0;i!=data.length;i++){
            data[i] = (short)(max + min -data[i]);
            raster.setVal(i, data[i]);
        }
        return raster;
    }

    public static Raster Filter(Raster raster, int w, int h){
        Raster destRaster = new Raster(w, h, raster.getDataType());
        short[] data = raster.getShortData();
        for(int i=1;i<h-1;i++) {
            for (int j = 1; j < w - 1; j++) {
                short v1 = data[(i - 1) * w + j - 1];
                short v2 = data[(i - 1) * w + j];
                short v3 = data[(i - 1) * w + j + 1];
                short v4 = data[i * w + j - 1];
                short v6 = data[i * w + j + 1];
                short v7 = data[(i + 1) * w + j - 1];
                short v8 = data[(i + 1) * w + j];
                short v9 = data[(i + 1) * w + j + 1];
                int mean= (v1 + v2 + v3 + v4 + v6 + v7 + v8 + v9) / 8;
                destRaster.setVal(i*w + j, (short)mean);
            }
        }
        return destRaster;
    }

    public static Raster RasterT(ArrayList<Raster> list, int w, int h, int pos){
        Raster destRaster = new Raster(h, list.size(), list.get(0).getDataType());
        for (int i=0;i!=list.size();i++){
            for (int j=0;j!=h;j++){
                destRaster.setVal(i*h + j, list.get(i).getShortData()[j*w + pos]);
            }
        }
        return destRaster;
    }

    public static Raster RasterS(ArrayList<Raster> list, int w, int h, int pos){
        Raster destRaster = new Raster(w, list.size(), list.get(0).getDataType());
        for (int i=0;i!=list.size();i++){
            for (int j=0;j!=w;j++){
                destRaster.setVal(i*w + j, list.get(i).getShortData()[pos*w + j]);
            }
        }
        return destRaster;
    }

    public static Raster applyWindowCenter(DicomImageReader reader, Raster raster, int ww, int wc){
        Attributes imgAttrs = reader.getAttributes();
        StoredValue sv = StoredValue.valueOf(imgAttrs);
        LookupTableFactory lutParam = new LookupTableFactory(sv);
        DicomImageReadParam dParam = new DicomImageReadParam();
        dParam.setWindowCenter((float)wc);
        dParam.setWindowWidth((float)ww);
        lutParam.setModalityLUT(imgAttrs);
        if (dParam.getWindowWidth() != 0.0F) {
            lutParam.setWindowCenter(dParam.getWindowCenter());
            lutParam.setWindowWidth(dParam.getWindowWidth());
        }
        lutParam.setPresentationLUT(imgAttrs);
        LookupTable lut = lutParam.createLUT(8);
        Raster destRaster = new Raster(reader.getWidth(), reader.getHeight(), 0);
        lut.lookup(raster, destRaster);
        return destRaster;
    }

    public static Raster applyWindowCenter(DicomImageReader reader, Raster raster, int ww, int wc, int width, int height){
        Attributes imgAttrs = reader.getAttributes();
        StoredValue sv = StoredValue.valueOf(imgAttrs);
        LookupTableFactory lutParam = new LookupTableFactory(sv);
        DicomImageReadParam dParam = new DicomImageReadParam();
        dParam.setWindowCenter((float)wc);
        dParam.setWindowWidth((float)ww);
        lutParam.setModalityLUT(imgAttrs);
        if (dParam.getWindowWidth() != 0.0F) {
            lutParam.setWindowCenter(dParam.getWindowCenter());
            lutParam.setWindowWidth(dParam.getWindowWidth());
        }
        lutParam.setPresentationLUT(imgAttrs);
        LookupTable lut = lutParam.createLUT(8);
        Raster destRaster = new Raster(width, height, 0);
        lut.lookup(raster, destRaster);
        return destRaster;
    }
}
