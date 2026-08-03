# Gradle Build System & Product Flavors

## 1. Gradle Build Specs ([`app/build.gradle.kts`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/build.gradle.kts))

- **Android Gradle Plugin (AGP)**: `8.7.2`
- **Kotlin Plugin**: `2.0.20`
- **KSP Plugin**: `2.0.20-1.0.25`
- **Compose Compiler Plugin**: `2.0.20`

---

## 2. Product Flavor Dimensions (`publishChannel`)

```kotlin
flavorDimensions += "publishChannel"

productFlavors {
    create("generic") {
        dimension = "publishChannel"
        isDefault = true
    }
    create("githubPreview") {
        dimension = "publishChannel"
        applicationIdSuffix = ".preview"
        resValue("string", "app_name", "Seal Preview")
    }
    create("fdroid") {
        dimension = "publishChannel"
        versionName = "$baseVersionName-(F-Droid)"
    }
}
```

---

## 3. ABI Split Strategy

Generates distinct architecture-optimized APKs to reduce binary download sizes:
- `arm64-v8a` (Code: 2)
- `armeabi-v7a` (Code: 1)
- `x86` (Code: 3)
- `x86_64` (Code: 4)
- `universal` (Combined fallback)
