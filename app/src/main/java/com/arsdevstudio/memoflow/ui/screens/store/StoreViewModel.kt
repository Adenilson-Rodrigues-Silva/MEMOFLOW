package com.arsdevstudio.memoflow.ui.screens.store

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arsdevstudio.memoflow.MemoApplication
import com.arsdevstudio.memoflow.utils.BillingManager
import com.arsdevstudio.memoflow.utils.BillingPrefs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as MemoApplication
    private val billingManager: BillingManager = app.billingManager
    private val billingPrefs: BillingPrefs = app.billingPrefs

    private val repository = app.repository

    val isPremium: StateFlow<Boolean> = repository.userSettings
        .map { it?.isPremium ?: false }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    val products = billingManager.products
    
    private val _purchaseEvents = MutableSharedFlow<BillingManager.PurchaseEvent>()
    val purchaseEvents = _purchaseEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            billingManager.purchaseEvents.collect { event ->
                _purchaseEvents.emit(event)
            }
        }
    }

    fun buyPremium(activity: Activity) {
        billingManager.launchBillingFlow(activity, "premiumlifetime")
    }

    fun restorePurchases() {
        billingManager.queryPurchases()
    }

    fun donate(activity: Activity, type: String) {
        val productId = when (type) {
            "coffee" -> "donationcoffee"
            "snack" -> "donationsnack"
            "meal" -> "donationmeal"
            else -> return
        }
        billingManager.launchBillingFlow(activity, productId)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoreViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

