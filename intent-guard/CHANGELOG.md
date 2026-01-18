# Changelog

All notable changes to the **IntentGuard** project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-18

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