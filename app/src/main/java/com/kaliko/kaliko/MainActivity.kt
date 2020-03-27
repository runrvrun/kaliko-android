package com.kaliko.kaliko

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    internal var RegisterURL = "http://localhost/register"
    private var etname: EditText? = null
    private var ethobby:EditText? = null
    private var etusername:EditText? = null
    private var etpassword:EditText? = null
    private var btnregister: Button? = null
    private var tvlogin: TextView? = null
    private var tvskiplogin: TextView? = null
    private var preferenceHelper: PreferenceHelper? = null
    private val RegTask = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceHelper = PreferenceHelper(this)

        if (preferenceHelper!!.getIsLogin()) {
            val intent = Intent(this@MainActivity, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            this.finish()
        }


        etname = findViewById<View>(R.id.etname) as EditText
        etusername = findViewById<View>(R.id.etusername) as EditText
        etpassword = findViewById<View>(R.id.etpassword) as EditText

        btnregister = findViewById<View>(R.id.btn) as Button
        tvlogin = findViewById<View>(R.id.tvlogin) as TextView

        tvlogin!!.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        btnregister!!.setOnClickListener {
            try {
                register()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        tvskiplogin = findViewById<View>(R.id.tvskiplogin) as TextView
        tvskiplogin!!.setOnClickListener {
            val intent = Intent(this@MainActivity, DashboardActivity::class.java)
            startActivity(intent)
        }


    }

    @Throws(IOException::class, JSONException::class)
    private fun register() {

        try {

            Fuel.post(RegisterURL, listOf("name" to  etname!!.text.toString()
                , "hobby" to  ethobby!!.text.toString()
                , "username" to  etusername!!.text.toString()
                , "password" to  etpassword!!.text.toString()
            )).responseJson { request, response, result ->
                Log.d("registerpost", result.get().content)
                onTaskCompleted(result.get().content,RegTask)
            }
        } catch (e: Exception) {

        } finally {

        }

    }

    private fun onTaskCompleted(response: String, task: Int) {
        Log.d("responsejson", response)
        when (task) {
            RegTask -> if (isSuccess(response)) {
                saveInfo(response)
                Toast.makeText(this@MainActivity, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                this.finish()
            } else {
                Toast.makeText(this@MainActivity, getErrorMessage(response), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveInfo(response: String) {
        preferenceHelper!!.putIsLogin(true)
        try {
            val jsonObject = JSONObject(response)
            if (jsonObject.getString("status") == "true") {
                val dataArray = jsonObject.getJSONArray("data")
                for (i in 0 until dataArray.length()) {

                    val dataobj = dataArray.getJSONObject(i)
                    preferenceHelper!!.putName(dataobj.getString("name"))
                    preferenceHelper!!.putHobby(dataobj.getString("hobby"))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun isSuccess(response: String): Boolean {
        try {
            val jsonObject = JSONObject(response)
            return if (jsonObject.optString("status") == "true") {
                true
            } else {

                false
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return false
    }

    fun getErrorMessage(response: String): String {
        try {
            val jsonObject = JSONObject(response)
            return jsonObject.getString("message")

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return "No data"
    }

}