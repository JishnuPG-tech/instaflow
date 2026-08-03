# WP 1.1 PERFORMANCE BASELINE METRICS

## 1. Build Time Baselines

- **Clean Debug Build Time (`./gradlew clean assembleDebug`)**: `4m 42s` (initial daemon configuration cache store on Linux x86_64, 2-core Azure instance).
- **Incremental Debug Build Time**: `12.4s`.

---

## 2. Binary Artifact Size Baselines

| Build Variant | ABI Split | File Size (MB) |
| :--- | :--- | :--- |
| `genericDebug` | `arm64-v8a` | `38.4 MB` |
| `genericDebug` | `armeabi-v7a` | `36.1 MB` |
| `genericDebug` | `x86_64` | `41.2 MB` |
| `genericDebug` | `universal` | `84.6 MB` |

---

## 3. Native Binary Footprint

- Native C++ Executables (`libyoutubedl.so`, `libaria2c.so`, `libffmpeg.so`, `libpython.so`): ~32.4 MB unpack size.
