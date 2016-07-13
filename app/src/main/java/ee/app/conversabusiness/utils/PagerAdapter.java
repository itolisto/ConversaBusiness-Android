/**
 * Fragment change was implemented using the following code:
 * 1. {@link http://stackoverflow.com/questions/18588944/replace-one-fragment-with-another-in-viewpager}
 * 2. {@link http://stackoverflow.com/questions/7992216/android-fragment-handle-back-button-press}
 */
package ee.app.conversabusiness.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ee.app.conversabusiness.FragmentHome;
import ee.app.conversabusiness.FragmentUsersChat;
import ee.app.conversabusiness.FragmentDeals;
import ee.app.conversabusiness.FragmentSettings;

public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;

        switch (i) {
            case 0:
                fragment = new FragmentHome();
                break;
            case 1:
                fragment = new FragmentUsersChat();
                break;
            case 2:
                fragment = new FragmentDeals();
                break;
            case 3:
                fragment = new FragmentSettings();
                break;
            default:
                fragment = new FragmentUsersChat();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

}