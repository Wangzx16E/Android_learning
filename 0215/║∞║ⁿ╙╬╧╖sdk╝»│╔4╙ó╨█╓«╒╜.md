- 获取fingerprint用于谷歌验证签名
```bash
keytool -keystore {your keystore path} -list -v
```

- 将TGSDK_Release-release.aar放入app\libs\目录中

- 将以下部分代码参考放入app\build.gradle中dependencies节中
``` groovy
api fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
implementation 'com.facebook.android:facebook-login:latest.release'
implementation 'com.google.android.gms:play-services-auth:19.2.0'
implementation 'com.appsflyer:af-android-sdk:5.0.0'
```
-  在app下的AndroidManifest.xml中增加如下queries配置
```groovy
 <queries>
   <package android:name="com.facebook.katana" />
</queries>
```
-  在app下的AndroidManifest.xml中Application节确保有以下配置
```groovy
    <!-- tg sdk -->
    <activity android:name="ru.threeguns.ui.LoginActivity" android:configChanges="keyboardHidden|orientation|screenSize" android:theme="@android:style/Theme.Translucent.NoTitleBar" />
    <activity android:name="ru.threeguns.ui.AccountActivity" android:configChanges="keyboardHidden|orientation|screenSize" android:theme="@android:style/Theme.Translucent.NoTitleBar" />
    <activity android:name="ru.threeguns.ui.GuestActivity" android:configChanges="keyboardHidden|orientation|screenSize" android:theme="@android:style/Theme.Translucent.NoTitleBar" />
    <activity android:name="ru.threeguns.ui.CommonWebActivity" android:configChanges="keyboardHidden|orientation|screenSize" android:theme="@android:style/Theme.Translucent.NoTitleBar" />

<receiver android:name="com.appsflyer.SingleInstallBroadcastReceiver" android:exported="true">
  <intent-filter>
  	<action android:name="com.android.vending.INSTALL_REFERRER" />
  </intent-filter>
</receiver>
<!-- ql tracker -->
<receiver android:name="ru.threeguns.engine.tracker.QLReferrerReceiver" android:exported="true">
  <intent-filter>
    <action android:name="com.android.vending.INSTALL_REFERRER" />
  </intent-filter>
</receiver>

<!-- google -->
<activity android:name="com.google.android.gms.auth.api.signin.internal.SignInHubActivity" />

<meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />

<!-- facebook extension -->
<meta-data android:name="com.facebook.sdk.ApplicationId" android:value="460333488415050"/>
<activity android:name="com.facebook.FacebookActivity" android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation" />
<activity android:name="com.facebook.CustomTabActivity" android:exported="true">
  <intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="fb460333488415050" />
  </intent-filter>
</activity>
```

