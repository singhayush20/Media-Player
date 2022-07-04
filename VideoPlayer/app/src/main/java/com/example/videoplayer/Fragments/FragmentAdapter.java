package com.example.videoplayer.Fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class FragmentAdapter extends FragmentStateAdapter {
    /**
     * @param fragmentActivity if the {@link ViewPager2} lives directly in a
     *                         {@link FragmentActivity} subclass.
     * @see FragmentStateAdapter#FragmentStateAdapter(Fragment)
     * @see FragmentStateAdapter#FragmentStateAdapter(FragmentManager, Lifecycle)
     */
    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Provide a new Fragment associated with the specified position.
     * <p>
     * The adapter will be responsible for the Fragment lifecycle:
     * <ul>
     *     <li>The Fragment will be used to display an item.</li>
     *     <li>The Fragment will be destroyed when it gets too far from the viewport, and its state
     *     will be saved. When the item is close to the viewport again, a new Fragment will be
     *     requested, and a previously saved state will be used to initialize it.
     * </ul>
     *
     * @param position
     * @see ViewPager2#setOffscreenPageLimit
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment mFragment=null;
        switch (position)
        {
            case 0: mFragment=new VideoFragment();
                break;
            case 1: mFragment=new MusicFragment();
        }
        assert mFragment!=null;
        return mFragment;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}
