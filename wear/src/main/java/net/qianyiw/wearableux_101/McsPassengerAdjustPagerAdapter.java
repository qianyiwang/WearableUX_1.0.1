package net.qianyiw.wearableux_101;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;

/**
 * Created by Qianyi on 7/24/2016.
 */
public class McsPassengerAdjustPagerAdapter extends FragmentGridPagerAdapter {
    public McsPassengerAdjustPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int i, int i1) {
        Fragment fragment = new McsPassengerAdjustFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("count", i1 + 1);
        int temp = i1+1;
        Log.v("send", "page" + temp);
        fragment.setArguments(bundle);
        return  fragment;
    }

    @Override
    public long getFragmentId(int row, int column) {
        Log.v("fragment id","fragment id"+column);
        return super.getFragmentId(row, column);
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return 2;
    }
}
