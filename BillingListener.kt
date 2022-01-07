package com.fitness.XXXX.billing

import com.android.billingclient.api.*

interface BillingListner {
      fun isBillingFailed(reason: String)
      fun billingWindow(billingCleint:BillingClient,launchParams: BillingFlowParams,purchasesUpdatedListener: PurchasesUpdatedListener)
      fun cancelBilling()
      fun isAlreadyPurchased()
      fun onConsumableSucess(purchases:MutableList<Purchase>)
      fun callRestoration(billingCleint: BillingClient,history: PurchaseHistoryResponseListener);
      fun purchaseSucess(purchases:MutableList<Purchase>)
      fun saveToLocalDataAndPerformAccordingly(data:String)
      fun savetheList( list: MutableList<PurchaseHistoryRecord>?)
}