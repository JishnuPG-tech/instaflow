# WP 2.1 TEST REPORT

- **Work Package ID**: `WP 2.1`
- **Test Suite**: `com.junkfood.seal.util.InstagramUrlValidatorTest`
- **Execution Command**: `./gradlew :app:testGenericDebugUnitTest --tests com.junkfood.seal.util.InstagramUrlValidatorTest`
- **Status**: 🟢 100% PASS (5/5 unit tests passed)

---

## Unit Test Breakdown

1. `testReelUrlParsing` — PASSED (`instagram.com/reel/C1xAbCdEfGh/` → `REEL`)
2. `testPostUrlParsing` — PASSED (`instagram.com/p/Cz123456789/` → `POST`)
3. `testStoryUrlParsing` — PASSED (`instagram.com/stories/test_user/3216549870123/` → `STORY`)
4. `testProfileUrlParsing` — PASSED (`instagram.com/cristiano/` → `PROFILE`)
5. `testInvalidUrlParsing` — PASSED (`youtube.com/...` → `UNKNOWN`)
