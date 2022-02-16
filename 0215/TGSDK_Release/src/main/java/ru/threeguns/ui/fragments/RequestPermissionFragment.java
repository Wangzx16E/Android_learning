package ru.threeguns.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import kh.hyper.event.EventManager;
import kh.hyper.event.Handle;
import kh.hyper.ui.HFragment;
import kh.hyper.utils.HL;
import ru.threeguns.engine.controller.TGController;
import ru.threeguns.event.ActivityPermissionResultEvent;

public class RequestPermissionFragment extends HFragment {
  private static final int PERMISSION_REQUEST_CODE = 11727;

  public static Parameter parameterHolder;

  @TargetApi(23)
  @SuppressLint("InflateParams")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    int tg_fragment_requestpermission_id = getActivity().getResources().getIdentifier("tg_fragment_requestpermission", "layout", getActivity().getPackageName());
    View v = inflater.inflate(tg_fragment_requestpermission_id, null);

    EventManager.instance.register(this);

    String[] permissons = new String[]{ //
        //				Manifest.permission.GET_ACCOUNTS, //
        Manifest.permission.WRITE_EXTERNAL_STORAGE, //
        Manifest.permission.READ_EXTERNAL_STORAGE//
        //				Manifest.permission.ACCESS_COARSE_LOCATION //
        //				Manifest.permission.READ_PHONE_STATE //
    };

    //		for (String permission : permissons) {
    //			if (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
    //				// if
    //				// (!getActivity().shouldShowRequestPermissionRationale(permission))
    //				// {
    //				// HL.w("Permission {} not granted.", permission);
    //				// Toast.makeText(getActivity(), "Some permission is denied.",
    //				// Toast.LENGTH_LONG).show();
    //				// System.exit(-1);
    //				// Process.killProcess(Process.myPid());
    //				// return null;
    //				// }
    //			}
    //		}

    getActivity().requestPermissions(permissons, PERMISSION_REQUEST_CODE);

    return v;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    EventManager.instance.unregister(this);
  }

  @Handle
  protected void onPermissionResult(ActivityPermissionResultEvent e) {
    if (e.getRequestCode() == PERMISSION_REQUEST_CODE) {
      for (int i = e.getPermissions().length - 1; i >= 0; i--) {
        String permission = e.getPermissions()[i];
        int result = e.getGrantResults()[i];
        HL.w("permission : {}", permission);
        HL.w("result : {}", result);
        if (result != PackageManager.PERMISSION_GRANTED) {
          HL.w("PERMISSION DENIED , SHOW DIALOG");
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {

              new AlertDialog.Builder(getActivity())//
                  .setTitle("Notice")//
                  .setMessage("You should grant all permissions before enter game.")//
                  .setCancelable(false)//
                  .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      System.exit(-1);
                      Process.killProcess(Process.myPid());
                    }
                  })//
                  .create().show();
            }
          });
          return;
        }
      }

      HL.w("ALL GRANTED");
      requestFinish();
      Module.of(TGController.class, parameterHolder);
      parameterHolder = null;
    }
  }
}
