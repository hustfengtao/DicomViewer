package com.taofe.dicomviewer;

import java.util.Comparator;

public class DicomListItem {
    private int instanceNum;
    private String filePath;
    public DicomListItem(String path, int num){
        instanceNum = num;
        filePath = path;
    }
    public int getInstanceNum(){
        return instanceNum;
    }
    public String getFilePath(){
        return filePath;
    }
    public static Comparator r_idComparator = new Comparator<DicomListItem>() {
        @Override
        public int compare(DicomListItem o1, DicomListItem o2) {
            return (o1.getInstanceNum()<o2.getInstanceNum()? -1:
                    (o1.getInstanceNum() == o2.getInstanceNum() ? 0 : 1));
        }
    };
    public static Comparator nameComparator = new Comparator<DicomListItem>() {
        @Override
        public int compare(DicomListItem o1, DicomListItem o2) {
            return (int) (o1.getFilePath().compareTo(o2.getFilePath()));
        }
    };
}
