package com.fitness.XXXX.billing.utilbilling

import android.app.Activity
import com.android.billingclient.api.*
import com.fitness.XXXX.R
import com.fitness.XXXX.billing.BillingListner

class BillingConsume (var activity: Activity, var bilingListner: BillingListner): PurchasesUpdatedListener, PurchaseHistoryResponseListener {
    private var billingClient: BillingClient? = null

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
//if item newly purchased
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            bilingListner.onConsumableSucess(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
            val alreadyPurchases = queryAlreadyPurchasesResult.purchasesList
            alreadyPurchases?.let {  bilingListner.onConsumableSucess(it)}
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            bilingListner.isBillingFailed("You cancelled our billing.")
         } else {
            bilingListner.isBillingFailed("Error !"+billingResult.debugMessage)
         }
    }



    fun reconnectService(productId: String) {
        billingClient = BillingClient.newBuilder(activity).enablePendingPurchases().setListener(this).build()
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    callPurchases(productId)
                }
                else {
                    bilingListner.isBillingFailed("Error" + billingResult.debugMessage)
                }
            }
            override fun onBillingServiceDisconnected() {
                bilingListner.isBillingFailed(activity.getString(R.string.billing_disconnted))
            }
        })

    }


    override fun onPurchaseHistoryResponse(p0: BillingResult, p1: MutableList<PurchaseHistoryRecord>?) {
     }

    fun intitateBilling() {
        billingClient = BillingClient.newBuilder(activity).enablePendingPurchases().setListener(this).build()
    }


    /*check the dependency*/
    fun isBillingReadyOrnot(): Boolean {
        intitateBilling()
        return (billingClient!!.isReady)
    }


    fun callPurchases(data:String){
        val skuList: MutableList<String> = ArrayList()
        skuList.add(data)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient!!.querySkuDetailsAsync(params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    bilingListner.billingWindow(billingClient!!,flowParams,this)
                 }
                else {
                    bilingListner.isBillingFailed(activity.getString(R.string.billing_disconnted))

                }
            }
            else {
                bilingListner.isBillingFailed(activity.getString(R.string.billing_disconnted))

            }
        }

    }

    public fun handlePurchases(purchases: List<Purchase>,PRODUCT_ID:String){
        for (purchase in purchases) {
            //if item is purchased
            if (PRODUCT_ID == purchase.sku && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

                if (!purchase.isAcknowledged) {
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient!!.consumeAsync(consumeParams, ConsumeResponseListener{
                            billingResult, purchaseToken ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            bilingListner.saveToLocalDataAndPerformAccordingly(BillingKeys.PerChallenge)

                    }}
                    )
                }
            } else if (PRODUCT_ID == purchase.sku && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                bilingListner.isBillingFailed("Purchase is pending")
            } else if (PRODUCT_ID == purchase.sku && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                bilingListner.isBillingFailed("Purchase state unknown")

            }
        }
    }



    }





