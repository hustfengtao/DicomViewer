package com.taofe.dicomviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class HistogramUtil {
    private  ArrayList<Pair> list = new ArrayList<Pair>();
    private short[] data;
    private short maxValue;
    private short minValue;
    private int maxCount;
    public HistogramUtil(short[] data){
        this.data = data;
        minValue = sortList(data)[0];
        maxValue = sortList(data)[1];
        for (short i=minValue;i!=maxValue+1;i++){
            list.add(new Pair(i, 0));
        }
        sortHist();
    }

    public static class Pair{
        private short value;
        private int counts;
        public Pair(short value, int counts){
            this.value = value;
            this.counts = counts;
        }

        public int getCounts() {
            return counts;
        }

        public short getValue() {
            return value;
        }

        public void setCounts(int counts) {
            this.counts = counts;
        }
        public void setValue(short value) {
            this.value = value;
        }
    }

    private class PairSortByCounts implements Comparator<Pair> {
        public int compare(Pair o1, Pair o2) {
            return (o1.getCounts()<o2.getCounts()? -1:
                    (o1.getCounts() == o2.getCounts() ? 0 : 1));
        }
    }

    public ArrayList<Pair> retriveHistData(){
        return list;
    }

    public short getMax() {
        return maxValue;
    }

    public short getMin() {
        return minValue;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public static short[] sortList(short[] data) {
        short[] arr = new short[2];
        Arrays.sort(data);
        arr[0]= data[0];
        arr[1]= data[data.length-1];
        return arr;
    }

    public static byte[] sortList(byte[] data) {
        byte[] arr = new byte[2];
        Arrays.sort(data);
        arr[0] = data[0];
        arr[1] = data[data.length-1];
        return arr;
    }

    public static int[] sortList(int[] data) {
        int[] arr = new int[2];
        Arrays.sort(data);
        arr[0] = data[0];
        arr[1] = data[data.length-1];
        return arr;
    }

    private void sortHist(){
        int next = 0;
        int count = 0;
        while(next < data.length){
            count = findPixelValue(next);
            list.get(data[next]).setCounts(count);
            //list.add(new Pair(data[next], count));
            next += count;
        }
        ArrayList<Pair> sortlist = (ArrayList<Pair>)list.clone();
        Collections.sort(sortlist, new PairSortByCounts());

        if (sortlist.isEmpty()){
            maxCount = 0;
        }
        if (sortlist.size() == 1){
            maxCount = sortlist.get(sortlist.size() -1).getCounts();
        }else {
            maxCount = sortlist.get(sortlist.size() - 1).getCounts();
            int maxl = sortlist.get(sortlist.size() - 2).getCounts();
            if (maxCount > maxl * 2) {
                maxCount = maxl * 2;
            }
        }
    }

    private int findPixelValue(int pos){
        int nCount = 1;
        for (int i=pos;i!=data.length;i++){
            if (data[i] == data[pos]){
                nCount++;
                continue;
            }else {
                break;
            }
        }
        return nCount;
    }
}
