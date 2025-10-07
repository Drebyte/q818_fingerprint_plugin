import 'dart:async';
import 'package:flutter/services.dart';

class FingerprintSdk {
  static const MethodChannel _channel = MethodChannel('fingerprint_sdk');
  static const EventChannel _eventChannel = EventChannel('fingerprint_sdk/events');

  static Stream<Map<String, dynamic>>? _eventStream;

  static Future<bool> initialize({bool simulator = false}) async {
    return await _channel.invokeMethod<bool>(
          'initialize',
          {'simulator': simulator},
        ) ??
        false;
  }

  static Future<Map<String, dynamic>?> openDevice() async {
    final res = await _channel.invokeMethod('openDevice');
    return res is Map ? Map<String, dynamic>.from(res) : null;
  }

  static Future<bool> closeDevice() async {
    return await _channel.invokeMethod<bool>('closeDevice') ?? false;
  }

  static Future<String?> captureImage() async {
    return await _channel.invokeMethod<String>('captureImage');
  }

  static Future<Map<String, String>?> createISOTemplate(String imageBase64) async {
    final res = await _channel.invokeMethod(
      'createISOTemplate',
      {'imageBase64': imageBase64},
    );
    return res is Map ? Map<String, String>.from(res) : null;
  }

  static Future<Map<String, String>?> createANSITemplate(String imageBase64) async {
    final res = await _channel.invokeMethod(
      'createANSITemplate',
      {'imageBase64': imageBase64},
    );
    return res is Map ? Map<String, String>.from(res) : null;
  }

  static Future<int?> compareTemplates(String template1, String template2) async {
    return await _channel.invokeMethod<int>(
      'compareTemplates',
      {
        'template1': template1,
        'template2': template2,
      },
    );
  }

  static Stream<Map<String, dynamic>> get events {
    _eventStream ??= _eventChannel
        .receiveBroadcastStream()
        .map((event) => Map<String, dynamic>.from(event as Map));
    return _eventStream!;
  }
}