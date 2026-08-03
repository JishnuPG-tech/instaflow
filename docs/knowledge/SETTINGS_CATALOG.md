# SETTINGS CATALOG — Key-Value Storage & MMKV Keys

## Utility: `PreferenceUtil`
- **Location**: [`app/src/main/java/com/junkfood/seal/util/PreferenceUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/PreferenceUtil.kt)
- **Purpose**: Static API layer over Tencent MMKV binary storage.
- **Called by**: ViewModels, Settings Pages, Downloader Engine
- **Depends on**: MMKV 1.3.12
- **Thread**: Synchronous / In-memory mmap
- **Decision**: KEEP & MODIFY
- **Reason**: Near-instant read/writes without main-thread I/O blocking.
- **Future modifications**: Strip out SponsorBlock and YouTube keys; add Instagram media preference defaults.
