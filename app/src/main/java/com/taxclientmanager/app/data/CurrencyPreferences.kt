package com.taxclientmanager.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.currencyDataStore: DataStore<Preferences> by preferencesDataStore(name = "currency_prefs")

@Singleton
class CurrencyPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val CURRENCY_CODE = stringPreferencesKey("currency_code")
    private val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")

    val currencyCode: Flow<String> = context.currencyDataStore.data
        .map { preferences ->
            preferences[CURRENCY_CODE] ?: "BDT"
        }

    val currencySymbol: Flow<String> = context.currencyDataStore.data
        .map { preferences ->
            preferences[CURRENCY_SYMBOL] ?: "৳"
        }

    suspend fun saveCurrency(code: String, symbol: String) {
        context.currencyDataStore.edit { preferences ->
            preferences[CURRENCY_CODE] = code
            preferences[CURRENCY_SYMBOL] = symbol
        }
    }
}
