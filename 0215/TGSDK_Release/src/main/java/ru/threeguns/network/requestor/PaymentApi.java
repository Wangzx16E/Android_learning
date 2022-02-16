package ru.threeguns.network.requestor;

import java.util.Map;

import kh.hyper.network.annotations.Address;
import kh.hyper.network.annotations.P;
import ru.threeguns.network.TGResultHandler;
import ru.threeguns.network.annotations.Progress;
import ru.threeguns.network.annotations.WithToken;

public interface PaymentApi extends TGApi {
	public static final int SUCCESS = 0;
	public static final int FAILED = 1;
	public static final int CANCEL = 2;
	public static final int IN_PROGRESS = 3;

	//创建订单
	@Progress(Progress.PAYMENT)
	@WithToken
	@Address("/api/createOrder")
	void createOrder(@P("payment_type") String paymentType, @P Map<String, String> paymentParams, TGResultHandler handler);

	@Progress(Progress.PAYMENT)
	@WithToken
	@Address("/api/wechatpay")
	void wechatpay(@P Map<String, String> paymentParams, TGResultHandler handler);
		
	//创建KB订单
	@Progress(Progress.PAYMENT)
	@WithToken
	@Address("/api/createKBOrder")
	void createKBOrder(@P("payment_type") String paymentType, @P("amount") int amount, TGResultHandler handler);


	//Google订单有效性
	@Address("/api/AndroidValid")
	void updateOrder(//
			@P("status") int status, //
			@P("user_id") String userId, //
			@P("platform_reciept") String data, //
			@P("platform_signature") String sign, //
			TGResultHandler handler);

	//平台充值
	@Address("/api/platformTopup")
	@Progress(Progress.PAYMENT)
	@WithToken
	void thpay(//
			@P("PIN") String pin, //
			@P("order_id") String orderId, //
			TGResultHandler handler);

	//获取用户余额
	@Address("/api/getUserBalance")
	@Progress(Progress.LOADING)
	@WithToken
	void getUserBalance(@P("product_id") String productId, TGResultHandler handler);
}
