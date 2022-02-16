package ru.threeguns.manager;

import android.app.Activity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kh.hyper.utils.NotProguard;
import ru.threeguns.event.handler.PaymentHandler;

public interface PaymentManager extends NotProguard {

  void requestPay(Activity activity, PaymentRequest request, PaymentHandler handler);

  String getPriceByProductId(String productId);

  public static class PaymentRequest implements NotProguard {
    private String productId;
    private String productName;
    private String productDescription;
    private String currency;
    private float amount;
    private float gameCoinAmount;
    private int count;
    private String serverId;

    private String gameUsername;
    private String gameUserId;
    private String gameExtra;
    // Not For Game.
    private HashMap<String, String> extraMap;

    public PaymentRequest() {
      extraMap = null;
      count = 1;
    }

    public String getGameUsername() {
      return gameUsername;
    }

    public PaymentRequest setGameUsername(String gameUsername) {
      this.gameUsername = gameUsername;
      return this;
    }

    public String getGameUserId() {
      return gameUserId;
    }

    public PaymentRequest setGameUserId(String gameUserId) {
      this.gameUserId = gameUserId;
      return this;
    }

    public int getCount() {
      return count;
    }

    public PaymentRequest setCount(int count) {
      this.count = count;
      return this;
    }

    public String getGameExtra() {
      return gameExtra;
    }

    public PaymentRequest setGameExtra(String gameExtra) {
      this.gameExtra = gameExtra;
      return this;
    }

    public String getProductId() {
      return productId;
    }

    public PaymentRequest setProductId(String productId) {
      this.productId = productId;
      return this;
    }

    public String getProductName() {
      return productName;
    }

    public PaymentRequest setProductName(String productName) {
      this.productName = productName;
      return this;
    }

    public String getProductDescription() {
      return productDescription;
    }

    public PaymentRequest setProductDescription(String productDescription) {
      this.productDescription = productDescription;
      return this;
    }

    public String getCurrency() {
      return currency;
    }

    public PaymentRequest setCurrency(String currency) {
      this.currency = currency;
      return this;
    }

    public float getAmount() {
      return amount;
    }

    public PaymentRequest setAmount(float amount) {
      this.amount = amount;
      return this;
    }

    public float getGameCoinAmount() {
      return gameCoinAmount;
    }

    public PaymentRequest setGameCoinAmount(float gameCoinAmount) {
      this.gameCoinAmount = gameCoinAmount;
      return this;
    }

    public String getServerId() {
      return serverId;
    }

    public PaymentRequest setServerId(String serverId) {
      this.serverId = serverId;
      return this;
    }

    public Map<String, String> toParams() {
      Map<String, String> params = new HashMap<String, String>();

      params.put("product_id", productId);
      params.put("product_name", productName);
      params.put("product_description", productDescription);
      params.put("amount", String.format(Locale.US, "%.2f", amount));
      params.put("server_id", serverId);
      params.put("currencycode", currency);

      if (gameExtra != null) {
        params.put("game_extra", gameExtra);
      }
      if (extraMap != null) {
        params.put("extra_data", new JSONObject(extraMap).toString());
      }

      return params;
    }

    public HashMap<String, String> getInternalExtra() {
      return extraMap;
    }

  }
}
