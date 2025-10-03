package com.example.fingerprint_sdk;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;
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
                    Map<String, Object> deviceInfo = new HashMap<>();
                    if (simulatorMode) {
                        deviceHandle = 12345L;
                        deviceInfo.put("handle", deviceHandle);
                        deviceInfo.put("hardwareAvailable", false);
                    } else {
                        // Real hardware opening logic goes here
                        deviceHandle = 67890L; // Example handle
                        deviceInfo.put("handle", deviceHandle);
                        deviceInfo.put("hardwareAvailable", true);
                    }
                    result.success(deviceInfo);
                    break;

                case "captureImage":
                    if (deviceHandle <= 0) {
                        result.error("NO_DEVICE", "Device not opened", null);
                        return;
                    }
                    String fakeImage = Base64.encodeToString("fake_image".getBytes(), Base64.NO_WRAP);
                    result.success(fakeImage);
                    break;

                case "createISOTemplate":
                    String isoTemplate = Base64.encodeToString("iso_template".getBytes(), Base64.NO_WRAP);
                    Map<String, String> isoResult = new HashMap<>();
                    isoResult.put("template", isoTemplate);
                    isoResult.put("mode", "ISO");
                    result.success(isoResult);
                    break;

                case "createANSITemplate":
                    String ansiTemplate = Base64.encodeToString("ansi_template".getBytes(), Base64.NO_WRAP);
                    Map<String, String> ansiResult = new HashMap<>();
                    ansiResult.put("template", ansiTemplate);
                    ansiResult.put("mode", "ANSI");
                    result.success(ansiResult);
                    break;

                case "compareTemplates":
                    result.success(100); // Fake match score
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
}