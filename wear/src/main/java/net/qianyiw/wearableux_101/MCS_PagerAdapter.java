package net.qianyiw.wearableux_101;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;

/**
 * Created by Qianyi on 7/24/2016.
 */
public class MCS_PagerAdapter extends FragmentGridPagerAdapter {

    int currentIdx;
    Fragment fragment;

    public MCS_PagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public android.app.Fragment getFragment(int i, int i1) {
        fragment = new MCS_Fragment();
        Bundle bundle = new Bundle();
        bundle.putInt("count", i1 + 1);
        fragment.setArguments(bundle);
        return  fragment;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return 2;
    }

    @Override
    public int getCurrentColumnForRow(int row, int currentColumn) {
        Log.v("current column", String.valueOf(currentColumn));
        currentIdx = currentColumn;
        return super.getCurrentColumnForRow(row, currentColumn);
    }
}
