package com.taofe.dicomviewer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SeriesList implements Parcelable {
    private ArrayList<String> pathList;
    public SeriesList(){

    }

    public String get(int i){
        return pathList.get(i);
    }

    public int size(){
        return pathList.size();
    }

    public boolean add(String s){
        return pathList.add(s);
    }

    public void clear(){
        pathList.clear();
    }

    public boolean isEmpty(){
        return pathList.isEmpty();
    }

    public ArrayList<String> getArrayList(){
        return pathList;
    }

    private SeriesList(Parcel in){
        pathList = in.createStringArrayList();
    }

    public static final Creator<SeriesList> CREATOR = new Creator<SeriesList>() {
        @Override
        public SeriesList createFromParcel(Parcel in) {
            return new SeriesList(in);
        }

        @Override
        public SeriesList[] newArray(int size) {
            return new SeriesList[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(pathList);
    }
}