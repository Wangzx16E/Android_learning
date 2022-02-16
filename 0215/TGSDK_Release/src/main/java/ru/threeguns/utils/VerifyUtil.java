package ru.threeguns.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import ru.threeguns.engine.controller.TGString;

public class VerifyUtil {
  protected static final String usernamePattern = "[0-9a-zA-Z@\\-_]{6,30}";
  protected static final String passwordPattern = "[0-9a-zA-Z]{6,20}";
  protected static final String emailPattern = "^[a-zA-Z0-9!#$%&\\'*+\\\\/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&\'*+\\\\/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$";

  public static String verifyAccount(String account) {
    if (account == null || account.trim().equals("")) {
      return TGString.account_blank;
    }
    if (Pattern.compile(usernamePattern).matcher(account).matches() || Pattern.compile(emailPattern).matcher(account).matches()) {
      return null;
    }
    return TGString.account_format_incorrect;
  }

  public static String verifyUsername(String username) {
    if (username == null || username.trim().equals("")) {
      return TGString.account_blank;
    }
    Pattern p = Pattern.compile(usernamePattern);
    boolean b = p.matcher(username).matches();
    if (b) {
      return null;
    } else {
      return TGString.account_format_incorrect;
    }
  }

  public static String verifyPassword(String password) {
    if (password == null || password.trim().equals("")) {
      return TGString.password_blank;
    }
    Pattern p = Pattern.compile(passwordPattern);
    boolean b = p.matcher(password).matches();
    if (b) {
      return null;
    } else {
      return TGString.password_format_incorrect;
    }
  }

  public static String verifyNickname(String nickname) {
    if (nickname == null || nickname.trim().equals("")) {
      return TGString.nickname_blank;
    }
    return null;
  }

  public static String verifyEmail(String email) {
    if (email == null || email.trim().equals("")) {
      return TGString.email_blank;
    }
    Pattern p = Pattern.compile(emailPattern);
    return p.matcher(email).matches() ? null : TGString.email_format_incorrect;
  }

  public static boolean notEmpty(String str) {
    return str == null ? false : str.trim().equals("") ? false : true;
  }


  public static String getJsonTextFromAssetsFile(Context context, String fileName) {
    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader bf = new BufferedReader(new InputStreamReader(
          context.getAssets().open(fileName + ".json")));
      String line;
      while ((line = bf.readLine()) != null) {
        sb.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

}
