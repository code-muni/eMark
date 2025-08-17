# eMark - PDF Signing Application

<div align="center">
  <img src="src/main/resources/icons/logo.png" alt="eMark Logo" width="100">
  
  <p>
    <img src="https://img.shields.io/badge/Java-1.8%2B-007396?logo=java&logoColor=white" alt="Java 8+">
    <img src="https://img.shields.io/badge/License-AGPL%203.0-brightgreen" alt="License">
    <img src="https://img.shields.io/badge/Platform-Cross--Platform-brightgreen" alt="Cross-Platform">
    <img src="https://img.shields.io/badge/Version-1.0.0-brightgreen" alt="Version">
  </p>
</div>

## 📝 Overview

**eMark** is a robust, cross-platform desktop application for digitally signing PDF documents with support for multiple signing methods. It provides a user-friendly interface for secure document signing while maintaining the highest security standards.

## ✨ Features

- **Multiple Signing Methods**
  - Windows Certificate Store integration
  - PKCS#11/HSM support (multi-device support)
  - PFX/PKCS#12 file support
- **Cross-Platform**
  - Works on Windows, macOS, and Linux
  - Consistent UI experience across platforms
- **Advanced Security**
  - Support for hardware security modules (HSM)
  - Timestamping support
  - Password-protected PDF support
  - LTV (Lightweight Trust Verification) support
  - Timestamp Authority (TSA) support
- **User-Friendly Interface**
  - Modern, simple but powerful dark-themed UI
  - Visual signature placement and customization
  - Signature preview and configuration
  - Document preview
- **Open Source**
  - Open source codebase available for customization and contribution

## 🖼 Screenshots

Check out our [Image Gallery](docs/image-gallery.md) to see eMark in action with detailed screenshots of all the features and interface elements.

## 🚀 Getting Started

### Prerequisites

- **Java 8 (JDK 8) or later**
  - Windows: [Download Java](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
  - macOS: `brew install openjdk@8`
  - Linux: `sudo apt-get install openjdk-8-jdk`

### Installation

#### Option 1: Download Latest Release
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/code-muni/eMark?style=for-the-badge&color=blue)](https://github.com/code-muni/eMark/releases/latest)

1. Download the latest release from the [Releases](https://github.com/code-muni/eMark/releases) page
2. Select the appropriate installer for your operating system (Windows, macOS, or Linux)
3. Run the installer and follow the on-screen instructions

> **Note**: Each release includes platform-specific installers for easy setup on your operating system.

#### Option 2: Build from Source
```bash
git clone https://github.com/code-muni/eMark.git
cd eMark
mvn clean package
java -jar target/eMark-1.0-SNAPSHOT.jar
```

## 🖥 Usage

### Signing a PDF Document
1. Launch eMark
2. Click "Open PDF" and select your document
3. Click "Begin Sign" and select the signing area
4. Choose your certificate from the list
5. Enter your certificate password if required
6. Click "Sign" and choose where to save the signed document

### Supported Operations
- **Open PDF**: Open and view PDF documents
- **Sign Document**: Add digital signatures to PDFs
- **Save As**: Save signed documents with a new filename
- **Settings**: Configure application preferences and security settings

## 🛠 Troubleshooting

### Common Issues

**Java Version Error**
- Ensure you have Java 8 or later installed
- Run `java -version` to check your Java version

**PDF Loading Issues**
- Ensure the PDF is not corrupted
- Check if the PDF is password protected and provide the correct password

**Certificate Issues**
- Ensure your certificate is properly installed
- Check certificate expiration date
- Verify certificate trust chain

## 🤝 Contributing

We welcome contributions to eMark! Whether you want to report a bug, request a feature, or submit code changes, please follow our [Contributing Guidelines](CONTRIBUTING.md).

### Report a Bug / Request a Feature

Found a bug or have a feature idea? We'd love to hear about it!

1. **Search** existing issues to avoid duplicates
2. **Create a new issue** using the appropriate template:
   - 🐛 [Report a Bug](https://github.com/code-muni/eMark/issues/new?template=bug_report.md)
   - ✨ [Request a Feature](https://github.com/code-muni/eMark/issues/new?template=feature_request.md)
3. Fill in the template with as much detail as possible
4. Submit the issue and we'll review it as soon as possible

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

For support or queries, please open an issue on our [GitHub repository](https://github.com/code-muni).

---

<div align="center">
  Made with ❤️ by <a href="https://github.com/code-muni">CodeMuni</a>
</div>
