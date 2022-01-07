package com.fitness.XXXX.billing.utilbilling

import android.app.Activity
import android.text.TextUtils
import android.widget.Toast
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.SUBS
import com.fitness.XXXX.R
import com.fitness.XXXX.billing.BillingListner


class BillingClass(var activity:Activity,var bilingListner: BillingListner):   PurchasesUpdatedListener,PurchaseHistoryResponseListener {
    private var billingClient: BillingClient? = null

    /*initialize*/
      fun intitateBilling() {
        billingClient = BillingClient.newBuilder(activity).enablePendingPurchases().setListener(this).build()
    }


    /*check the dependency*/
      fun isBillingReadyOrnot(): Boolean {
        intitateBilling()
         return (billingClient!!.isReady)
    }


    /*after click*/
      fun callSubscribe(productId: String) {
        val skuList: MutableList<String> = ArrayList()
//        skuList.add("android.test.purchased")
         skuList.add(productId)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(SUBS)

        billingClient!!.querySkuDetailsAsync(params.build())
        { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    bilingListner.billingWindow(billingClient!!, flowParams, this)
                } else {
                    bilingListner.isBillingFailed(activity.getString(R.string.no_item_found))
                }
            } else {
                bilingListner.isBillingFailed("Error! " + billingResult.debugMessage)

            }
        }
    }



    fun callRestoration(){
        bilingListner.callRestoration(billingClient!!,this)
    }
    /*purchaseUpdate*/
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

            bilingListner.purchaseSucess(purchases)
//                handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult = billingClient!!.queryPurchases(SUBS)
            val alreadyPurchases: List<Purchase>? = queryAlreadyPurchasesResult.purchasesList
            if (alreadyPurchases != null) {
                bilingListner.isAlreadyPurchased()
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            bilingListner.cancelBilling()
        } else {
            bilingListner.isBillingFailed("Error! " + billingResult.debugMessage)
        }


    }




    /*reconnect service*/
      fun reconnectService(productId: String) {
        billingClient =
            BillingClient.newBuilder(activity).enablePendingPurchases().setListener(this).build()
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    callSubscribe(productId)
                } else {
                    bilingListner.isBillingFailed("Error" + billingResult.debugMessage)
                }

            }

            override fun onBillingServiceDisconnected() {
                bilingListner.isBillingFailed(activity.getString(R.string.billing_disconnted))

            }
        })

    }


    /*handle purchase Results*/
         fun handlePurchases(purchases: List<Purchase>,PRODUCT_ID:String) {
        for (purchase in purchases) {

            //if item is purchased

            //if item is purchased
            if (PRODUCT_ID.equals(purchase.sku) && purchase.purchaseState === Purchase.PurchaseState.PURCHASED) {
                  if (!purchase.isAcknowledged) {
                      if(!TextUtils.equals(PRODUCT_ID,BillingKeys.PerChallenge)){
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                      billingClient!!.acknowledgePurchase(acknowledgePurchaseParams,
                          AcknowledgePurchaseResponseListener { billingResult ->
                              if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                                  bilingListner.saveToLocalDataAndPerformAccordingly(PRODUCT_ID)


                              }
                          })}
                      else{

                          val consumeParams = ConsumeParams.newBuilder()
                              .setPurchaseToken(purchase.purchaseToken)
                              .build()
                          billingClient!!.consumeAsync(consumeParams,
                          ConsumeResponseListener{ billingResult, purchaseToken ->
                              if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                                  val consumeCountValue = purchaseCountValueFromPref + 1
//                                  savePurchaseCountValueToPref(consumeCountValue)
//                                  Toast.makeText(applicationContext, "Item Consumed", Toast.LENGTH_SHORT).show()
//                                  consumeCount!!.text = "Item Consumed $purchaseCountValueFromPref Time(s)"
                              }
                          })
                      }



                } else {
                    // Grant entitlement to the user on item purchase
                    // restart activity

                }
            } else if (PRODUCT_ID.equals(purchase.sku) && purchase.purchaseState === Purchase.PurchaseState.PENDING) {


            } else if (PRODUCT_ID.equals(purchase.sku) && purchase.purchaseState === Purchase.PurchaseState.UNSPECIFIED_STATE) {

            }


        }
        }






     private fun callPurchase(productId: String){
         val skuList: MutableList<String> = ArrayList()
         skuList.add(productId)
         val params = SkuDetailsParams.newBuilder()
         params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
         billingClient!!.querySkuDetailsAsync(params.build()
         ) {
                 billingResult, skuDetailsList ->
             if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                 if (skuDetailsList != null && skuDetailsList.size > 0) {
                     val flowParams = BillingFlowParams.newBuilder()
                         .setSkuDetails(skuDetailsList[0])
                         .build()
                     bilingListner!!.billingWindow(billingClient!!, flowParams,this)
                 }
                 else {
                     //try to add item/product id "c1" "c2" "c3" inside managed product in google play console
                     bilingListner.isBillingFailed("Purchase Item $productId not Found")
                 }
             }
             else {
                 bilingListner.isBillingFailed(" Error " + billingResult.debugMessage)

             }
         }

     }

    override fun onPurchaseHistoryResponse(billingResult: BillingResult, list: MutableList<PurchaseHistoryRecord>?) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (list!!.isEmpty() || list.size == 0) {
                bilingListner.isBillingFailed("Not any purchase found to be have right now.")

            } else {

                 bilingListner.savetheList(list)




            }
        } else {
            bilingListner.isBillingFailed("Issues getting the purchases")
        }
    }
}

