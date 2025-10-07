package com.drebyte.fingerprint_sdk;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.util.Base64;

import com.HZFINGER.LAPI;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class FingerprintSdkPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    private MethodChannel channel;
    private EventChannel eventChannel;
    private Activity activity;
    private LAPI lapi;
    private boolean simulatorMode = false;
    private long deviceHandle = 0;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "fingerprint_sdk");
        eventChannel = new EventChannel(binding.getBinaryMessenger(), "fingerprint_sdk/events");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "initialize":
                simulatorMode = call.argument("simulator");
                lapi = new LAPI(activity);
                result.success(true);
                break;

            case "openDevice":
                if (simulatorMode) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("status", "simulator");
                    result.success(info);
                    return;
                }
                deviceHandle = lapi.OpenDeviceEx(LAPI.SCSI_MODE);
                if (deviceHandle > 0) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("status", "device_opened");
                    info.put("handle", deviceHandle);
                    result.success(info);
                } else {
                    result.error("DEVICE_ERROR", "Failed to open device", null);
                }
                break;

            case "closeDevice":
                if (!simulatorMode && deviceHandle > 0) {
                    boolean closed = lapi.CloseDeviceEx(deviceHandle) == 1;
                    result.success(closed);
                    return;
                }
                result.success(true);
                break;

            case "captureImage":
                if (simulatorMode) {
                    result.success("SIMULATOR_IMAGE_BASE64");
                    return;
                }
                try {
                    byte[] image = new byte[LAPI.IMAGE_SIZE];
                    int ret = lapi.GetImage(deviceHandle, image);
                    if (ret > 0) {
                        String base64Image = Base64.encodeToString(image, Base64.NO_WRAP);
                        result.success(base64Image);
                    } else {
                        result.error("CAPTURE_ERROR", "Failed to capture fingerprint", null);
                    }
                } catch (Exception e) {
                    result.error("CAPTURE_EXCEPTION", e.getMessage(), null);
                }
                break;

            case "createISOTemplate":
                String imgBase64 = call.argument("imageBase64");
                if (imgBase64 == null) {
                    result.error("INVALID_ARGUMENT", "imageBase64 is null", null);
                    return;
                }
                byte[] imgBytes = Base64.decode(imgBase64, Base64.NO_WRAP);
                byte[] isoTemplate = new byte[LAPI.FPINFO_SIZE];
                int isoRet = lapi.CreateISOTemplate(deviceHandle, imgBytes, isoTemplate);
                if (isoRet > 0) {
                    result.success(Map.of("template", Base64.encodeToString(isoTemplate, Base64.NO_WRAP)));
                } else {
                    result.error("ISO_ERROR", "Failed to create ISO template", null);
                }
                break;

            case "createANSITemplate":
                String ansiImgBase64 = call.argument("imageBase64");
                if (ansiImgBase64 == null) {
                    result.error("INVALID_ARGUMENT", "imageBase64 is null", null);
                    return;
                }
                byte[] ansiImgBytes = Base64.decode(ansiImgBase64, Base64.NO_WRAP);
                byte[] ansiTemplate = new byte[LAPI.FPINFO_SIZE];
                int ansiRet = lapi.CreateANSITemplate(deviceHandle, ansiImgBytes, ansiTemplate);
                if (ansiRet > 0) {
                    result.success(Map.of("template", Base64.encodeToString(ansiTemplate, Base64.NO_WRAP)));
                } else {
                    result.error("ANSI_ERROR", "Failed to create ANSI template", null);
                }
                break;

            case "compareTemplates":
                String tmpl1 = call.argument("template1");
                String tmpl2 = call.argument("template2");
                if (tmpl1 == null || tmpl2 == null) {
                    result.error("INVALID_ARGUMENT", "Templates are null", null);
                    return;
                }
                byte[] tmpl1Bytes = Base64.decode(tmpl1, Base64.NO_WRAP);
                byte[] tmpl2Bytes = Base64.decode(tmpl2, Base64.NO_WRAP);
                int score = lapi.CompareTemplates(deviceHandle, tmpl1Bytes, tmpl2Bytes);
                result.success(score);
                break;

            default:
                result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {}
    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {}
    @Override
    public void onDetachedFromActivity() {}
}