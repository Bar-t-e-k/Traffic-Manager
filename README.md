# 🌐 Network Traffic Manager & Simulator (Mobile & Native Engine)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![C++](https://img.shields.io/badge/c++-%2300599C.svg?style=for-the-badge&logo=c%2B%2B&logoColor=white)
![CMake](https://img.shields.io/badge/CMake-%23008FBA.svg?style=for-the-badge&logo=cmake&logoColor=white)
![Cryptography](https://img.shields.io/badge/Security-AES--128-red?style=for-the-badge)

A high-performance, cross-platform network traffic router and simulation engine. 
This project demonstrates advanced systems engineering by bridging a modern Android UI (Jetpack Compose) with a custom, high-speed C++ networking core via JNI (Java Native Interface).

## ✨ Key Features & Engineering Highlights

*   **End-to-End Encryption (AES-128 CBC):** Integrated `tiny-AES-c` natively. All business payloads are encrypted/decrypted at the transport layer, ensuring military-grade data obfuscation before hitting the socket.
*   **Custom RUDP (Reliable UDP) Protocol:** Engineered a reliable transport layer over standard UDP featuring sequence numbering, ACK parsing, and a multi-threaded `RudpRetransmissionManager` with mutex-protected buffers.
*   **Multi-mode CLI Engine:** Run the C++ core in standalone modes using `--simulate` (internal traffic generator for stress testing) or `--server` (real-time UDP socket listener).
*   **Advanced Traffic Management:** Implements automated Policing (MTU size filtering) and Scheduling (priority-based queues) using efficient STL algorithms.
*   **Modern C++ Best Practices:** Extensive use of RAII for socket/memory lifecycle management, smart pointers (`std::unique_ptr`) to eliminate memory leaks, and custom exception handling.
*   **Android & JNI Integration:** Features a clean Kotlin MVVM architecture utilizing Coroutines, linked to the C++ core via a seamless JNI bridge (`native-lib.cpp`).

## 🛠 Technologies & Tools

*   **Native Engine:** C++17
*   **Mobile App:** Kotlin, Android NDK, Jetpack Compose
*   **Build System:** CMake (3.15+)
*   **Networking:** Custom RUDP over UDP Sockets (Winsock2 / POSIX)
*   **Testing:** Google Test (GTest) fetched automatically via CMake
*   **Environment:** Cross-platform (Android, Windows, Linux, macOS)

## 🏗️ Architecture overview

The project is strictly divided into two main modules to ensure cross-platform reusability of the engine:

```text
Traffic-Manager/
│
├── core/                       # ⚙️ Platform-agnostic C++ Engine
│   └── Traffic-Manager/
│       ├── CMakeLists.txt      # Build system for the engine
│       ├── aes.cpp / aes.h     # Cryptographic library
│       ├── crypto.hpp          # PKCS7 Padding & AES Pipeline
│       ├── rudpServer.hpp      # RUDP lifecycle & decryption
│       └── packetParser.hpp    # Business logic decoding
│       └── ...
│
└── android-app/                # 📱 Android UI & JNI Wrapper
    ├── app/src/main/cpp/
    │   ├── CMakeLists.txt      # Links Android to the 'core' directory
    │   └── native-lib.cpp      # JNI Bindings (Kotlin <-> C++)
    └── app/src/main/java/...   # Kotlin UI (Jetpack Compose, MVVM)
```

## 🚀 How Data Flows (Pipeline)
* **UI Layer:** User selects priority (e.g., CRITICAL) and payload size in the Android App.

* **JNI Boundary:** Kotlin passes the raw string to C++ via native-lib.cpp.

* **Security Layer:** C++ pads the data (PKCS7) and encrypts it using AES-128 CBC.

* **Transport Layer:** The engine wraps the ciphertext in an RUDP packet (adding DATA tags and Sequence Numbers).

* **Network:** The raw bytes are blasted through a UDP socket.

* **Receiver (Server):** Validates the sequence, sends an ACK, peels off the AES encryption, and parses the clear-text business logic.

## ⚙️ Build & Run Instructions
### Option A: Running the Standalone C++ CLI Engine
Requires a C++17 compatible compiler (MSVC 2019+, GCC 9+, Clang 10+) and CMake 3.15+.

```bash
# 1. Build the engine
mkdir build
cd build
cmake ..
cmake --build .

# 2. Run in Simulation mode
./TrafficSimulator --simulate       # Linux/macOS
.\Debug\TrafficSimulator.exe --simulate  # Windows

# 3. Run in Server mode (listens on port 8080)
./TrafficSimulator --server
```

### Option B: Running the Android App
1. Clone the repository and open the android-app folder in Android Studio (Giraffe or newer).

2. Ensure Android NDK & CMake are installed via the SDK Manager.

3. Click Sync Project with Gradle Files to establish the C++ bindings.

4. Build and run the application on an emulator or physical device.

## 🧪 Testing
### Unit Tests (C++)
Unit tests are organized in the tests/ directory using Google Test. To execute them:
```bash
cd build
./TrafficTests             # Linux/macOS
.\Debug\TrafficTests.exe   # Windows
```

## External Network Testing (Python)
You can test the RUDP connection and encryption using the cryptography package in Python.

```bash
pip install cryptography
```

Example encrypted packet injection:

```python
import socket
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.backends import default_backend

# Encryption setup (Matching crypto.hpp keys)
key, iv = b"TajnyKlucz123456", b"WektorInicjuj123"
padder = padding.PKCS7(128).padder()
padded_data = padder.update(b"99,CRITICAL,1024,Payload") + padder.finalize()

cipher = Cipher(algorithms.AES(key), modes.CBC(iv), backend=default_backend())
encryptor = cipher.encryptor()
encrypted_payload = encryptor.update(padded_data) + encryptor.finalize()

# Wrap in RUDP and send
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.sendto(b"DATA,1," + encrypted_payload, ("127.0.0.1", 8080))
```

## 📝 License
This project is created for portfolio and educational purposes. The tiny-AES-c library is utilized under the Public Domain license.

## Autor
Bartłomiej Zięcina