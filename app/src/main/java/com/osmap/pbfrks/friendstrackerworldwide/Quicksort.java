package com.osmap.pbfrks.friendstrackerworldwide;

import java.util.ArrayList;

/**
 * Created by kevin on 28-Jun-17.
 */

public class Quicksort {
    private ArrayList<MarkerCompareHelper> mch;
    public Quicksort(ArrayList<MarkerCompareHelper> mch){
        this.mch = mch;
    }


    public ArrayList<MarkerCompareHelper> sort(int l, int r) {
        int q;
        if (l < r) {
            q = partition(l, r);
            sort(l, q);
            sort(q + 1, r);
        }
        return mch;
    }

    int partition(int l, int r) {
        double x = mch.get((l+r)/2).getDistance();
        int i = l - 1;
        int j = r + 1;

        do {
            i++;
        } while (mch.get(i).getDistance() < x);

        do {
            j--;
        } while (mch.get(j).getDistance() > x);

        if (i < j) {
            double k = mch.get(i).getDistance();
            String between = mch.get(i).getDescription();
            mch.get(i).setDistance(mch.get(j).getDistance());
            mch.get(i).setDescription(mch.get(j).getDescription());
            mch.get(j).setDistance(k);
            mch.get(j).setDescription(between);
        } else {
            return j;
        }
        return -1;
    }
}