package com.drebyte.fingerprint_sdk;

import android.app.Activity;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import com.HZFINGER.HostUsb;
import com.HZFINGER.LAPI;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class FingerprintSdkPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    private static final String TAG = "FingerprintSdkPlugin";

    private MethodChannel methodChannel;
    private EventChannel eventChannel;
    private Activity activity;
    private static LAPI lapi;
    private boolean simulator = false;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel = new MethodChannel(binding.getBinaryMessenger(), "fingerprint_sdk");
        methodChannel.setMethodCallHandler(this);

        eventChannel = new EventChannel(binding.getBinaryMessenger(), "fingerprint_sdk/events");
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                // Could send events from hardware here
            }
            @Override
            public void onCancel(Object arguments) {}
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        lapi = new LAPI(activity);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        lapi = new LAPI(activity);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        try {
            switch (call.method) {
                case "initialize":
                    simulator = call.argument("simulator");
                    result.success(initialize(simulator));
                    break;

                case "getPlatformVersion":
                    result.success("Android " + Build.VERSION.RELEASE);
                    break;

                case "openDevice":
                    result.success(openDevice());
                    break;

                case "closeDevice":
                    result.success(closeDevice());
                    break;

                case "captureImage":
                    result.success(captureImage());
                    break;

                case "createISOTemplate":
                    String isoBase64 = call.argument("imageBase64");
                    result.success(createTemplate(isoBase64, true));
                    break;

                case "createANSITemplate":
                    String ansiBase64 = call.argument("imageBase64");
                    result.success(createTemplate(ansiBase64, false));
                    break;

                case "verifyFingerprint":
                    String regId = call.argument("regId");
                    int secLevel = call.argument("secLevel");
                    boolean checkLive = call.argument("checkLive");
                    result.success(verifyFingerprint(regId, secLevel, checkLive));
                    break;

                case "searchFingerprint":
                    int level = call.argument("secLevel");
                    boolean live = call.argument("checkLive");
                    result.success(searchFingerprint(level, live));
                    break;

                default:
                    result.notImplemented();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            result.error("ERROR", e.getMessage(), null);
        }
    }

    private boolean initialize(boolean simulatorMode) {
        this.simulator = simulatorMode;
        Log.i(TAG, "Fingerprint SDK initialized in " + (simulator ? "SIMULATOR" : "HARDWARE") + " mode");
        return true;
    }

    private Map<String, Object> openDevice() {
        Map<String, Object> res = new HashMap<>();
        if (simulator) {
            res.put("handle", 12345L);
            res.put("hardwareAvailable", true);
            return res;
        }

        long handle = lapi.OpenDeviceEx(LAPI.SCSI_MODE);
        res.put("handle", handle);
        res.put("hardwareAvailable", handle != 0);
        return res;
    }

    private boolean closeDevice() {
        if (simulator) return true;
        return lapi.CloseDeviceEx(0) == 1;
    }

    private String captureImage() {
        if (simulator) {
            return Base64.encodeToString(new byte[256 * 360], Base64.DEFAULT);
        }
        byte[] image = new byte[LAPI.IMAGE_SIZE];
        int ret = lapi.GetImage(0, image);
        if (ret > 0) {
            return Base64.encodeToString(image, Base64.DEFAULT);
        }
        return null;
    }

    private Map<String, String> createTemplate(String imageBase64, boolean iso) {
        Map<String, String> result = new HashMap<>();
        if (simulator) {
            result.put("template", Base64.encodeToString(new byte[512], Base64.DEFAULT));
            result.put("score", "100");
            return result;
        }

        byte[] image = Base64.decode(imageBase64, Base64.DEFAULT);
        byte[] template = new byte[LAPI.FPINFO_SIZE];
        int ret = iso
            ? lapi.CreateISOTemplate(0, image, template)
            : lapi.CreateANSITemplate(0, image, template);

        result.put("template", Base64.encodeToString(template, Base64.DEFAULT));
        result.put("score", String.valueOf(ret));
        return result;
    }

    private String verifyFingerprint(String regId, int secLevel, boolean checkLive) {
        if (simulator) return "SIMULATOR_VERIFY_SUCCESS";
        // TODO: Implement actual hardware verification logic
        return "VERIFY_SUCCESS";
    }

    private String searchFingerprint(int secLevel, boolean checkLive) {
        if (simulator) return "SIMULATOR_SEARCH_FOUND";
        // TODO: Implement actual hardware search logic
        return "SEARCH_SUCCESS";
    }
}