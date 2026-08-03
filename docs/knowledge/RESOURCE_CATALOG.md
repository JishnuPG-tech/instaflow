# RESOURCE CATALOG — Values, Strings, Icons & Assets

## Directory: `app/src/main/res/values-*/strings.xml`
- **Location**: `app/src/main/res/values/strings.xml`
- **Purpose**: App UI text resources and string templates.
- **Called by**: Compose UI `stringResource(R.string...)`
- **Depends on**: Android Resource Manager
- **Thread**: Main / Layout Inflation
- **Decision**: KEEP & MODIFY
- **Reason**: Foundation for multi-language localization.
- **Future modifications**: Update string copy to replace generic downloader terminology with Instagram media terms.
