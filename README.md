# Net P&L Calculator — Indian Stock Market Trade Charges App

A native Android app (Kotlin + Jetpack Compose) that calculates your **net profit/loss after all
charges** for trades in:
- Equity Delivery & Equity Intraday
- Futures (Carry Forward & Intraday)
- Options (Carry Forward & Intraday)

## Option A: Build an APK online (no Android Studio install needed)

This project includes a ready-made GitHub Actions workflow
(`.github/workflows/build-apk.yml`) that builds a debug APK in the cloud for free.

1. Create a free account at [github.com](https://github.com) if you don't have one.
2. Create a new **public** repository (e.g. `net-pl-calculator`) — public repos get unlimited
   free GitHub Actions minutes.
3. Upload this entire `NetPLCalculator` folder into that repo. Easiest way: on the repo page,
   click **Add file > Upload files**, then drag the whole folder in (or use `git push` if you're
   comfortable with git — see below).
4. Go to the **Actions** tab in your repo. A workflow called "Build Debug APK" will already be
   running (it auto-starts on every push). Wait ~3–5 minutes for it to finish (green checkmark).
5. Click into that workflow run, scroll to **Artifacts**, and download
   `NetPLCalculator-debug-apk` — it's a zip containing `app-debug.apk`.
6. Transfer the APK to your Android phone (email it to yourself, Google Drive, WhatsApp, USB —
   whatever's easiest), tap it to install. Android will ask you to allow "install from this
   source" the first time — approve it, then install.

That's it — no Android Studio, no SDK download, nothing installed on your computer. Optional git
push version of step 3, if you prefer the command line:
```
cd NetPLCalculator
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<your-username>/net-pl-calculator.git
git push -u origin main
```

Note: this produces a **debug APK** — fine for installing on your own phone for testing, but
not signed for the Play Store. If you later want a signed release build, that's a small extra
step Android Studio (or the same Actions workflow with a signing config) can handle.

## Option B: Build and run locally in Android Studio

1. Install **Android Studio** (Hedgehog or newer).
2. Open this folder (`NetPLCalculator`) as an existing project — `File > Open`.
3. Let Gradle sync (Android Studio will auto-download the Gradle wrapper jar and dependencies
   the first time — needs an internet connection once).
4. Click **Run ▶** on an emulator or a connected Android phone (minimum Android 7.0 / API 24).

No API keys, signing, or extra setup needed — it's fully offline and self-contained.

## What it does

**Calculator tab** — pick a segment (Equity / Futures / Options) and trade type
(Delivery–Carry Forward / Intraday), enter buy price, sell price, and quantity
(for F&O: lot size × number of lots), and it instantly shows:
- Buy/Sell turnover and Gross P&L
- Brokerage, STT/CTT, Exchange Transaction Charges, SEBI Charges, Stamp Duty, GST, DP Charges
- **Total Charges** and **Net P&L** (green if profit, red if loss)
- Breakeven price move required per share/unit

**Charges Master tab** — a single place to edit:
- **Brokerage rates** per segment/trade-type (this is what you'll use most)
- **Statutory rates** (STT, exchange charges, SEBI fees, stamp duty, GST, DP charges) — these are
  set by the government/exchanges, not your broker, but are editable here in case a future Budget
  or exchange circular changes them.
- A **Reset All To Defaults** button.

All settings persist across app restarts using Jetpack DataStore.

## Default brokerage rates (editable in Charges Master)

| Segment | Delivery / Carry Forward | Intraday |
|---|---|---|
| Equity (Stocks) | 0.20% | 0.020% |
| Futures | 0.20% | 0.020% |
| Options | 0.10% | 0.010% |

## Default statutory charges (effective 1 April 2026, post Union Budget 2026-27)

These reflect the STT hike on Futures & Options announced in Budget 2026 (futures STT raised from
0.02% → 0.05%, options STT raised from 0.10% → 0.15%), plus standard NSE exchange transaction
charges, SEBI turnover fees, stamp duty and GST that apply uniformly across all brokers.

| Charge | Equity Delivery | Equity Intraday | Futures | Options |
|---|---|---|---|---|
| STT | 0.10% (both legs) | 0.025% (sell leg) | 0.05% (sell leg) | 0.15% (sell premium) |
| Exchange Txn Charge (NSE) | 0.00297% (both legs) | same | 0.00173% (both legs) | 0.03503% (premium, both legs) |
| SEBI + IPFT | 0.0001% (both legs) | same | 0.0001% (both legs) | 0.0005% (premium, both legs) |
| Stamp Duty | 0.015% (buy leg) | 0.003% (buy leg) | 0.002% (buy leg) | 0.003% (buy leg) |
| GST | 18% on (Brokerage + Exchange Txn + SEBI charges) | | | |
| DP Charges | ₹15.34 flat, only on equity delivery **sell** | — | — | — |

**Note:** Statutory charges can change with Union Budgets and exchange circulars. The Charges
Master screen lets you update these instantly without needing an app update — just verify against
your broker's current contract note or nseindia.com / your broker's charges page if you suspect a
change.

## Calculation logic (see `data/TradeCalculator.kt`)

```
buyTurnover  = buyPrice  × quantity
sellTurnover = sellPrice × quantity
totalTurnover = buyTurnover + sellTurnover

brokerage = totalTurnover × brokerage% (from Charges Master, by segment + trade type)

STT:
  Equity Delivery → totalTurnover × STT%   (charged on both legs)
  Everything else → sellTurnover × STT%    (charged on sell leg only)

exchangeTxnCharges = totalTurnover × exchangeTxn% (by segment)
sebiCharges        = totalTurnover × sebi%        (by segment)
stampDuty          = buyTurnover   × stampDuty%   (buy leg only, by segment + trade type)

gst = (brokerage + exchangeTxnCharges + sebiCharges) × 18%

dpCharges = ₹15.34 flat, only if Equity Delivery (selling shares out of demat)

totalCharges = brokerage + STT + exchangeTxnCharges + sebiCharges + stampDuty + gst + dpCharges

grossPnl = sellTurnover − buyTurnover
netPnl   = grossPnl − totalCharges

breakevenPerUnit = totalCharges ÷ quantity
```

## Project structure

```
app/src/main/java/com/tradecalc/netpl/
├── MainActivity.kt              # All Compose UI: Calculator + Charges Master screens
├── data/
│   ├── ChargesConfig.kt         # Segment, TradeType enums + BrokerageRates/StatutoryRates models
│   ├── TradeCalculator.kt       # TradeInput/TradeResult models + the calculation engine
│   └── SettingsRepository.kt    # DataStore persistence for the Charges Master
└── ui/theme/
    ├── Color.kt, Type.kt, Theme.kt
```

## Notes / possible next steps

- Options "exercise" settlement (STT on intrinsic value instead of premium) isn't modeled — every
  trade is treated as a normal buy+sell. This covers ~95% of real-world option trades; exercised/
  assigned ITM options at expiry would need a small follow-up feature.
- Only NSE exchange transaction rates are used; BSE rates differ slightly for equity. You can tweak
  these per-segment percentages directly in the Charges Master if you trade on BSE.
- Currently doesn't connect to a live data feed for charges — rates are accurate as of when this
  app was built (June 2026) and are meant to be manually updated in the Charges Master if/when
  exchanges or the government revise them.
