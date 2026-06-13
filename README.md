# 🌐 Network Traffic Manager & Simulator

Symulator oraz procesor ruchu sieciowego w warstwie 3 (L3), napisany w nowoczesnym standardzie **C++17**. Projekt został zaprojektowany z myślą o wysokiej wydajności, bezpieczeństwie pamięci oraz łatwości testowania.

---

> 🚧 **AKTUALNY STATUS: Aktywna migracja na platformę Android (JNI/NDK)**
> Projekt przechodzi obecnie transformację z aplikacji konsolowej w natywne narzędzie mobilne do monitorowania sieci. Istniejący silnik C++ (Core) jest integrowany z warstwą UI w Kotlinie za pomocą *Java Native Interface (JNI)*. 

### 🗺️ Roadmapa rozwoju (Mobile Integration)
- [x] Silnik C++ L3 (Filtrowanie MTU, kolejkowanie priorytetowe L3).
- [x] Abstrakcja sieciowa UDP oraz kompleksowe testy jednostkowe (GTest).
- [x] Implementacja mostu JNI (`native-lib.cpp`) do przesyłania danych pakietów do Kotlina.
- [ ] **W trakcie:** Aplikacja Android (Kotlin) z wykresem ruchu w czasie rzeczywistym i powiadomieniami dla pakietów `CRITICAL`.
- [ ] **W planach:** Rozszerzenie o protokół Reliable UDP (RUDP) oraz natywne szyfrowanie AES.

---

## 🚀 Kluczowe Funkcjonalności

* **Dwa Tryby Pracy (Multi-mode CLI):**
    * `--simulate`: Wewnętrzny generator generujący losowy ruch w celu testowania wydajności algorytmów.
    * `--server`: Realny serwer UDP nasłuchujący na porcie sieciowym, parsujący pakiety w czasie rzeczywistym.
* **Networking (UDP Sockets):** Cross-platformowa implementacja gniazd sieciowych (Winsock2 dla Windows / POSIX dla Linux).
* **Automatyczne Zarządzanie Ruchem (Policing & Scheduling):**
    * Filtrowanie pakietów na podstawie rozmiaru (MTU filtering).
    * Sortowanie priorytetowe (Priority-based scheduling) przy użyciu wydajnych algorytmów STL.
* **Software Engineering Best Practices:**
    * **RAII (Resource Acquisition Is Initialization):** Automatyczne zarządzanie cyklem życia gniazd, plików i pamięci.
    * **Smart Pointers:** Wykorzystanie `std::unique_ptr` do eliminacji wycieków pamięci.
    * **Custom Exception Handling:** Własne mechanizmy obsługi błędów dla integralności danych.
    * **Logging System:** Wielozadaniowy logger (konsola + plik) ze znacznikami czasu.

## 🛠 Technologie i Narzędzia

* **Silnik Natywny:** C++17
* **Aplikacja Mobilna (Wdrażane):** Kotlin, Android NDK, JNI
* **System Budowania:** CMake (3.15+)
* **Testowanie:** Google Test (GTest) – automatycznie pobierany przez FetchContent
* **Sieć:** UDP Sockets (Winsock2 / POSIX)
* **Środowisko:** Wieloplatformowe (Windows/Linux/Android)

## 🏗 Struktura Projektu

```text
├── main.cpp                # Punkt wejścia i logika CLI
├── packet.hpp              # Model danych i logika wyjątków
├── trafficManager.hpp      # Serce systemu - zarządzanie kolejką i statystyki
├── trafficGenerator.hpp    # Generator ruchu do celów testowych
├── udpReceiver.hpp         # Abstrakcja warstwy sieciowej
├── packetParser.hpp        # Parser danych surowych na obiekty C++
├── logger.hpp              # System logowania
├── tests/                  # Testy jednostkowe (Google Test)
└── CMakeLists.txt          # Konfiguracja budowania i zależności
```

## ⚙️ Budowanie i Uruchamianie

#### Wymagania
- Kompilator wspierający C++17 (np. MSVC 2019+, GCC 9+, Clang 10+)
- CMake 3.15 lub nowszy

#### Budowanie
Bezpośrednio z katalogu projektu:
```bash
mkdir build
cd build
cmake ..
cmake --build .
```

#### Uruchamianie
* Tryb symulacji:
```bash
# Windows
.\Debug\TrafficSimulator.exe --simulate

# Linux / macOS
./TrafficSimulator --simulate
```

* Tryb serwera (nasłuchuje na porcie 8080):
```bash
# Windows
.\Debug\TrafficSimulator.exe --server

# Linux / macOS
./TrafficSimulator --server
```

Przykładowy skrypt w Python do wysyłania pakietów UDP:
```python
import socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.sendto(b"101,LOW,500,Background", ("127.0.0.1", 8080))
sock.sendto(b"102,CRITICAL,120,Heartbeat", ("127.0.0.1", 8080))
sock.sendto(b"103,HIGH,9000,Jumbo_Video", ("127.0.0.1", 8080))
sock.sendto(b"104,MEDIUM,400,WebData", ("127.0.0.1", 8080))
sock.sendto(b"105,CRITICAL,64,SysControl", ("127.0.0.1", 8080))
```

## 🧪 Testowanie
Testy jednostkowe są zorganizowane w katalogu `tests/` i wykorzystują
Google Test. Aby uruchomić testy, w katalogu `build/` wpisz:
```bash
# Windows
.\Debug\TrafficTests.exe

# Linux / macOS
./TrafficTests
```

## Autor
Bartłomiej Zięcina
