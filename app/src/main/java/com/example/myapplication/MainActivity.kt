package com.example.myapplication

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CallAPILoginAsyncTask().execute()
    }
    private inner class CallAPILoginAsyncTask() :
        AsyncTask<Any, Void, String>() {
        private lateinit var customProgressDialog: Dialog
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }
        override fun doInBackground(vararg params: Any): String {
            var result: String
            var connection: HttpURLConnection? = null
            try {
                val url = URL("http://www.mocky.io/v2/5e3826143100006a00d37ffa")
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)

                val jsonRequest = JSONObject()
                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {

                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }

            return result
        }


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            cancelProgressDialog()

            val jsonObject = JSONObject(result)
            val message = jsonObject.optString("message")
            val userId = jsonObject.optInt("user_id")
            val name = jsonObject.optString("name")
            val email = jsonObject.optString("email")
            val mobileNumber = jsonObject.optLong("mobile")

            val profileDetailsObject = jsonObject.optJSONObject("profile_details")

            val isProfileCompleted = profileDetailsObject.optBoolean("is_profile_completed")
            Log.i("Is Profile Completed", "$isProfileCompleted")

            val rating = profileDetailsObject.optDouble("rating")
            Log.i("Rating", "$rating")

            // Returns the value mapped by {name} if it exists.
            val dataListArray = jsonObject.optJSONArray("data_list")
            Log.i("Data List Size", "${dataListArray.length()}")

            for (item in 0 until dataListArray.length()) {
                Log.i("Value $item", "${dataListArray[item]}")
                val dataItemObject: JSONObject = dataListArray[item] as JSONObject
                val id = dataItemObject.optString("id")
                Log.i("ID", "$id")
                val value = dataItemObject.optString("value")
                Log.i("Value", "$value")
            }
        }

        private fun showProgressDialog() {
            customProgressDialog = Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.show()
        }
        private fun cancelProgressDialog() {
            customProgressDialog.dismiss()
        }
    }
}