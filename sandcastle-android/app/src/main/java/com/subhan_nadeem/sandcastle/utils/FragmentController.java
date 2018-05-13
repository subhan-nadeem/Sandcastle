package com.subhan_nadeem.sandcastle.utils;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.subhan_nadeem.sandcastle.R;

/**
 * Created by Subhan Nadeem on 2017-10-21.
 * Controls fragment business logic of hosting activity
 */

public class FragmentController {

    private final FragmentManager mFragmentManager;
    @IdRes private final int mFragmentContainerRes;

    public FragmentController(FragmentManager fragmentManager, @IdRes int fragmentContainerRes) {
        mFragmentContainerRes = fragmentContainerRes;
        mFragmentManager = fragmentManager;
    }

    public void initFragment(Fragment initFragment) {
        Fragment fragment = mFragmentManager.findFragmentById(mFragmentContainerRes);

        if (fragment == null) {
            fragment = initFragment;

            // First initialization should not be added to backstack
            changeFragment(fragment, false, false);
        }
    }

    /**
     * Change the current displayed fragment by a new one.
     * - if the fragment is in backstack, it will pop it
     * - if the fragment is already displayed (trying to change the fragment with the same),
     * it will not do anything
     * @param frag            the new fragment to display
     * @param saveInBackstack if we want the fragment to be in backstack
     * @param animate         if we want a nice animation or not
     */
    public void changeFragment(Fragment frag, boolean saveInBackstack, boolean animate) {
        String backStateName = ((Object) frag).getClass().getName();
        try {
            FragmentManager manager = mFragmentManager;
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            FragmentTransaction transaction = manager.beginTransaction();

            if (animate) {
                transaction.setCustomAnimations(R.anim.slide_in_up,
                        R.anim.slide_down_out,
                        R.anim.slide_in_up,
                        R.anim.slide_down_out);
            }

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                //fragment not in back stack, create it.

                transaction.replace(mFragmentContainerRes, frag, backStateName);

                if (saveInBackstack)
                    transaction.addToBackStack(backStateName);

                transaction.commit();
            } else {
                Fragment fragment = manager.findFragmentByTag(backStateName);

                transaction.replace(mFragmentContainerRes, fragment, backStateName).commit();
            }
        } catch (IllegalStateException exception) {
            Log.w(this.getClass().getName(),
                    "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());
        }
    }
}
