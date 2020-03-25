# DataWedge Background Scanning
Demo to show scanning with DataWedge when the device screen is off or the app is in the background

Although not common, some customers targeting Zebra Android mobile computers need to be able to scan barcodes when the application is in the background or the mobile computer is in the standby state.  Following a [question posted on the developer forum](http://developer.zebra.com/forum/24851) we came up with an architecture that appeared to work reliably.

**This application has been tested with the internal imager but the principle also applies to Bluetooth scanner.**  Be aware the BT scanners have other considerations such as maintaining the connection when the device goes into standby.  See [here](https://developer.zebra.com/community/home/blog/2018/04/11/rs6000-recommended-settings-for-effective-power-management) for more information.

## Set Wake-up Sources
In order to wake up the device from a standby mode when the trigger button is pressed, you need to set the wake-up sources.  The wake-up sources can be configured using StageNow and the [MX PowerManager](https://techdocs.zebra.com/mx/powermgr/#wake-up-method) but be aware that, at the time of writing, this capability requires the relative recent MX version 9.2+ which is not available on all devices. 

If your device is in standby, you can (of course) press the power key to wake it up, after which the scan can be captured even when the keyguard (password screen) is shown.

## Approach overview
1.  Use DataWedge to start an Android service whenever a barcode is scanned.  Unlike a broadcast message the receiving application does not need to be in the foreground.
2. Listen for the scan intent in a service exported from the recipient application
3. Process the scan in the recipient service, accounting for Oreo background restrictions.

### 1. Use DataWedge to start an Android service whenever a barcode is scanned.

This article assumes familiarity with Zebra's DataWedge tool as well as the DataWedge profile mechanism.  For an overview of DataWedge, please refer to the [DataWedge Techdocs page](https://techdocs.zebra.com/datawedge/latest/guide/overview/)

The aim is to have DataWedge start a service when the application is in the background, this means we will not know the foreground application.  Since we do not know the foreground application then both the **default** and **Launcher** profiles need to be modified.  Typically the default profile on DataWedge is called **Profile0 (default)**

Modify both the default and launcher DataWedge profiles as follows:
- Barcode Input Enabled
- Intent Output Enabled
- Intent Output action: com.zebra.backgroundscan.ACTION (this action needs to match the action we define in the application manifest for the service)
- Intent delivery: Send via startService
- Intent Output 'Use startForegroundService on failure' set to true.  (This gives the maximum compatibility for both Oreo+ and pre-Oreo Android versions.)

Input settings:

![Default Input](https://raw.githubusercontent.com/darryncampbell/DataWedge-Background-Scanning/master/screenshots/default-input.jpg)
![Launcher Input](https://raw.githubusercontent.com/darryncampbell/DataWedge-Background-Scanning/master/screenshots/launcher-input.jpg)

Output settings:

![Default Output](https://raw.githubusercontent.com/darryncampbell/DataWedge-Background-Scanning/master/screenshots/default-output.jpg)
![Launcher Output](https://raw.githubusercontent.com/darryncampbell/DataWedge-Background-Scanning/master/screenshots/launcher-output.jpg)

### 2. Listen for the scan intent in a service exported from the application

Create an Android service in your application and modify the AndroidManifest.xml to listen for the Intent sent by DataWedge:

```xml
<service
    android:name=".ListeningService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.zebra.backgroundscan.ACTION" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</service>
```

From the Android service, extract the received scan data.  In the case of an IntentService:

```java
@Override
protected void onHandleIntent(Intent intent) {
    if (intent.getAction().equals("com.zebra.backgroundscan.ACTION")) {
        String scanData = intent.getStringExtra("com.symbol.datawedge.data_string");
        //  process scanData
        ...
    }
}
```


### 3. Process the scan in the recipient serivce, accounting for Oreo background restrictions

On Android Oreo and above, the amount of work you can do in a background service is *very* limited, therefore it makes sense to promote this service to a foreground service, at least whilst the scan is being processed.

You need to declare that you are using a foregound service in the AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

And then promote the service to a foreground service with an accompanying notification

```java
createNotificationChannel();
Intent notificationIntent = new Intent(this, MainActivity.class);
PendingIntent pendingIntent = PendingIntent.getActivity(this,
    0, notificationIntent, 0);
Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
.setContentTitle("DataWedge Background Scanning")
.setContentText("barcode processing")
.setSmallIcon(R.drawable.ic_launcher_foreground)
.setContentIntent(pendingIntent)
.build();
startForeground(1, notification);
```

The sample application processes the barcode so quickly that you will not see the noticiation but doing the above will make it work reliably on Android Oreo+

Finally, actually do work to process the scan.  This might be communicating with a backend or updating a database but the sample application shows a toast and does some text-to-speech.

```java
showToast("Scanned: " + scanData);
```

### Run the application

Install this application on a Zebra mobile device, configure DataWedge as described above and scan a barcode from any activity.  You should see a toast appear and text-to-speech should read the first 3 digits of the barcode

![Running 1](https://raw.githubusercontent.com/darryncampbell/DataWedge-Background-Scanning/master/screenshots/launcher.jpg)
![Running 2](https://raw.githubusercontent.com/darryncampbell/DataWedge-Background-Scanning/master/screenshots/default.jpg)