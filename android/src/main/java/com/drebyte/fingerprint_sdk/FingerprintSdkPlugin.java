package com.drebyte.fingerprint_sdk;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.HZFINGER.HostUsb;
import com.HZFINGER.HAPI;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class FingerprintSdkPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    private MethodChannel channel;
    private Activity activity;
    private boolean simulatorMode = true;
    private long deviceHandle = 0L;
    private static final String TAG = "FingerprintSdkPlugin";

    private HostUsb mHostUsb;
    private HAPI mHapi;

    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "fingerprint_sdk");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        requestPermissions();

        mHapi = new HAPI(activity, new Handler(Looper.getMainLooper()));
        mHostUsb = new HostUsb(activity);
    }

    private void requestPermissions() {
        boolean allGranted = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (!allGranted) {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        closeDevice();
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        try {
            switch (call.method) {
                case "getPlatformVersion":
                    result.success("Android " + android.os.Build.VERSION.RELEASE);
                    break;

                case "toggleSimulatorMode":
                    Boolean enable = call.argument("enable");
                    if (enable != null) {
                        simulatorMode = enable;
                        result.success(true);
                    } else {
                        result.error("ARG_ERROR", "Missing 'enable' argument", null);
                    }
                    break;

                case "openDevice":
                    result.success(openDevice());
                    break;

                case "captureImage":
                    result.success(captureImage());
                    break;

                case "createISOTemplate":
                    result.success(createTemplate("ISO", call.argument("image")));
                    break;

                case "createANSITemplate":
                    result.success(createTemplate("ANSI", call.argument("image")));
                    break;

                case "compareTemplates":
                    result.success(compareTemplates(call.argument("t1"), call.argument("t2")));
                    break;

                case "verifyFingerprint":
                    result.success(verifyFingerprint(
                            call.argument("regId"),
                            call.argument("secLevel"),
                            call.argument("checkLive")
                    ));
                    break;

                case "searchFingerprint":
                    result.success(searchFingerprint(
                            call.argument("secLevel"),
                            call.argument("checkLive")
                    ));
                    break;

                case "deleteRecord":
                    result.success(deleteRecord(call.argument("regId")));
                    break;

                case "refreshDatabase":
                    refreshDatabase();
                    result.success(true);
                    break;

                case "closeDevice":
                    closeDevice();
                    result.success(true);
                    break;

                default:
                    result.notImplemented();
                    break;
            }
        } catch (Exception e) {
            result.error("PLUGIN_EXCEPTION", e.getMessage(), null);
        }
    }

    private Map<String, Object> openDevice() {
        Map<String, Object> deviceInfo = new HashMap<>();

        if (simulatorMode) {
            deviceHandle = 12345L; // Simulator dummy
            deviceInfo.put("handle", deviceHandle);
            deviceInfo.put("hardwareAvailable", false);
        } else {
            try {
                if (mHapi != null) {
                    deviceHandle = mHapi.openDevice();
                } else if (mHostUsb != null) {
                    deviceHandle = mHostUsb.open();
                }

                boolean hardwareAvailable = deviceHandle != 0;
                deviceInfo.put("handle", deviceHandle);
                deviceInfo.put("hardwareAvailable", hardwareAvailable);

            } catch (Exception e) {
                Log.e(TAG, "Error opening device: " + e.getMessage());
                deviceInfo.put("handle", 0);
                deviceInfo.put("hardwareAvailable", false);
            }
        }

        return deviceInfo;
    }

    private void closeDevice() {
        try {
            if (!simulatorMode && deviceHandle != 0) {
                if (mHapi != null) mHapi.closeDevice(deviceHandle);
                if (mHostUsb != null) mHostUsb.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing device: " + e.getMessage());
        }
        deviceHandle = 0;
    }

    private String captureImage() {
        if (simulatorMode) {
            return Base64.encodeToString("fake_image".getBytes(), Base64.NO_WRAP);
        }

        try {
            return mHapi.captureImage(deviceHandle);
        } catch (Exception e) {
            Log.e(TAG, "Capture image error: " + e.getMessage());
            return null;
        }
    }

    private Map<String, String> createTemplate(String mode, String imageBase64) {
        Map<String, String> templateResult = new HashMap<>();
        if (simulatorMode) {
            templateResult.put("template", Base64.encodeToString((mode + "_template").getBytes(), Base64.NO_WRAP));
            templateResult.put("mode", mode);
            return templateResult;
        }

        try {
            String template = mode.equals("ISO") ?
                    mHapi.createISOTemplate(deviceHandle, imageBase64) :
                    mHapi.createANSITemplate(deviceHandle, imageBase64);

            templateResult.put("template", template);
            templateResult.put("mode", mode);
        } catch (Exception e) {
            Log.e(TAG, "Template creation error: " + e.getMessage());
        }

        return templateResult;
    }

    private int compareTemplates(String t1, String t2) {
        if (simulatorMode) return 100;

        try {
            return mHapi.compareTemplates(deviceHandle, t1, t2);
        } catch (Exception e) {
            Log.e(TAG, "Compare templates error: " + e.getMessage());
            return 0;
        }
    }

    private Map<String, Object> verifyFingerprint(String regId, Integer secLevel, Boolean checkLive) {
        Map<String, Object> result = new HashMap<>();

        if (simulatorMode) {
            result.put("regId", regId);
            result.put("matchScore", 95);
            result.put("status", "verified");
            result.put("checkLive", checkLive);
            return result;
        }

        try {
            result = mHapi.verifyFingerprint(deviceHandle, regId, secLevel, checkLive);
        } catch (Exception e) {
            Log.e(TAG, "Verify fingerprint error: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> searchFingerprint(Integer secLevel, Boolean checkLive) {
        Map<String, Object> result = new HashMap<>();

        if (simulatorMode) {
            result.put("matchFound", true);
            result.put("matchedId", "user_123");
            result.put("score", 88);
            result.put("checkLive", checkLive);
            return result;
        }

        try {
            result = mHapi.searchFingerprint(deviceHandle, secLevel, checkLive);
        } catch (Exception e) {
            Log.e(TAG, "Search fingerprint error: " + e.getMessage());
        }

        return result;
    }

    private boolean deleteRecord(String regId) {
        if (simulatorMode) return true;

        try {
            return mHapi.deleteRecord(deviceHandle, regId);
        } catch (Exception e) {
            Log.e(TAG, "Delete record error: " + e.getMessage());
            return false;
        }
    }

    private void refreshDatabase() {
        if (simulatorMode) {
            Log.d(TAG, "Simulator: Database refreshed");
            return;
        }

        try {
            mHapi.refreshDatabase(deviceHandle);
        } catch (Exception e) {
            Log.e(TAG, "Refresh database error: " + e.getMessage());
        }
    }
}