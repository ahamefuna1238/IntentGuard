# Changelog

All notable changes to the **IntentGuard** project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-01-18
## 🛡️ Security Fixes
* Token Consumption Logic: Fixed a potential replay vulnerability in DefaultAccessManager. Tokens are now explicitly invalidated via tokenManager.consumeToken() immediately after successful validation.
* Validation Integrity: Improved the handshake between AccessManager and IntentProcessor to ensure onValidAccessToken is only triggered when the session key echo is cryptographically verified.

## 🐛 Bug Fixes
* Listener Synchronization: Resolved an issue where ResultListener callbacks were lost. IntentGuardManager now correctly bridges the listener to both the processing engine and the security manager.
* Dialog Lifecycle: Fixed a bug in OnClickHelper where the host Activity/Fragment could occasionally fail to deliver RESULT_CANCELED when the dialog was dismissed via the cancel button.
* Fragment Context Safety: Added defensive null-checks in OnClickHelper to safely retrieve the parent activity during the APPROVE_BUTTON click flow in Fragment-hosted scenarios.

## 🚀 Performance & Internals
* Logging: Added strategic debug logging across IntentProcessor and DefaultAccessManager to provide developers with visibility into the secure handshake (token presence and validation status).
* Improved Metadata Handling: Standardized the extraction of REQUEST_TOKEN from incoming intents to prevent null pointer exceptions during the response phase.

## 💡 Implementation Tip for v1.0.1
When upgrading, ensure your MainActivity continues to register the activity result correctly:

```java

import android.app.Activity;

public class YourActivity extends Activity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // This call is essential for v1.0.1 token validation
        intentGuardManager.registerActivityResult(requestCode, resultCode, data);
    }
}

```

## [1.0] - 2026-01-18

### Added
- **Core Security:** Initial release of `IntentGuardManager` for secure inter-app communication.
- **Identity Verification:** Support for certificate fingerprint validation via `AccessManager`.
- **Anti-Replay:** Implementation of one-time session tokens in `IntentProcessor`.
- **UI Components:** Material 3 (M3) compliant Bottom Sheet rationale dialogs.
- **UI Components:** Custom adapter for displaying requested permissions in a list.
- **Customization:** Support for custom XML layouts and `DialogInfo` text overrides.

### Changed
- Refactored `intent-guard` module for JitPack compatibility.

### Fixed
- Fixed nested scrolling conflicts in the permission list within the Bottom Sheet.