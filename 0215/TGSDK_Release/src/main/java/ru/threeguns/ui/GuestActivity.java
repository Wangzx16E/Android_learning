package ru.threeguns.ui;

import android.os.Bundle;

import kh.hyper.core.Module;
import ru.threeguns.entity.User;
import ru.threeguns.ui.dfs.GuestDialogFragment;
import ru.threeguns.ui.fragments.AountDialogFragment;

public class GuestActivity extends TGFragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            preloadFragment(GuestDialogFragment.class);
            changeFragment(GuestDialogFragment.class);
    }
}
