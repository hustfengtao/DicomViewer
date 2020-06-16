package com.taofe.dicomviewer;

import java.util.ArrayList;

public class TypedViewData <T>{
    private T t;
    private int dataType;
    private String titleText;
    private String contentText;
    private String modifyString;
    private ArrayList<String> imgArray = new ArrayList<String>();

    public TypedViewData(T t, int dataType, ArrayList<String> imgArray, String modifyString, String contentText, String titleText){
        this.t = t;
        this.dataType = dataType;
        this.modifyString = modifyString;
        this.imgArray = imgArray;
        this.contentText = contentText;
        this.titleText = titleText;
    }

    public T getData(){
        return this.t;
    }

    public int getType(){
        return dataType;
    }

    public void setData(T t){
        this.t = t;
    }

    public void setType(int dataType){
        this.dataType = dataType;
    }

    public ArrayList<String> getImgArray(){
        return imgArray;
    }

    public String getContentText(){
        return contentText;
    }

    public String getTitleText(){
        return titleText;
    }

    public String getModifyString(){
        return modifyString;
    }
}