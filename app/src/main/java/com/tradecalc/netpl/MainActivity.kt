package com.tradecalc.netpl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tradecalc.netpl.data.BrokerageRates
import com.tradecalc.netpl.data.Segment
import com.tradecalc.netpl.data.SettingsRepository
import com.tradecalc.netpl.data.StatutoryRates
import com.tradecalc.netpl.data.TradeCalculator
import com.tradecalc.netpl.data.TradeInput
import com.tradecalc.netpl.data.TradeResult
import com.tradecalc.netpl.data.TradeType
import com.tradecalc.netpl.ui.theme.GreenProfit
import com.tradecalc.netpl.ui.theme.NetPLCalculatorTheme
import com.tradecalc.netpl.ui.theme.RedLoss
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = SettingsRepository(applicationContext)
        setContent {
            NetPLCalculatorTheme {
                AppRoot(repository)
            }
        }
    }
}

private fun formatInr(value: Double): String {
    val sign = if (value < 0) "-" else ""
    return "${sign}\u20B9${String.format(Locale.US, "%,.2f", kotlin.math.abs(value))}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(repository: SettingsRepository) {
    var selectedTab by remember { mutableStateOf(0) }
    val brokerageRates by repository.brokerageRates.collectAsState(initial = BrokerageRates())
    val statutoryRates by repository.statutoryRates.collectAsState(initial = StatutoryRates())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedTab == 0) "Net P&L Calculator" else "Charges Master",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Calculate, contentDescription = "Calculator") },
                    label = { Text("Calculator") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Charges Master") },
                    label = { Text("Charges Master") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> CalculatorScreen(
                brokerageRates = brokerageRates,
                statutoryRates = statutoryRates,
                contentPadding = padding
            )
            1 -> ChargesMasterScreen(
                repository = repository,
                brokerageRates = brokerageRates,
                statutoryRates = statutoryRates,
                contentPadding = padding
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CALCULATOR SCREEN
// ---------------------------------------------------------------------------

@Composable
fun CalculatorScreen(
    brokerageRates: BrokerageRates,
    statutoryRates: StatutoryRates,
    contentPadding: PaddingValues
) {
    var segment by remember { mutableStateOf(Segment.EQUITY) }
    var tradeType by remember { mutableStateOf(TradeType.DELIVERY) }

    var buyPriceText by remember { mutableStateOf("") }
    var sellPriceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var lotSizeText by remember { mutableStateOf("") }
    var lotsText by remember { mutableStateOf("") }

    val buyPrice = buyPriceText.toDoubleOrNull() ?: 0.0
    val sellPrice = sellPriceText.toDoubleOrNull() ?: 0.0
    val quantity = if (segment == Segment.EQUITY) {
        quantityText.toDoubleOrNull() ?: 0.0
    } else {
        (lotSizeText.toDoubleOrNull() ?: 0.0) * (lotsText.toDoubleOrNull() ?: 0.0)
    }

    val result: TradeResult? = remember(buyPrice, sellPrice, quantity, segment, tradeType, brokerageRates, statutoryRates) {
        if (buyPrice > 0 && sellPrice > 0 && quantity > 0) {
            TradeCalculator.calculate(
                TradeInput(segment, tradeType, buyPrice, sellPrice, quantity),
                brokerageRates,
                statutoryRates
            )
        } else null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Segment", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Segment.values().forEach { seg ->
                    FilterChip(
                        selected = segment == seg,
                        onClick = { segment = seg },
                        label = { Text(seg.label) }
                    )
                }
            }
        }

        item {
            Text("Trade Type", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TradeType.values().forEach { type ->
                    val label = if (segment != Segment.EQUITY && type == TradeType.DELIVERY) {
                        "Carry Forward"
                    } else {
                        type.label
                    }
                    FilterChip(
                        selected = tradeType == type,
                        onClick = { tradeType = type },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = buyPriceText,
                onValueChange = { buyPriceText = it },
                label = { Text("Buy Price (\u20B9 per share/unit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = sellPriceText,
                onValueChange = { sellPriceText = it },
                label = { Text("Sell Price (\u20B9 per share/unit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (segment == Segment.EQUITY) {
            item {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity (no. of shares)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = lotSizeText,
                        onValueChange = { lotSizeText = it },
                        label = { Text("Lot Size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lotsText,
                        onValueChange = { lotsText = it },
                        label = { Text("No. of Lots") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (quantity > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Total quantity: ${quantity.toInt()} units",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        item {
            if (result == null) {
                Text(
                    "Enter buy price, sell price and quantity to see your net P&L breakdown.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                ResultCard(segment, tradeType, result)
            }
        }
    }
}

@Composable
fun ResultCard(segment: Segment, tradeType: TradeType, result: TradeResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${segment.label} \u2022 ${tradeType.label}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            ChargeRow("Buy Turnover", formatInr(result.buyTurnover))
            ChargeRow("Sell Turnover", formatInr(result.sellTurnover))
            ChargeRow("Gross P&L", formatInr(result.grossPnl))

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Text("Charges Breakdown", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ChargeRow("Brokerage", formatInr(result.brokerage))
            ChargeRow("STT / CTT", formatInr(result.stt))
            ChargeRow("Exchange Txn Charges", formatInr(result.exchangeTxnCharges))
            ChargeRow("SEBI Charges", formatInr(result.sebiCharges))
            ChargeRow("Stamp Duty", formatInr(result.stampDuty))
            ChargeRow("GST (18%)", formatInr(result.gst))
            if (result.dpCharges > 0) {
                ChargeRow("DP Charges", formatInr(result.dpCharges))
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            ChargeRow(
                "Total Charges",
                formatInr(result.totalCharges),
                valueWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            val netColor = if (result.netPnl >= 0) GreenProfit else RedLoss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Net P&L",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatInr(result.netPnl),
                    style = MaterialTheme.typography.headlineSmall,
                    color = netColor
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Breakeven move required: \u20B9${String.format(Locale.US, "%.2f", result.breakevenPerUnit)} per share/unit",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun ChargeRow(label: String, value: String, valueWeight: FontWeight = FontWeight.Normal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = valueWeight)
    }
}

// ---------------------------------------------------------------------------
// CHARGES MASTER SCREEN
// ---------------------------------------------------------------------------

@Composable
fun ChargesMasterScreen(
    repository: SettingsRepository,
    brokerageRates: BrokerageRates,
    statutoryRates: StatutoryRates,
    contentPadding: PaddingValues
) {
    val scope = rememberCoroutineScope()

    var eqDelivery by remember(brokerageRates) { mutableStateOf(brokerageRates.equityDeliveryPercent.toString()) }
    var eqIntraday by remember(brokerageRates) { mutableStateOf(brokerageRates.equityIntradayPercent.toString()) }
    var futDelivery by remember(brokerageRates) { mutableStateOf(brokerageRates.futuresDeliveryPercent.toString()) }
    var futIntraday by remember(brokerageRates) { mutableStateOf(brokerageRates.futuresIntradayPercent.toString()) }
    var optDelivery by remember(brokerageRates) { mutableStateOf(brokerageRates.optionsDeliveryPercent.toString()) }
    var optIntraday by remember(brokerageRates) { mutableStateOf(brokerageRates.optionsIntradayPercent.toString()) }

    var sttEqDelivery by remember(statutoryRates) { mutableStateOf(statutoryRates.sttEquityDeliveryPercent.toString()) }
    var sttEqIntraday by remember(statutoryRates) { mutableStateOf(statutoryRates.sttEquityIntradayPercent.toString()) }
    var sttFutures by remember(statutoryRates) { mutableStateOf(statutoryRates.sttFuturesPercent.toString()) }
    var sttOptions by remember(statutoryRates) { mutableStateOf(statutoryRates.sttOptionsPercent.toString()) }

    var exchEquity by remember(statutoryRates) { mutableStateOf(statutoryRates.exchangeTxnEquityPercent.toString()) }
    var exchFutures by remember(statutoryRates) { mutableStateOf(statutoryRates.exchangeTxnFuturesPercent.toString()) }
    var exchOptions by remember(statutoryRates) { mutableStateOf(statutoryRates.exchangeTxnOptionsPercent.toString()) }

    var sebiEqFut by remember(statutoryRates) { mutableStateOf(statutoryRates.sebiEquityFuturesPercent.toString()) }
    var sebiOptions by remember(statutoryRates) { mutableStateOf(statutoryRates.sebiOptionsPercent.toString()) }

    var stampEqDelivery by remember(statutoryRates) { mutableStateOf(statutoryRates.stampEquityDeliveryPercent.toString()) }
    var stampEqIntraday by remember(statutoryRates) { mutableStateOf(statutoryRates.stampEquityIntradayPercent.toString()) }
    var stampFutures by remember(statutoryRates) { mutableStateOf(statutoryRates.stampFuturesPercent.toString()) }
    var stampOptions by remember(statutoryRates) { mutableStateOf(statutoryRates.stampOptionsPercent.toString()) }

    var gst by remember(statutoryRates) { mutableStateOf(statutoryRates.gstPercent.toString()) }
    var dpCharges by remember(statutoryRates) { mutableStateOf(statutoryRates.dpChargesFlat.toString()) }

    fun saveBrokerage() {
        scope.launch {
            repository.saveBrokerageRates(
                BrokerageRates(
                    equityDeliveryPercent = eqDelivery.toDoubleOrNull() ?: brokerageRates.equityDeliveryPercent,
                    equityIntradayPercent = eqIntraday.toDoubleOrNull() ?: brokerageRates.equityIntradayPercent,
                    futuresDeliveryPercent = futDelivery.toDoubleOrNull() ?: brokerageRates.futuresDeliveryPercent,
                    futuresIntradayPercent = futIntraday.toDoubleOrNull() ?: brokerageRates.futuresIntradayPercent,
                    optionsDeliveryPercent = optDelivery.toDoubleOrNull() ?: brokerageRates.optionsDeliveryPercent,
                    optionsIntradayPercent = optIntraday.toDoubleOrNull() ?: brokerageRates.optionsIntradayPercent
                )
            )
        }
    }

    fun saveStatutory() {
        scope.launch {
            repository.saveStatutoryRates(
                StatutoryRates(
                    sttEquityDeliveryPercent = sttEqDelivery.toDoubleOrNull() ?: statutoryRates.sttEquityDeliveryPercent,
                    sttEquityIntradayPercent = sttEqIntraday.toDoubleOrNull() ?: statutoryRates.sttEquityIntradayPercent,
                    sttFuturesPercent = sttFutures.toDoubleOrNull() ?: statutoryRates.sttFuturesPercent,
                    sttOptionsPercent = sttOptions.toDoubleOrNull() ?: statutoryRates.sttOptionsPercent,
                    exchangeTxnEquityPercent = exchEquity.toDoubleOrNull() ?: statutoryRates.exchangeTxnEquityPercent,
                    exchangeTxnFuturesPercent = exchFutures.toDoubleOrNull() ?: statutoryRates.exchangeTxnFuturesPercent,
                    exchangeTxnOptionsPercent = exchOptions.toDoubleOrNull() ?: statutoryRates.exchangeTxnOptionsPercent,
                    sebiEquityFuturesPercent = sebiEqFut.toDoubleOrNull() ?: statutoryRates.sebiEquityFuturesPercent,
                    sebiOptionsPercent = sebiOptions.toDoubleOrNull() ?: statutoryRates.sebiOptionsPercent,
                    stampEquityDeliveryPercent = stampEqDelivery.toDoubleOrNull() ?: statutoryRates.stampEquityDeliveryPercent,
                    stampEquityIntradayPercent = stampEqIntraday.toDoubleOrNull() ?: statutoryRates.stampEquityIntradayPercent,
                    stampFuturesPercent = stampFutures.toDoubleOrNull() ?: statutoryRates.stampFuturesPercent,
                    stampOptionsPercent = stampOptions.toDoubleOrNull() ?: statutoryRates.stampOptionsPercent,
                    gstPercent = gst.toDoubleOrNull() ?: statutoryRates.gstPercent,
                    dpChargesFlat = dpCharges.toDoubleOrNull() ?: statutoryRates.dpChargesFlat
                )
            )
        }
    }

    val rateFields = listOf(
        Triple("Equity Delivery Brokerage (%)", eqDelivery) { v: String -> eqDelivery = v },
        Triple("Equity Intraday Brokerage (%)", eqIntraday) { v: String -> eqIntraday = v },
        Triple("Futures Delivery/Carry Brokerage (%)", futDelivery) { v: String -> futDelivery = v },
        Triple("Futures Intraday Brokerage (%)", futIntraday) { v: String -> futIntraday = v },
        Triple("Options Delivery/Carry Brokerage (%)", optDelivery) { v: String -> optDelivery = v },
        Triple("Options Intraday Brokerage (%)", optIntraday) { v: String -> optIntraday = v }
    )

    val statutoryFields = listOf(
        Triple("STT - Equity Delivery (%, both legs)", sttEqDelivery) { v: String -> sttEqDelivery = v },
        Triple("STT - Equity Intraday (%, sell leg)", sttEqIntraday) { v: String -> sttEqIntraday = v },
        Triple("STT - Futures (%, sell leg)", sttFutures) { v: String -> sttFutures = v },
        Triple("STT - Options (%, sell premium)", sttOptions) { v: String -> sttOptions = v },
        Triple("Exchange Txn Charges - Equity (%)", exchEquity) { v: String -> exchEquity = v },
        Triple("Exchange Txn Charges - Futures (%)", exchFutures) { v: String -> exchFutures = v },
        Triple("Exchange Txn Charges - Options (%, on premium)", exchOptions) { v: String -> exchOptions = v },
        Triple("SEBI Charges - Equity/Futures (%)", sebiEqFut) { v: String -> sebiEqFut = v },
        Triple("SEBI Charges - Options (%, on premium)", sebiOptions) { v: String -> sebiOptions = v },
        Triple("Stamp Duty - Equity Delivery (%, buy leg)", stampEqDelivery) { v: String -> stampEqDelivery = v },
        Triple("Stamp Duty - Equity Intraday (%, buy leg)", stampEqIntraday) { v: String -> stampEqIntraday = v },
        Triple("Stamp Duty - Futures (%, buy leg)", stampFutures) { v: String -> stampFutures = v },
        Triple("Stamp Duty - Options (%, buy leg)", stampOptions) { v: String -> stampOptions = v },
        Triple("GST (%)", gst) { v: String -> gst = v },
        Triple("DP Charges (\u20B9 flat, equity delivery sell)", dpCharges) { v: String -> dpCharges = v }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Set your own brokerage rates here. Statutory rates (STT, exchange, SEBI, stamp duty, GST) reflect government/exchange rules effective 1 April 2026 \u2014 edit only if these change in a future Budget or circular.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item { Text("Brokerage", style = MaterialTheme.typography.titleMedium) }

        items(rateFields) { (label, value, onChange) ->
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { saveBrokerage() }) { Text("Save Brokerage Rates") }
            }
        }

        item { Divider(modifier = Modifier.padding(vertical = 6.dp)) }

        item { Text("Statutory Charges (Advanced)", style = MaterialTheme.typography.titleMedium) }

        items(statutoryFields) { (label, value, onChange) ->
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { saveStatutory() }) { Text("Save Statutory Rates") }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    scope.launch { repository.resetToDefaults() }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All To Defaults")
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
