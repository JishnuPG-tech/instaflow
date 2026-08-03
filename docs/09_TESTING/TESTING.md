# Testing Strategy & Linting Framework

## 1. Test Architecture & Structure

```
app/
├── src/
│   ├── test/               # JUnit4 JVM Unit Tests
│   └── androidTest/        # Instrumentation & Espresso UI Tests
```

---

## 2. Testing Frameworks

- **JUnit4 (`junit:junit:4.13.2`)**: Primary test runner for unit tests.
- **AndroidX Test Ext (`androidx.test.ext:junit-ktx:1.1.4`)**: Kotlin extensions for Android unit/integration tests.
- **Espresso (`androidx.test.espresso:espresso-core:3.5.0`)**: UI action automation and assertion engine.
- **Compose UI Test (`ui-test-junit4`)**: Compose node testing and composable tree assertions.

---

## 3. Formatting & Static Analysis (`ktfmt`)

Seal uses `com.ncorti.ktfmt.gradle` (`version 0.20.1`) configured in [`app/build.gradle.kts`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/build.gradle.kts):

```bash
# Verify formatting compliance
./gradlew ktlintCheck

# Apply automatic formatting fixes
./gradlew ktfmtFormat
```
