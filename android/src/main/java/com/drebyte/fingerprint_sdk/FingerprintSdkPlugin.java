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

import com.HZFINGER.HostUsb;
import com.HZFINGER.HAPI;
import com.HZFINGER.LAPI;

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
                    result.success(100); // Placeholder match score
                    break;

                case "closeDevice":
                    deviceHandle = 0;
                    result.success(1);
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
            deviceHandle = 12345L;
            deviceInfo.put("handle", deviceHandle);
            deviceInfo.put("hardwareAvailable", false);
        } else {
            deviceHandle = 67890L; // Example handle
            deviceInfo.put("handle", deviceHandle);
            deviceInfo.put("hardwareAvailable", true);
        }
        return deviceInfo;
    }

    private String captureImage() {
        return Base64.encodeToString("fake_image".getBytes(), Base64.NO_WRAP);
    }

    private Map<String, String> createTemplate(String mode, String imageBase64) {
        Map<String, String> templateResult = new HashMap<>();
        String template = Base64.encodeToString((mode + "_template").getBytes(), Base64.NO_WRAP);
        templateResult.put("template", template);
        templateResult.put("mode", mode);
        return templateResult;
    }
}