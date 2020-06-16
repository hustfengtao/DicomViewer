package com.taofe.dicomviewer;

import java.util.ArrayList;
import java.util.Collections;

public class DicomListSorter {
    ArrayList<DicomListItem> ItemList = new ArrayList<DicomListItem>();
    public DicomListSorter(ArrayList ItemList) {
        this.ItemList = ItemList;
    }
    public ArrayList getSortedListByID() {
        Collections.sort(ItemList, DicomListItem.r_idComparator);
        return ItemList;
    }
    public ArrayList getSortedListByName() {
        Collections.sort(ItemList, DicomListItem.nameComparator);
        return ItemList;
    }
}