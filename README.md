# Q818 Device Fingerprint SDK Plugin for Flutter

This Flutter plugin provides a simple bridge to native Q818 fingerprint hardware SDKs using **Platform Channels**.  
It allows developers to capture fingerprint images, generate templates (ISO/ANSI), and perform template matching.  

---

## ✨ Features

- Open and close the fingerprint device
- Capture fingerprint images
- Generate **ISO** and **ANSI** templates
- Compare fingerprint templates for matching
- Works with custom hardware SDKs via native integration (Java/Kotlin, Swift/Objective-C)

---

## 📦 Installation

Add the dependency in your Flutter project:

```yaml
dependencies:
  fingerprint_sdk:
    path: ./fingerprint_sdk
```

Then run:

```yaml
flutter pub get
```

⸻

🚀 Usage

Import the plugin:

```yaml
import 'package:fingerprint_sdk/fingerprint_sdk.dart';
```

Example

```yaml
final sdk = FingerprintSdk();

// Open device
final handle = await sdk.openDevice();
print("Device Handle: $handle");

// Capture fingerprint image
final imageBase64 = await sdk.captureImage();
print("Captured Image (Base64): $imageBase64");

// Create ISO template
final isoTemplate = await sdk.createISOTemplate(imageBase64!);
print("ISO Template: $isoTemplate");

// Create ANSI template
final ansiTemplate = await sdk.createANSITemplate(imageBase64);
print("ANSI Template: $ansiTemplate");

// Compare templates
final score = await sdk.compareTemplates(isoTemplate!, ansiTemplate!);
print("Match Score: $score");

// Close device
await sdk.closeDevice();
 ```

⸻

## 📂 Project Structure

```yaml
fingerprint_sdk/
├─ .dart_tool/
│  ├─ extension_discovery/
│  │  ├─ README.md
│  │  └─ vs_code.json
│  ├─ package_config.json
│  ├─ package_graph.json
│  └─ version
├─ android/
│  ├─ gradle/
│  │  └─ wrapper/
│  │     └─ gradle-wrapper.properties
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/
│  │  │  │  └─ com/
│  │  │  │     ├─ example/
│  │  │  │     │  └─ fingerprint_sdk/
│  │  │  │     │     └─ FingerprintSdkPlugin.java
│  │  │  │     └─ HZFINGER/
│  │  │  │        ├─ HAPI.java
│  │  │  │        ├─ HostUsb.java
│  │  │  │        └─ LAPI.java
│  │  │  ├─ jniLibs/
│  │  │  │  ├─ arm64-v8a/
│  │  │  │  │  ├─ libbiofp_e_lapi.so
│  │  │  │  │  ├─ libcheckLive.so
│  │  │  │  │  └─ libFingerILA.so
│  │  │  │  ├─ armeabi/
│  │  │  │  │  ├─ libbiofp_e_lapi.so
│  │  │  │  │  ├─ libcheckLive.so
│  │  │  │  │  └─ libFingerILA.so
│  │  │  │  └─ armeabi-v7a/
│  │  │  │     ├─ libbiofp_e_lapi.so
│  │  │  │     ├─ libcheckLive.so
│  │  │  │     └─ libFingerILA.so
│  │  │  ├─ kotlin/
│  │  │  │  └─ com/
│  │  │  │     └─ example/
│  │  │  │        └─ fingerprint_sdk/
│  │  │  │           ├─ FingerprintSdkPlugin.kt
│  │  │  │           └─ FingerprintSdkPlugin.txt
│  │  │  └─ AndroidManifest.xml
│  │  └─ test/
│  │     └─ kotlin/
│  │        └─ com/
│  │           └─ example/
│  │              └─ fingerprint_sdk/
│  │                 └─ FingerprintSdkPluginTest.kt
│  ├─ .gitignore
│  ├─ build.gradle
│  ├─ fingerprint_sdk_android.iml
│  ├─ local.properties
│  └─ settings.gradle
├─ example/
├─ lib/
│  ├─ fingerprint_sdk_method_channel.dart
│  ├─ fingerprint_sdk_platform_interface.dart
│  └─ fingerprint_sdk.dart
├─ test/
│  ├─ fingerprint_sdk_method_channel_test.dart
│  └─ fingerprint_sdk_test.dart
├─ .gitignore
├─ .metadata
├─ analysis_options.yaml
├─ CHANGELOG.md
├─ fingerprint_sdk.iml
├─ LICENSE
├─ pubspec.lock
├─ pubspec.yaml
└─ README.md
```

⸻

## ⚙️ API Reference

| Method                 | Parameters               | Returns          | Description                                       |
|------------------------|--------------------------|------------------|-------------------------------------------------|
| `openDevice()`         | none                     | `Future<int?>`   | Opens the fingerprint device, returning a handle or null if it fails. |
| `captureImage()`       | none                     | `Future<String?>`| Captures a fingerprint image and returns it as a Base64-encoded string. |
| `createISOTemplate()`  | `String imageBase64`      | `Future<String?>`| Generates an ISO fingerprint template from a captured image. |
| `createANSITemplate()` | `String imageBase64`      | `Future<String?>`| Generates an ANSI fingerprint template from a captured image. |
| `compareTemplates()`   | `String t1, String t2`    | `Future<int?>`   | Compares two templates and returns a similarity score. |
| `closeDevice()`        | none                     | `Future<int?>`   | Closes the fingerprint device.                   |

## 🔧 Platform Support

- Android: Implemented via `FingerprintSdkPlugin.java`
- iOS: Implementation pending (Swift/Objective-C)

## 🛠 Development Notes

- Ensure native SDK libraries required by your fingerprint hardware (such as `.so`, `.dll`, or `.framework` files) are correctly integrated into your Android/iOS projects.
- This plugin communicates between Dart and native code using Flutter’s MethodChannel (`fingerprint_sdk`).
- The ISO and ANSI templates are returned as Base64-encoded strings for easy storage and transmission.

## 🖼 Workflow Overview

Fingerprint Device  
↓  
Capture Image (Base64)  
↓  
Generate Template (ISO / ANSI)  
↓  
Store or Compare Templates  

## 📄 License

This project is licensed under the MIT License. Use it at your own risk and ensure compliance with your hardware vendor’s SDK terms.
