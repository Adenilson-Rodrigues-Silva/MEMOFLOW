package com.example.memoflow.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(private val context: Context, private val billingPrefs: BillingPrefs) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "BillingManager"

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.d(TAG, "Purchases updated: ${billingResult.responseCode}")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products = _products.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    init {
        startConnection()
    }

    fun startConnection(onComplete: (() -> Unit)? = null) {
        if (_isConnecting.value) return
        _isConnecting.value = true
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                _isConnecting.value = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Setup Success")
                    queryProducts()
                    queryPurchases()
                    onComplete?.invoke()
                } else {
                    Log.e(TAG, "Billing Setup Error: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnecting.value = false
                Log.w(TAG, "Billing Service Disconnected")
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_lifetime")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("donation_coffee")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("donation_snack")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("donation_meal")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = productDetailsList
            }
        }
    }

    fun queryPurchases() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Se a lista vier vazia e o usuário era premium, talvez a compra tenha sido reembolsada ou expirada
                var hasPremium = false
                for (purchase in purchases) {
                    if (purchase.products.contains("premium_lifetime") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        hasPremium = true
                        handlePurchase(purchase)
                    } else if (purchase.products.any { it.startsWith("donation_") }) {
                        handlePurchase(purchase)
                    }
                }
                
                // Se não encontrou premium na consulta atual, e não estamos em modo de teste local
                // Opcional: resetar premium se não encontrar na Play Store
                // scope.launch { if (!hasPremium) billingPrefs.setPremium(false) }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch {
                            if (purchase.products.contains("premium_lifetime")) {
                                billingPrefs.setPremium(true)
                            }
                        }
                    }
                }
            } else {
                scope.launch {
                    if (purchase.products.contains("premium_lifetime")) {
                        billingPrefs.setPremium(true)
                    }
                }
            }

            if (purchase.products.any { it.startsWith("donation_") }) {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.consumeAsync(consumeParams) { _, _ -> }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String) {
        val productDetails = _products.value.find { it.productId == productId }
        if (productDetails == null) {
            Log.e(TAG, "Product $productId not found in details")
            queryProducts()
            return
        }
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
}
