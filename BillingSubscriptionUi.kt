package com.fitness.XXXX.billing.billingui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.android.billingclient.api.*
import com.fitness.XXXX.R
import com.fitness.XXXX.billing.BillingListner
import com.fitness.XXXX.billing.utilbilling.BillingClass
import com.fitness.XXXX.billing.utilbilling.BillingConsume
import com.fitness.XXXX.billing.utilbilling.BillingKeys
import com.fitness.XXXX.databinding.FragmentBillingSubscriptionUiBinding
import com.fitness.XXXX.models.BillingData
import com.fitness.XXXX.session.SessionManager
import com.fitness.XXXX.ui.auth.AuthViewModel
import com.fitness.XXXX.utils.*
import javax.inject.Inject


class BillingSubscriptionUi : AppCompatActivity() ,BillingListner,CommonUtil.CallRestoreClcik{
    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory
    lateinit var mContext: Context
 private lateinit var mBinding:FragmentBillingSubscriptionUiBinding
       lateinit var mBilling: BillingClass
          var mKey:String=""
    var Type=0
    private lateinit var mBillingConsume:BillingConsume

    val viewModel: AuthViewModel by viewModels {
        providerFactory
    }
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

         mBinding=DataBindingUtil.setContentView(this,R.layout.fragment_billing_subscription_ui)
        mBinding.billing=this
        mContext=this
         mBilling= BillingClass(this,this)
        CommonUtil.hideKeyboard(this)
        Type=intent.getIntExtra("Type",0);
        mBillingConsume= BillingConsume(this,this)
        if(Type==0){
        mBinding.oneTimeAccess.visibility= GONE;
            mBinding.normalAcces.visibility= VISIBLE}
        else{
            mBinding.normalAcces.visibility= GONE
            mBinding.oneTimeAccess.visibility= VISIBLE;
        }
        mBillingConsume.intitateBilling()
          mBilling.intitateBilling()
        onClick()
    }

    fun restoreClick(){
       mBilling.callRestoration()
    }

    fun onClick(){

        mBinding.allAccess.setOnCheckedChangeListener { compoundButton, b ->

            mBinding.challenge.isChecked =false
            mBinding.fitness.isChecked =false
            mBinding.nutritionProgram.isChecked =false
            if(b)
                mKey=BillingKeys.AllAccess;
            else
                mKey=""

        }

        mBinding.challenge.setOnCheckedChangeListener { compoundButton, b ->

            mBinding.allAccess.isChecked =false
            mBinding.fitness.isChecked =false
            mBinding.nutritionProgram.isChecked =false
            if(b)
                mKey=BillingKeys.AllAccess
            else
                mKey=""
        }

        mBinding.fitness.setOnCheckedChangeListener { compoundButton, b ->

            mBinding.allAccess.isChecked =false
            mBinding.challenge.isChecked =false
            mBinding.nutritionProgram.isChecked =false
            if(b)
                mKey=BillingKeys.XXXXFitness
            else
                mKey=""

        }

        mBinding.nutritionProgram.setOnCheckedChangeListener { compoundButton, b ->

            mBinding.allAccess.isChecked =false
            mBinding.fitness.isChecked =false
            mBinding.challenge.isChecked =false
            if(b)
                mKey=BillingKeys.XXXXNutrition
            else
                mKey=""

        }
    }
    fun crossClick(){
        val returnIntent = Intent()
        setResult(RESULT_CANCELED, returnIntent)
        finish()


    }

    fun onBillingClick() {
        if(!TextUtil.isEmptyOrNull(mKey)){
            if(!TextUtils.equals(mKey,BillingKeys.PerChallenge)){
               if(mBillingConsume.isBillingReadyOrnot())
                   mBillingConsume.callPurchases(mKey)
                else
                    mBillingConsume.reconnectService(mKey)

            }
             else{
                if(mBilling.isBillingReadyOrnot())
              mBilling.callSubscribe(mKey)
            else
            mBilling.reconnectService(mKey)
          }}
        else{
            PopupUtil.showSnackBar(this, mBinding.root, getString(R.string.select_the_subscirpiton));
        //            CommonUtil.showErrorDailog(mContext,getString(R.string.select_the_subscirpiton))
        }


    }

    override fun isBillingFailed(reason: String) {
        PopupUtil.showSnackBar(this, mBinding.root, reason);
//        CommonUtil.showErrorDailog(mContext,reason)
     }

    override fun billingWindow(billingClient: BillingClient, launchParams: BillingFlowParams, purchasesUpdatedListener: PurchasesUpdatedListener) {
        billingClient.launchBillingFlow(this, launchParams);
    }

    override fun cancelBilling() {
        CommonUtil.showErrorDailog(mContext,getString(R.string.billing_cancel))
    }

    override fun isAlreadyPurchased() {
        CommonUtil.openRestorePuchases(this,this)
    }

    override fun onConsumableSucess(purchases: MutableList<Purchase>) {
        mBillingConsume.handlePurchases(purchases,mKey)
    }

    override fun callRestoration(billingCleint: BillingClient,purchaseHistory: PurchaseHistoryResponseListener) {
        billingCleint.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, purchaseHistory)
    }

    override fun purchaseSucess(purchases: MutableList<Purchase>) {
        mBilling.handlePurchases(purchases,mKey)

    }

    fun saveSubscriptionDetails(data:String){
        var map=HashMap<String,Any>()
        var  dataClass:BillingData
        var  subscribeBill= BillingData("Auto-Renewable Subscription",true)
        var subscriptioeNot=BillingData("Auto-Renewable Subscription",false)
             if(data.equals(BillingKeys.PerChallenge))
           dataClass = BillingData("Non-Consumable",true)
        else
           dataClass= BillingData("Non-Consumable",false)
        map.put(Constant.ACCESSALL,dataClass)

         if(data.equals(BillingKeys.PerChallenge)){
        map.put(Constant.CHALLENGE,subscribeBill)}
        else
             map.put(Constant.CHALLENGE,subscriptioeNot)
        if(data.equals(BillingKeys.XXXXNutrition)){
        map.put(Constant.NUTRITION,subscribeBill)}
        else
            map.put(Constant.NUTRITION,subscriptioeNot)
          if(data.equals(BillingKeys.XXXXFitness))

        map.put(Constant.WORKOUT,subscribeBill)
        else

              map.put(Constant.WORKOUT,subscriptioeNot)

        viewModel.onSetSubscribeBilling(map).observe(this){ resources->
            resources.let {
                when(resources.status){
                    Status.LOADING -> CommonUtil.showProgressBar(this)

                    Status.SUCCESS -> {
                        CommonUtil.hideProgressBar()
                        val returnIntent = Intent()
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    }
                    Status.ERROR -> {
                        CommonUtil.hideProgressBar()

                    }
                }
            }

        }
    }



    override fun saveToLocalDataAndPerformAccordingly(data: String) {
        when(data){
            BillingKeys.XXXXFitness-> sessionManager.setFitnessType(this,data)
            BillingKeys.XXXXNutrition->sessionManager.setNutritonType(this,data)
            BillingKeys.AllAccess->sessionManager.setAllType(this,data)
            BillingKeys.PerChallenge->sessionManager.setChallangeType(this,data)
        }
        saveSubscriptionDetails(data)


    }

    override fun savetheList(list: MutableList<PurchaseHistoryRecord>?) {
        for(item in list!!.indices){
            when(list.get(item).sku){
                BillingKeys.AllAccess->sessionManager.setAllType(this,list.get(item).sku)
                BillingKeys.XXXXFitness->sessionManager.setFitnessType(this,list.get(item).sku)
                BillingKeys.XXXXNutrition->sessionManager.setNutritonType(this,list.get(item).sku)
                BillingKeys.PerChallenge->sessionManager.setNutritonType(this,list.get(item).sku)
            }
            }
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
        }

    override fun clcikRestore() {
         restoreClick()
    }


}