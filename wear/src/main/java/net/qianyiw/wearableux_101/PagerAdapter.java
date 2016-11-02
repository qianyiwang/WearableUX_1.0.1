package net.qianyiw.wearableux_101;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

/**
 * Created by QWANG97 on 4/4/2016.
 */
public class PagerAdapter extends FragmentGridPagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getFragment(int i, int i1) {
        Fragment fragment = new ClimateControlFragment();
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

}
