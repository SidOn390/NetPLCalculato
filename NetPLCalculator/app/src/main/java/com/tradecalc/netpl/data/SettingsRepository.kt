package com.tradecalc.netpl.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "charges_master")

/**
 * Persists the editable "Charges Master" (brokerage + statutory rates) across app restarts.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val EQ_DELIVERY = doublePreferencesKey("eq_delivery_brokerage")
        val EQ_INTRADAY = doublePreferencesKey("eq_intraday_brokerage")
        val FUT_DELIVERY = doublePreferencesKey("fut_delivery_brokerage")
        val FUT_INTRADAY = doublePreferencesKey("fut_intraday_brokerage")
        val OPT_DELIVERY = doublePreferencesKey("opt_delivery_brokerage")
        val OPT_INTRADAY = doublePreferencesKey("opt_intraday_brokerage")

        val STT_EQ_DELIVERY = doublePreferencesKey("stt_eq_delivery")
        val STT_EQ_INTRADAY = doublePreferencesKey("stt_eq_intraday")
        val STT_FUTURES = doublePreferencesKey("stt_futures")
        val STT_OPTIONS = doublePreferencesKey("stt_options")

        val EXCH_EQUITY = doublePreferencesKey("exch_equity")
        val EXCH_FUTURES = doublePreferencesKey("exch_futures")
        val EXCH_OPTIONS = doublePreferencesKey("exch_options")

        val SEBI_EQ_FUT = doublePreferencesKey("sebi_eq_fut")
        val SEBI_OPTIONS = doublePreferencesKey("sebi_options")

        val STAMP_EQ_DELIVERY = doublePreferencesKey("stamp_eq_delivery")
        val STAMP_EQ_INTRADAY = doublePreferencesKey("stamp_eq_intraday")
        val STAMP_FUTURES = doublePreferencesKey("stamp_futures")
        val STAMP_OPTIONS = doublePreferencesKey("stamp_options")

        val GST = doublePreferencesKey("gst")
        val DP_CHARGES = doublePreferencesKey("dp_charges")
    }

    val brokerageRates: Flow<BrokerageRates> = context.dataStore.data.map { prefs ->
        val defaults = BrokerageRates()
        BrokerageRates(
            equityDeliveryPercent = prefs[Keys.EQ_DELIVERY] ?: defaults.equityDeliveryPercent,
            equityIntradayPercent = prefs[Keys.EQ_INTRADAY] ?: defaults.equityIntradayPercent,
            futuresDeliveryPercent = prefs[Keys.FUT_DELIVERY] ?: defaults.futuresDeliveryPercent,
            futuresIntradayPercent = prefs[Keys.FUT_INTRADAY] ?: defaults.futuresIntradayPercent,
            optionsDeliveryPercent = prefs[Keys.OPT_DELIVERY] ?: defaults.optionsDeliveryPercent,
            optionsIntradayPercent = prefs[Keys.OPT_INTRADAY] ?: defaults.optionsIntradayPercent
        )
    }

    val statutoryRates: Flow<StatutoryRates> = context.dataStore.data.map { prefs ->
        val defaults = StatutoryRates()
        StatutoryRates(
            sttEquityDeliveryPercent = prefs[Keys.STT_EQ_DELIVERY] ?: defaults.sttEquityDeliveryPercent,
            sttEquityIntradayPercent = prefs[Keys.STT_EQ_INTRADAY] ?: defaults.sttEquityIntradayPercent,
            sttFuturesPercent = prefs[Keys.STT_FUTURES] ?: defaults.sttFuturesPercent,
            sttOptionsPercent = prefs[Keys.STT_OPTIONS] ?: defaults.sttOptionsPercent,
            exchangeTxnEquityPercent = prefs[Keys.EXCH_EQUITY] ?: defaults.exchangeTxnEquityPercent,
            exchangeTxnFuturesPercent = prefs[Keys.EXCH_FUTURES] ?: defaults.exchangeTxnFuturesPercent,
            exchangeTxnOptionsPercent = prefs[Keys.EXCH_OPTIONS] ?: defaults.exchangeTxnOptionsPercent,
            sebiEquityFuturesPercent = prefs[Keys.SEBI_EQ_FUT] ?: defaults.sebiEquityFuturesPercent,
            sebiOptionsPercent = prefs[Keys.SEBI_OPTIONS] ?: defaults.sebiOptionsPercent,
            stampEquityDeliveryPercent = prefs[Keys.STAMP_EQ_DELIVERY] ?: defaults.stampEquityDeliveryPercent,
            stampEquityIntradayPercent = prefs[Keys.STAMP_EQ_INTRADAY] ?: defaults.stampEquityIntradayPercent,
            stampFuturesPercent = prefs[Keys.STAMP_FUTURES] ?: defaults.stampFuturesPercent,
            stampOptionsPercent = prefs[Keys.STAMP_OPTIONS] ?: defaults.stampOptionsPercent,
            gstPercent = prefs[Keys.GST] ?: defaults.gstPercent,
            dpChargesFlat = prefs[Keys.DP_CHARGES] ?: defaults.dpChargesFlat
        )
    }

    suspend fun saveBrokerageRates(rates: BrokerageRates) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EQ_DELIVERY] = rates.equityDeliveryPercent
            prefs[Keys.EQ_INTRADAY] = rates.equityIntradayPercent
            prefs[Keys.FUT_DELIVERY] = rates.futuresDeliveryPercent
            prefs[Keys.FUT_INTRADAY] = rates.futuresIntradayPercent
            prefs[Keys.OPT_DELIVERY] = rates.optionsDeliveryPercent
            prefs[Keys.OPT_INTRADAY] = rates.optionsIntradayPercent
        }
    }

    suspend fun saveStatutoryRates(rates: StatutoryRates) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STT_EQ_DELIVERY] = rates.sttEquityDeliveryPercent
            prefs[Keys.STT_EQ_INTRADAY] = rates.sttEquityIntradayPercent
            prefs[Keys.STT_FUTURES] = rates.sttFuturesPercent
            prefs[Keys.STT_OPTIONS] = rates.sttOptionsPercent
            prefs[Keys.EXCH_EQUITY] = rates.exchangeTxnEquityPercent
            prefs[Keys.EXCH_FUTURES] = rates.exchangeTxnFuturesPercent
            prefs[Keys.EXCH_OPTIONS] = rates.exchangeTxnOptionsPercent
            prefs[Keys.SEBI_EQ_FUT] = rates.sebiEquityFuturesPercent
            prefs[Keys.SEBI_OPTIONS] = rates.sebiOptionsPercent
            prefs[Keys.STAMP_EQ_DELIVERY] = rates.stampEquityDeliveryPercent
            prefs[Keys.STAMP_EQ_INTRADAY] = rates.stampEquityIntradayPercent
            prefs[Keys.STAMP_FUTURES] = rates.stampFuturesPercent
            prefs[Keys.STAMP_OPTIONS] = rates.stampOptionsPercent
            prefs[Keys.GST] = rates.gstPercent
            prefs[Keys.DP_CHARGES] = rates.dpChargesFlat
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}
