package com.tradecalc.netpl.data

/**
 * Everything the user enters about a single trade (one buy leg + one sell leg).
 * For Futures/Options, `quantity` should already be Lots x LotSize (computed in the UI layer).
 */
data class TradeInput(
    val segment: Segment,
    val tradeType: TradeType,
    val buyPrice: Double,
    val sellPrice: Double,
    val quantity: Double
)

/**
 * Full line-item breakdown of a trade's charges plus the final net P&L.
 */
data class TradeResult(
    val buyTurnover: Double,
    val sellTurnover: Double,
    val totalTurnover: Double,
    val brokerage: Double,
    val stt: Double,
    val exchangeTxnCharges: Double,
    val sebiCharges: Double,
    val stampDuty: Double,
    val gst: Double,
    val dpCharges: Double,
    val totalCharges: Double,
    val grossPnl: Double,
    val netPnl: Double,
    val breakevenPerUnit: Double
)

object TradeCalculator {

    fun calculate(
        input: TradeInput,
        brokerageRates: BrokerageRates,
        statutoryRates: StatutoryRates
    ): TradeResult {
        val buyTurnover = input.buyPrice * input.quantity
        val sellTurnover = input.sellPrice * input.quantity
        val totalTurnover = buyTurnover + sellTurnover

        // Brokerage - applied on total turnover (both legs), using the master rate
        val brokeragePercent = brokerageRates.percentFor(input.segment, input.tradeType)
        val brokerage = totalTurnover * brokeragePercent / 100.0

        // STT - rule differs: equity delivery = both legs, everything else = sell leg only
        val sttPercent = statutoryRates.sttPercentFor(input.segment, input.tradeType)
        val stt = if (input.segment == Segment.EQUITY && input.tradeType == TradeType.DELIVERY) {
            totalTurnover * sttPercent / 100.0
        } else {
            sellTurnover * sttPercent / 100.0
        }

        // Exchange transaction charges - both legs
        val exchangeTxnPercent = statutoryRates.exchangeTxnPercentFor(input.segment)
        val exchangeTxnCharges = totalTurnover * exchangeTxnPercent / 100.0

        // SEBI turnover fees - both legs
        val sebiPercent = statutoryRates.sebiPercentFor(input.segment)
        val sebiCharges = totalTurnover * sebiPercent / 100.0

        // Stamp duty - buy leg only
        val stampPercent = statutoryRates.stampPercentFor(input.segment, input.tradeType)
        val stampDuty = buyTurnover * stampPercent / 100.0

        // GST - 18% on (brokerage + exchange charges + SEBI charges)
        val gst = (brokerage + exchangeTxnCharges + sebiCharges) * statutoryRates.gstPercent / 100.0

        // DP charges - only for equity delivery (a sell out of demat holdings)
        val dpCharges = if (input.segment == Segment.EQUITY && input.tradeType == TradeType.DELIVERY) {
            statutoryRates.dpChargesFlat
        } else {
            0.0
        }

        val totalCharges = brokerage + stt + exchangeTxnCharges + sebiCharges + stampDuty + gst + dpCharges

        val grossPnl = sellTurnover - buyTurnover
        val netPnl = grossPnl - totalCharges

        val breakevenPerUnit = if (input.quantity > 0) totalCharges / input.quantity else 0.0

        return TradeResult(
            buyTurnover = buyTurnover,
            sellTurnover = sellTurnover,
            totalTurnover = totalTurnover,
            brokerage = brokerage,
            stt = stt,
            exchangeTxnCharges = exchangeTxnCharges,
            sebiCharges = sebiCharges,
            stampDuty = stampDuty,
            gst = gst,
            dpCharges = dpCharges,
            totalCharges = totalCharges,
            grossPnl = grossPnl,
            netPnl = netPnl,
            breakevenPerUnit = breakevenPerUnit
        )
    }
}
