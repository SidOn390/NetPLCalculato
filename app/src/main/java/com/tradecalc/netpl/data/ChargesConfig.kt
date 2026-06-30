package com.tradecalc.netpl.data

/**
 * Trading segment the user is calculating charges for.
 */
enum class Segment(val label: String) {
    EQUITY("Equity"),
    FUTURES("Futures"),
    OPTIONS("Options")
}

/**
 * Delivery (held beyond the trading day / carry-forward) vs Intraday (squared off same day).
 */
enum class TradeType(val label: String) {
    DELIVERY("Delivery"),
    INTRADAY("Intraday")
}

/**
 * User-editable brokerage rates (the "Brokerage Master").
 * All values are PERCENTAGES (e.g. 0.20 means 0.20%), applied on turnover (buy + sell value).
 *
 * Defaults match what was requested:
 *  - Delivery in stocks & futures: 0.20%
 *  - Intraday in stocks & futures: 0.020%
 *  - Options delivery (carry-forward): 0.10%
 *  - Options intraday: 0.010%
 */
data class BrokerageRates(
    val equityDeliveryPercent: Double = 0.20,
    val equityIntradayPercent: Double = 0.020,
    val futuresDeliveryPercent: Double = 0.20,
    val futuresIntradayPercent: Double = 0.020,
    val optionsDeliveryPercent: Double = 0.10,
    val optionsIntradayPercent: Double = 0.010
) {
    fun percentFor(segment: Segment, tradeType: TradeType): Double = when (segment) {
        Segment.EQUITY -> if (tradeType == TradeType.DELIVERY) equityDeliveryPercent else equityIntradayPercent
        Segment.FUTURES -> if (tradeType == TradeType.DELIVERY) futuresDeliveryPercent else futuresIntradayPercent
        Segment.OPTIONS -> if (tradeType == TradeType.DELIVERY) optionsDeliveryPercent else optionsIntradayPercent
    }
}

/**
 * Government / exchange statutory charges. These are NOT broker-controlled, but are kept
 * editable in the Charges Master in case rates change in a future Budget or exchange circular.
 *
 * Defaults below reflect rates effective from 1 April 2026 (post Union Budget 2026-27 STT hike
 * on Futures & Options) as published by NSE / brokers' charge pages at the time this app was built.
 * Always double-check against your broker's contract note or nseindia.com if rates change.
 */
data class StatutoryRates(
    // --- Securities Transaction Tax (STT) ---
    // Equity delivery: charged on BOTH buy and sell legs.
    val sttEquityDeliveryPercent: Double = 0.10,
    // Equity intraday: charged on SELL leg only.
    val sttEquityIntradayPercent: Double = 0.025,
    // Futures: charged on SELL leg only (hiked from 0.02% to 0.05% effective 1 Apr 2026).
    val sttFuturesPercent: Double = 0.05,
    // Options: charged on SELL side premium only (hiked from 0.10% to 0.15% effective 1 Apr 2026).
    val sttOptionsPercent: Double = 0.15,

    // --- Exchange transaction charges (NSE), charged on BOTH legs ---
    val exchangeTxnEquityPercent: Double = 0.00297,
    val exchangeTxnFuturesPercent: Double = 0.00173,
    // Options: charged on premium turnover.
    val exchangeTxnOptionsPercent: Double = 0.03503,

    // --- SEBI turnover fees (incl. IPFT), charged on BOTH legs ---
    val sebiEquityFuturesPercent: Double = 0.0001,
    // Options: on premium turnover.
    val sebiOptionsPercent: Double = 0.0005,

    // --- Stamp duty, charged on BUY leg only (Gujarat & most states follow these uniform rates) ---
    val stampEquityDeliveryPercent: Double = 0.015,
    val stampEquityIntradayPercent: Double = 0.003,
    val stampFuturesPercent: Double = 0.002,
    val stampOptionsPercent: Double = 0.003,

    // --- GST, applied on (Brokerage + Exchange txn charges + SEBI charges) ---
    val gstPercent: Double = 18.0,

    // --- Depository (DP) charges: flat fee per scrip, charged ONLY when equity delivery
    // shares are sold out of the demat account (not applicable to intraday or F&O). ---
    val dpChargesFlat: Double = 15.34
) {
    fun sttPercentFor(segment: Segment, tradeType: TradeType): Double = when (segment) {
        Segment.EQUITY -> if (tradeType == TradeType.DELIVERY) sttEquityDeliveryPercent else sttEquityIntradayPercent
        Segment.FUTURES -> sttFuturesPercent
        Segment.OPTIONS -> sttOptionsPercent
    }

    fun exchangeTxnPercentFor(segment: Segment): Double = when (segment) {
        Segment.EQUITY -> exchangeTxnEquityPercent
        Segment.FUTURES -> exchangeTxnFuturesPercent
        Segment.OPTIONS -> exchangeTxnOptionsPercent
    }

    fun sebiPercentFor(segment: Segment): Double = when (segment) {
        Segment.EQUITY, Segment.FUTURES -> sebiEquityFuturesPercent
        Segment.OPTIONS -> sebiOptionsPercent
    }

    fun stampPercentFor(segment: Segment, tradeType: TradeType): Double = when (segment) {
        Segment.EQUITY -> if (tradeType == TradeType.DELIVERY) stampEquityDeliveryPercent else stampEquityIntradayPercent
        Segment.FUTURES -> stampFuturesPercent
        Segment.OPTIONS -> stampOptionsPercent
    }
}
