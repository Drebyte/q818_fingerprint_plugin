import 'package:flutter/services.dart';

class FingerprintSdk {
  static const MethodChannel _channel = MethodChannel('fingerprint_sdk');

  Future<String?> getPlatformVersion() async {
    return await _channel.invokeMethod<String>('getPlatformVersion');
  }

  Future<Map<String, dynamic>?> openDevice() async {
    final res = await _channel.invokeMethod('openDevice');
    if (res is Map) {
      return Map<String, dynamic>.from(res);
    }
    return null;
  }

  Future<String?> captureImage() async {
    return await _channel.invokeMethod<String>('captureImage');
  }

  Future<Map<String, String>?> createISOTemplate(String imageBase64) async {
    final res = await _channel.invokeMethod('createISOTemplate', {
      "image": imageBase64,
    });
    if (res is Map) {
      return Map<String, String>.from(res);
    }
    return null;
  }

  Future<Map<String, String>?> createANSITemplate(String imageBase64) async {
    final res = await _channel.invokeMethod('createANSITemplate', {
      "image": imageBase64,
    });
    if (res is Map) {
      return Map<String, String>.from(res);
    }
    return null;
  }

  Future<int?> compareTemplates(String t1, String t2) async {
    return await _channel.invokeMethod<int>('compareTemplates', {
      "t1": t1,
      "t2": t2,
    });
  }

  Future<int?> closeDevice() async {
    return await _channel.invokeMethod<int>('closeDevice');
  }

  Future<void> toggleSimulatorMode(bool enable) async {
    await _channel.invokeMethod('toggleSimulatorMode', {"enable": enable});
  }
}