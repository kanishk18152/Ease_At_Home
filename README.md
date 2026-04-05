# Ease_At_Home

This repository now contains a new product blueprint and starter parsing engine for **Kharcha Pani**, an offline-first Android expense tracker.

## Added folder

- `kharcha-pani/`
  - `docs/KHARCHA_PANI_ARCHITECTURE.md` — full architecture and implementation plan.
  - `app/src/main/java/com/kharchapani/core/model/ParsedTransaction.kt` — parser output model.
  - `app/src/main/java/com/kharchapani/core/sms/SmsTransactionParser.kt` — SMS parser with support for Federal Bank and SBI samples.
  - `app/src/test/java/com/kharchapani/core/sms/SmsTransactionParserTest.kt` — unit tests for provided SMS samples and OTP filtering.
