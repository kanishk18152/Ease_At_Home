# Ease_At_Home

## Kharcha Pani (Android)

A new runnable **Android starter scaffold** has been added under `kharcha-pani/` for the Kharcha Pani expense tracker.

### What's implemented now

- Gradle-based Android project scaffold (`settings.gradle.kts`, root/app `build.gradle.kts`).
- Jetpack Compose app entry (`MainActivity`) with a Home screen.
- Room database setup (`KharchaDatabase`, `KharchaDao`, entities).
- Manual expense entry flow with default category seeding.
- Transaction listing + “today spend” summary card.
- SMS parser core (`SmsTransactionParser`) and initial receiver (`BankSmsReceiver`) for `SMS_RECEIVED`.
- Parser unit tests for provided Federal/SBI sample patterns.

### Notes

This is a Phase-1 baseline and not yet feature-complete against the full product brief.
