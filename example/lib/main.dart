import 'package:flutter/material.dart';
import 'package:fingerprint_sdk/fingerprint_sdk.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: FingerprintTestPage(),
    );
  }
}

class FingerprintTestPage extends StatefulWidget {
  const FingerprintTestPage({super.key});

  @override
  _FingerprintTestPageState createState() => _FingerprintTestPageState();
}

class _FingerprintTestPageState extends State<FingerprintTestPage> {
  String status = "Idle";
  String? template1;
  String? template2;
  bool simulatorMode = true;

  @override
  void initState() {
    super.initState();
    initializeSdk();
  }

  Future<void> initializeSdk() async {
    bool init = await FingerprintSdk.initialize(simulator: simulatorMode);
    setState(() => status = init ? "✅ SDK Initialized" : "❌ Initialization Failed");
  }

  Future<void> openDevice() async {
    final res = await FingerprintSdk.openDevice();
    if (res != null) {
      setState(() => status = "Device Opened: ${res['hardwareAvailable']}");
    } else {
      setState(() => status = "Failed to open device");
    }
  }

  Future<void> captureAndCreate(bool isFirst) async {
    setState(() => status = "Capturing image...");
    final img = await FingerprintSdk.captureImage();
    if (img == null) {
      setState(() => status = "Image capture failed");
      return;
    }

    setState(() => status = "Creating ISO template...");
    final tmpl = await FingerprintSdk.createISOTemplate(img);
    if (tmpl == null) {
      setState(() => status = "Template creation failed");
      return;
    }

    setState(() {
      if (isFirst) {
        template1 = tmpl['template'];
        status = "Template 1 created";
      } else {
        template2 = tmpl['template'];
        status = "Template 2 created";
      }
    });
  }

  Future<void> compareTemplates() async {
    if (template1 == null || template2 == null) {
      setState(() => status = "⚠️ Both templates required");
      return;
    }
    final score = await FingerprintSdk.compareTemplates(template1!, template2!);
    setState(() => status = "Match score: $score");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Fingerprint SDK Example")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Text("Status: $status", style: const TextStyle(fontSize: 18)),
            const SizedBox(height: 20),
            ElevatedButton(onPressed: openDevice, child: const Text("Open Device")),
            ElevatedButton(onPressed: () => captureAndCreate(true), child: const Text("Capture Template 1")),
            ElevatedButton(onPressed: () => captureAndCreate(false), child: const Text("Capture Template 2")),
            ElevatedButton(onPressed: compareTemplates, child: const Text("Compare Templates")),
            const SizedBox(height: 20),
            SwitchListTile(
              title: const Text("Simulator Mode"),
              value: simulatorMode,
              onChanged: (val) {
                setState(() => simulatorMode = val);
                initializeSdk();
              },
            ),
          ],
        ),
      ),
    );
  }
}