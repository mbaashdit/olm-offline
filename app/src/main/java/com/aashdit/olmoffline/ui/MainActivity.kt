package com.aashdit.olmoffline.ui

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.aashdit.olmoffline.BuildConfig
import com.aashdit.olmoffline.R
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver
import com.aashdit.olmoffline.ui.activities.LoginActivity
import com.aashdit.olmoffline.ui.activities.NewDashboardActivity
import com.aashdit.olmoffline.utils.Constant
import com.aashdit.olmoffline.utils.SharedPrefManager
import com.aashdit.olmoffline.utils.Utility
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), ConnectivityChangeReceiver.ConnectivityReceiverListener {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var sp : SharedPrefManager? = null

    private var mConnectivityChangeReceiver: ConnectivityChangeReceiver? = null
    private var isConnected = false
    private var handler = Handler()
    private val runnable = Runnable {
        val homeIntent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(homeIntent)
        finish()
    }

    private var username = ""
    private var password = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sp = SharedPrefManager.getInstance(this)

        val version = BuildConfig.VERSION_NAME
        tv_version.text = "version : $version"

        mConnectivityChangeReceiver = ConnectivityChangeReceiver()
        mConnectivityChangeReceiver!!.setConnectivityReceiverListener(this)
        isConnected = mConnectivityChangeReceiver!!.getConnectionStatus(this)
        registerNetworkBroadcast()
        if (sp!!.getBoolData(Constant.APP_LOGIN)){
            username = sp!!.getStringData(Constant.USER_NAME)
            password = sp!!.getStringData(Constant.USER_PASSWORD)

            doLogin()
        }else{
            handler.postDelayed(runnable, 1000)
        }
    }

    private fun registerNetworkBroadcast() {
        registerReceiver(
            mConnectivityChangeReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    private fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mConnectivityChangeReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
    private fun doLogin() {

        val loginObj = JSONObject()
        loginObj.put("userName", username)
        loginObj.put("password", password)

        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.BASE_PATH+"api/login")//"olm-demo/api/login" for sujit panigrahi
            .addJSONObjectBody(loginObj)
            .setTag("Login")
            .setPriority(Priority.HIGH)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    if (Utility.isStringValid(response)) {

                        try {
                            val resObj = JSONObject(response)
                            if (resObj.optBoolean("outcome")) {

                                sp!!.setStringData(Constant.APP_TOKEN, resObj.optString("data"))
                                sp!!.setStringData(Constant.USER_NAME, username)
                                sp!!.setStringData(Constant.USER_PASSWORD, password)
                                sp!!.setBoolData(Constant.APP_LOGIN, true)

                                moveToMainActivity()
                            } else {
//                                AlertDialog.Builder(applicationContext)
//                                        .setTitle("Wrong Credentials")
//                                        .setMessage(resObj.optString("message")) // Specifying a listener allows you to take an action before dismissing the dialog.
//                                        // The dialog is automatically dismissed when a dialog button is clicked.
//                                        .setPositiveButton(android.R.string.yes) { dialog, which -> dialog.dismiss()// Continue with delete operation
////                        setNewLocale(this@SettingsActivity, LocaleManager.ODIA)
//                                        } // A null listener allows the button to dismiss the dialog and take no further action.
////                    .setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }
//                                        .setIcon(android.R.drawable.ic_dialog_alert)
//                                        .show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onError(anError: ANError) {
                    Log.e(TAG, "onError: " + anError.errorDetail)
//                    try {
//                        val errObj = JSONObject(anError.errorDetail)
//                        val statusCode = errObj.optInt("status")
//                        if (statusCode == 500) {
//                            sp!!.clear()
//                            val intent = Intent(applicationContext, MainActivity::class.java)
//                            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
//                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
//                            finish()
//                        }
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                    }
                }
            })
    }

    private fun moveToMainActivity() {
        val dashboardIntent = Intent(this@MainActivity, NewDashboardActivity::class.java)
        startActivity(dashboardIntent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
//            Toast.makeText(this,"Please Check your internet connectivity & try later",Toast.LENGTH_LONG).show()
        }else {
            if (sp!!.getBoolData(Constant.APP_LOGIN)){
                moveToMainActivity()
            }else{

            }

            Toast.makeText(this,"Please Check your internet connectivity & try later", Toast.LENGTH_LONG).show()
        }
    }


    override fun onDestroy() {
        unregisterNetworkChanges()
        super.onDestroy()
    }
}