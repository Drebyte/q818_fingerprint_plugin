import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:fingerprint_sdk/fingerprint_sdk.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 🔹 Catch Flutter errors
  FlutterError.onError = (FlutterErrorDetails details) {
    debugPrint('❌ FlutterError: ${details.exception}');
    debugPrint('Stack trace:\n${details.stack}');
  };

  // 🔹 Catch all async errors
  PlatformDispatcher.instance.onError = (error, stack) {
    debugPrint('🚨 Uncaught async error: $error');
    debugPrint('Stack trace:\n$stack');
    return true;
  };

  debugPrint('🚀 App starting... preparing to launch SDK Demo');

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    debugPrint('🧩 Building MyApp widget');
    return MaterialApp(
      title: 'Fingerprint SDK Demo',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const FingerprintDemoPage(),
    );
  }
}

class FingerprintDemoPage extends StatefulWidget {
  const FingerprintDemoPage({super.key});

  @override
  FingerprintDemoPageState createState() => FingerprintDemoPageState();
}

class FingerprintDemoPageState extends State<FingerprintDemoPage> {
  String _status = "Launching...";
  bool _isSimulator = true;
  String? _captureImageBase64;
  Map<String, String>? _isoTemplate;
  Map<String, String>? _ansiTemplate;

  @override
  void initState() {
    super.initState();
    debugPrint('🔧 FingerprintDemoPageState initialized');
    _initializeSdk();
  }

  Future<void> _initializeSdk() async {
    debugPrint('🧠 Starting SDK initialization (simulator=$_isSimulator)...');
    setState(() => _status = "Initializing SDK...");

    try {
      final initialized = await FingerprintSdk.initialize(simulator: true);
      debugPrint('✅ SDK initialize() returned: $initialized');
      setState(() {
        _isSimulator = true;
        _status = initialized
            ? "SDK Initialized (Simulator Mode)"
            : "SDK Initialization Failed";
      });
    } catch (e, stack) {
      debugPrint('❌ Error during SDK initialization: $e');
      debugPrint('Stack trace:\n$stack');
      setState(() => _status = "Error initializing SDK: $e");
    }
  }

  Future<void> _captureImage() async {
    debugPrint('📸 Capture fingerprint triggered');
    if (_isSimulator) {
      debugPrint('🧪 Simulator mode active, simulating capture');
      setState(() {
        _captureImageBase64 = "SIMULATED_IMAGE_BASE64";
        _status = "Simulator: Fingerprint image captured.";
      });
      return;
    }

    try {
      setState(() => _status = "Capturing fingerprint...");
      final imageBase64 = await FingerprintSdk.captureImage();
      debugPrint('📷 Capture result: ${imageBase64 != null ? "OK" : "NULL"}');
      setState(() {
        _captureImageBase64 = imageBase64;
        _status =
            imageBase64 != null ? "Fingerprint captured" : "Capture failed";
      });
    } catch (e, stack) {
      debugPrint('❌ Error capturing fingerprint: $e');
      debugPrint('Stack trace:\n$stack');
      setState(() => _status = "Error capturing fingerprint: $e");
    }
  }

  Future<void> _createTemplates() async {
    debugPrint('🧩 Creating fingerprint templates');
    if (_captureImageBase64 == null) {
      setState(() => _status = "No fingerprint image captured");
      return;
    }

    try {
      if (_isSimulator) {
        debugPrint('🧪 Simulator: generating fake templates');
        _isoTemplate = {'template': 'SIMULATED_ISO_TEMPLATE'};
        _ansiTemplate = {'template': 'SIMULATED_ANSI_TEMPLATE'};
        setState(() => _status = "Templates created (Simulator)");
        return;
      }

      setState(() => _status = "Creating templates...");
      _isoTemplate = await FingerprintSdk.createISOTemplate(_captureImageBase64!);
      _ansiTemplate = await FingerprintSdk.createANSITemplate(_captureImageBase64!);
      debugPrint('✅ Templates created successfully');
      setState(() => _status = "Templates created successfully");
    } catch (e, stack) {
      debugPrint('❌ Error creating templates: $e');
      debugPrint('Stack trace:\n$stack');
      setState(() => _status = "Error creating templates: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    debugPrint('🧱 Building FingerprintDemoPage UI');
    return Scaffold(
      appBar: AppBar(title: const Text("Fingerprint SDK Demo")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("Status: $_status"),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _initializeSdk,
              child: const Text("Re-Initialize SDK (Simulator)"),
            ),
            ElevatedButton(
              onPressed: _captureImage,
              child: const Text("Capture Image"),
            ),
            ElevatedButton(
              onPressed: _createTemplates,
              child: const Text("Create Templates"),
            ),
          ],
        ),
      ),
    );
  }
}