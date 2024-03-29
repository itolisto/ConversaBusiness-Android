/**
 * Fragment change was implemented using the following code:
 * 1. {@link http://stackoverflow.com/questions/18588944/replace-one-fragment-with-another-in-viewpager}
 * 2. {@link http://stackoverflow.com/questions/7992216/android-fragment-handle-back-button-press}
 */
package ee.app.conversamanager.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import ee.app.conversamanager.FragmentPreferences;
import ee.app.conversamanager.FragmentStatistics;
import ee.app.conversamanager.FragmentUsersChat;

public class PagerAdapter extends SmartFragmentStatePagerAdapter<Fragment> {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;

        switch (i) {
            case 0:
                fragment = new FragmentUsersChat();
                break;
            case 1:
                fragment = new FragmentStatistics();
                break;
            case 2:
                fragment = new FragmentPreferences();
                break;
            default:
                fragment = new FragmentUsersChat();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

}