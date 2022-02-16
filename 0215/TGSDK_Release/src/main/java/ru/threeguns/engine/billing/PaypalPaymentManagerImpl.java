package ru.threeguns.engine.billing;

import android.content.Intent;

import kh.hyper.core.Module;
import kh.hyper.core.Parameter;
import ru.threeguns.engine.controller.Constants;
import ru.threeguns.engine.manager.AbstractPaymentManager;
import ru.threeguns.ui.CommonWebActivity;
import ru.threeguns.ui.fragments.PaypalPaymentFragment;
import ru.threeguns.utils.ActivityHolder;

public class PaypalPaymentManagerImpl extends AbstractPaymentManager {
  public static PaymentRequest paymentRequestHolder;

  @Override
  protected void doPay(final PaymentRequest request) {
    paymentRequestHolder = request;
    Intent intent = new Intent(Module.of(ActivityHolder.class).getTopActivity(), CommonWebActivity.class);
    intent.putExtra("fragment", PaypalPaymentFragment.class);
    Constants.request = request;
    Module.of(ActivityHolder.class).getTopActivity().startActivity(intent);
  }

  @Override
  protected void onLoad(Parameter parameter) {}


  @Override
  protected void onRelease() {}


  public void payFinish() {
    Module.of(ActivityHolder.class).getTopActivity().finish();
  }
}
