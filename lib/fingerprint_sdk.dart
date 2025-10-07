import 'dart:async';
import 'package:flutter/services.dart';

class FingerprintSdk {
  static const MethodChannel _channel =
      MethodChannel('fingerprint_sdk');
  static const EventChannel _eventChannel =
      EventChannel('fingerprint_sdk/events');

  static Stream<Map<String, dynamic>>? _eventStream;

  /// Initialize the SDK (simulator mode optional)
  static Future<bool> initialize({bool simulator = false}) async {
    return await _channel.invokeMethod<bool>(
          'initialize',
          {'simulator': simulator},
        ) ??
        false;
  }

  /// Get platform version
  static Future<String> getPlatformVersion() async {
    return await _channel.invokeMethod<String>('getPlatformVersion') ??
        "Unknown";
  }

  /// Open fingerprint device
  static Future<Map<String, dynamic>?> openDevice() async {
    final res = await _channel.invokeMethod('openDevice');
    if (res is Map) {
      return Map<String, dynamic>.from(res);
    }
    return null;
  }

  /// Close fingerprint device
  static Future<bool> closeDevice() async {
    return await _channel.invokeMethod<bool>('closeDevice') ?? false;
  }

  /// Capture fingerprint image (returns base64 encoded string)
  static Future<String?> captureImage() async {
    return await _channel.invokeMethod<String>('captureImage');
  }

  /// Create ISO fingerprint template
  static Future<Map<String, String>?> createISOTemplate(
      String imageBase64) async {
    final res = await _channel.invokeMethod('createISOTemplate', {
      'imageBase64': imageBase64,
    });
    if (res is Map) {
      return Map<String, String>.from(res);
    }
    return null;
  }

  /// Create ANSI fingerprint template
  static Future<Map<String, String>?> createANSITemplate(
      String imageBase64) async {
    final res = await _channel.invokeMethod('createANSITemplate', {
      'imageBase64': imageBase64,
    });
    if (res is Map) {
      return Map<String, String>.from(res);
    }
    return null;
  }

  /// Verify fingerprint
  static Future<String?> verifyFingerprint({
    required String regId,
    required int secLevel,
    bool checkLive = true,
  }) async {
    return await _channel.invokeMethod<String>('verifyFingerprint', {
      'regId': regId,
      'secLevel': secLevel,
      'checkLive': checkLive,
    });
  }

  /// Search fingerprint
  static Future<String?> searchFingerprint({
    required int secLevel,
    bool checkLive = true,
  }) async {
    return await _channel.invokeMethod<String>('searchFingerprint', {
      'secLevel': secLevel,
      'checkLive': checkLive,
    });
  }

  /// Stream of fingerprint events
  static Stream<Map<String, dynamic>> get events {
    _eventStream ??= _eventChannel
        .receiveBroadcastStream()
        .map((dynamic event) =>
            Map<String, dynamic>.from(event as Map<dynamic, dynamic>));
    return _eventStream!;
  }
}