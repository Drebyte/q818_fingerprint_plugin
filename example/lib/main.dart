import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fingerprint_sdk/fingerprint_sdk.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Example main.dart — Fingerprint SDK Plugin Demo
/// This is a complete working example to guide developers
/// integrating the FingerprintSdkPlugin into their Flutter app.

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _output = 'Unknown';
  String _debugLog = '';
  bool _showDebug = false;
  bool _simulatorMode = true;
  final _fingerprintSdk = FingerprintSdk();

  @override
  void initState() {
    super.initState();
    _loadSimulatorMode();
    _initPlugin();
  }

  /// Load saved simulator mode preference
  Future<void> _loadSimulatorMode() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    bool mode = prefs.getBool('simulatorMode') ?? true;
    setState(() {
      _simulatorMode = mode;
    });
    await _fingerprintSdk.toggleSimulatorMode(mode);
  }

  /// Save simulator mode preference
  Future<void> _saveSimulatorMode(bool value) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setBool('simulatorMode', value);
  }

  /// Initialize plugin and check platform version
  Future<void> _initPlugin() async {
    String result;
    try {
      result =
          await _fingerprintSdk.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      result = 'Failed to get platform version.';
    }

    if (!mounted) return;
    setState(() {
      _output = "Platform Version: $result";
    });
  }

  /// Toggle simulator mode
  Future<void> _toggleSimulatorMode(bool value) async {
    await _fingerprintSdk.toggleSimulatorMode(value);
    await _saveSimulatorMode(value);
    setState(() {
      _simulatorMode = value;
    });
  }

  /// Safe substring helper
  String safeSubstring(String? s, int end) {
    if (s == null) return "";
    return s.length >= end ? s.substring(0, end) : s;
  }

  /// Run full fingerprint test
  Future<void> _runPluginTests() async {
    try {
      final device = await _fingerprintSdk.openDevice();
      final img = await _fingerprintSdk.captureImage();
      final iso = await _fingerprintSdk.createISOTemplate(img ?? "dummy_img");
      final ansi = await _fingerprintSdk.createANSITemplate(img ?? "dummy_img");
      final score = await _fingerprintSdk.compareTemplates(
        iso?['template'] ?? "",
        ansi?['template'] ?? "",
      );
      final closed = await _fingerprintSdk.closeDevice();

      String debug = """
Simulator Mode: $_simulatorMode
Device Handle: ${device?['handle']}
Hardware Available: ${device?['hardwareAvailable']}
Captured Image: ${safeSubstring(img, 50)}
ISO Template: ${safeSubstring(iso?['template'], 50)}
ISO Mode: ${iso?['mode']}
ANSI Template: ${safeSubstring(ansi?['template'], 50)}
ANSI Mode: ${ansi?['mode']}
Match Score: $score
Close Result: $closed
""";

      setState(() {
        _debugLog = debug;
        _output = """
Simulator Mode: $_simulatorMode
Device Handle: ${device?['handle']}
Match Score: $score
""";
      });
    } catch (e) {
      setState(() {
        _output = "Error running plugin tests: $e";
      });
    }
  }

  /// Copy debug log to clipboard
  void _copyDebugLog() {
    Clipboard.setData(ClipboardData(text: _debugLog));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text("Debug log copied to clipboard")),
    );
  }

  /// Builds debug log view
  Widget _buildDebugLog() {
    List<String> lines = _debugLog.split("\n");
    return ListView.builder(
      itemCount: lines.length,
      itemBuilder: (context, index) {
        String line = lines[index];
        bool isLabel = line.contains(":");

        return Container(
          color: index % 2 == 0 ? Colors.grey.shade100 : Colors.white,
          padding: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
          child: isLabel
              ? RichText(
                  text: TextSpan(
                    children: [
                      TextSpan(
                        text: line.split(":")[0] + ": ",
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          color: Colors.blue,
                        ),
                      ),
                      TextSpan(
                        text: line.substring(line.indexOf(":") + 1).trim(),
                        style: const TextStyle(
                          color: Colors.black87,
                        ),
                      ),
                    ],
                  ),
                )
              : Text(line),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Fingerprint SDK Example'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SwitchListTile(
                title: const Text("Simulator Mode"),
                subtitle: const Text("Toggle between real hardware and simulation"),
                value: _simulatorMode,
                onChanged: _toggleSimulatorMode,
              ),
              const SizedBox(height: 10),
              Text(
                _output,
                style: const TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _runPluginTests,
                child: const Text("Run Plugin Test"),
              ),
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    "Debug Log:",
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  Row(
                    children: [
                      IconButton(
                        icon: Icon(
                          _showDebug ? Icons.expand_less : Icons.expand_more,
                        ),
                        onPressed: () {
                          setState(() {
                            _showDebug = !_showDebug;
                          });
                        },
                      ),
                      IconButton(
                        icon: const Icon(Icons.copy),
                        tooltip: "Copy Debug Log",
                        onPressed: _copyDebugLog,
                      ),
                    ],
                  ),
                ],
              ),
              Expanded(
                child: AnimatedSize(
                  duration: const Duration(milliseconds: 300),
                  curve: Curves.easeInOut,
                  child: _showDebug
                      ? Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: Colors.grey[200],
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: _debugLog.isEmpty
                              ? const Text("No debug log available.")
                              : _buildDebugLog(),
                        )
                      : const SizedBox.shrink(),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}