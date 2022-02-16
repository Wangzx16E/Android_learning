package ru.threeguns.ui.dfs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kh.hyper.ui.HFragment;
import ru.threeguns.ui.LoginActivity;

public class GuestDialogFragment extends HFragment {

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity instanceof LoginActivity){
            LoginActivity loginActivity = (LoginActivity) getActivity();
            loginActivity.setCenter();
        }
        int tg_fragment_guestupgrade_id = getActivity().getResources().getIdentifier("tg_dialog_guest", "layout", getActivity().getPackageName());
        View v = inflater.inflate(tg_fragment_guestupgrade_id, null);


        int tg_finish_id = getActivity().getResources().getIdentifier("tg_finish", "id", getActivity().getPackageName());
        v.findViewById(tg_finish_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBack();
            }
        });

        int tg_back_id = getActivity().getResources().getIdentifier("tg_back", "id", getActivity().getPackageName());
        v.findViewById(tg_back_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBack();
            }
        });


        int tg_confirm_btn_id = getActivity().getResources().getIdentifier("tg_confirm_btn", "id", getActivity().getPackageName());
        v.findViewById(tg_confirm_btn_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(GuestUpgradeDialogFragment.class);
            }
        });



        return v;
    }
}
