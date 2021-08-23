package com.aashdit.olmoffline.ui.activities

import android.R
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aashdit.olmoffline.BuildConfig
import com.aashdit.olmoffline.databinding.ActivityLoginBinding
import com.aashdit.olmoffline.receiver.ConnectivityChangeReceiver
import com.aashdit.olmoffline.utils.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity(), ConnectivityChangeReceiver.ConnectivityReceiverListener {

    private var isActivityDestroyed = false
    private val myHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            isActivityDestroyed = isFinishing
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private var username: String = ""
    private var password: String = ""


    private var mConnectivityChangeReceiver: ConnectivityChangeReceiver? = null
    private var isConnected = false

    private var sp: SharedPrefManager? = null
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(base))
    }

    protected fun resetTitles() {
        try {
            val info = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)
            if (info.labelRes != 0) {
                setTitle(info.labelRes)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resetTitles()
        sp = SharedPrefManager.getInstance(this)

        binding.progressLogin.visibility = View.GONE
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        AndroidNetworking.initialize(applicationContext, okHttpClient)

        mConnectivityChangeReceiver = ConnectivityChangeReceiver()
        mConnectivityChangeReceiver!!.setConnectivityReceiverListener(this)
        isConnected = mConnectivityChangeReceiver!!.getConnectionStatus(this)
        registerNetworkBroadcast()
        binding.rlLogin.setOnClickListener {
            username = binding.etLoginUsername.text.toString()
            password = binding.etLoginPassword.text.toString()

            if (username.isEmpty() or (username == "")) {
                Snackbar.make(binding.loginRoot, "Please enter username", Snackbar.LENGTH_LONG)
                    .show()
            } else if (password.isEmpty() or (password == "")) {
                Snackbar.make(binding.loginRoot, "Please enter password", Snackbar.LENGTH_LONG)
                    .show()
            } else {
//                binding.progressLogin.visibility = View.VISIBLE
                if (username != "" && password != "") {
                    doLogin()
//                    val homeIntent = Intent(this@LoginActivity, DashboardActivity::class.java)
//                    startActivity(homeIntent)
//                    binding.progressLogin.visibility = View.GONE
//                    finish()
                } else {
                    Snackbar.make(binding.loginRoot, "Invalid Credentials", Snackbar.LENGTH_LONG)
                        .show()
                    binding.progressLogin.visibility = View.GONE
                }
            }
        }

        binding.tvLoginForgetPassword.setOnClickListener {
//            ForgetPasswordDialog(this)

            AlertDialog.Builder(this)
                .setTitle("Forgot Password ?")
                .setMessage("Please Contact your admin.") // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.yes) { dialog, which ->
                    dialog.dismiss()// Continue with delete operation
//                        setNewLocale(this@SettingsActivity, LocaleManager.ODIA)
                } // A null listener allows the button to dismiss the dialog and take no further action.
//                    .setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }
                .setIcon(R.drawable.ic_dialog_alert)
                .show()

        }

    }

    private fun doLogin() {

        val loginObj = JSONObject()
        loginObj.put("userName", username)
        loginObj.put("password", password)

        binding.progressLogin.visibility = View.VISIBLE
        AndroidNetworking.post(BuildConfig.BASE_URL + Constant.BASE_PATH + "api/login")//for "olm-demo/api/login" sujit panigrahi
            .addJSONObjectBody(loginObj)
            .setTag("Login")
            .setPriority(Priority.HIGH)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    binding.progressLogin.visibility = View.GONE
                    if (Utility.isStringValid(response)) {

                        try {
                            val resObj = JSONObject(response)
                            if (resObj.optBoolean("outcome")) {

                                sp!!.setStringData(Constant.APP_TOKEN, resObj.optString("data"))
                                sp!!.setStringData(Constant.USER_NAME, username)
                                sp!!.setStringData(Constant.USER_PASSWORD, password)
                                sp!!.setBoolData(Constant.APP_LOGIN, true)


                                val jwt = JWTUtils.decoded(resObj.optString("data"))
                                if (Utility.isStringValid(jwt)) {
                                    try {
                                        val jwtRes = JSONObject(jwt)
                                        Log.i(
                                            TAG,
                                            "onResponse: " + jwtRes.optJSONArray("authorities")
                                        )
                                        val jwtArray = jwtRes.optJSONArray("authorities")
                                        if (jwtArray.length() > 0) {
                                            val role = jwtArray.get(0)
                                            Log.i(TAG, "onResponse: " + role)
                                            sp!!.setStringData(
                                                Constant.USER_ROLE,
                                                role!! as String?
                                            )

                                            moveToMainActivity()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                            } else {

                                if (!this@LoginActivity.isFinishing) {
                                    AlertDialog.Builder(this@LoginActivity)
                                        .setTitle("Wrong Credentials")
                                        .setMessage(resObj.optString("message")) // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(R.string.yes) { dialog, which ->
                                            dialog.dismiss()// Continue with delete operation
//                        setNewLocale(this@SettingsActivity, LocaleManager.ODIA)
                                        } // A null listener allows the button to dismiss the dialog and take no further action.
//                    .setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }
                                        .setIcon(R.drawable.ic_dialog_alert)
                                        .show()
                                }
                                
//                                if (!isActivityDestroyed)

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }


                    }
                }

                override fun onError(anError: ANError) {
                    Log.e(TAG, "onError: " + anError.errorDetail)
                    binding.progressLogin.visibility = View.GONE
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

    private fun registerNetworkBroadcast() {
        registerReceiver(
            mConnectivityChangeReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    protected fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mConnectivityChangeReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun moveToMainActivity() {
        val dashboardIntent = Intent(this@LoginActivity, NewDashboardActivity::class.java)
        startActivity(dashboardIntent)
        finish()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
//            Toast.makeText(this,"Please Check your internet connectivity & try later",Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this,
                "Please Check your internet connectivity & try later",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    override fun onDestroy() {
        unregisterNetworkChanges()
        super.onDestroy()
    }
}