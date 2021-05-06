package com.example.smarthomegesturecontrol

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import org.conscrypt.Conscrypt
import java.io.File
import java.io.IOException
import java.security.Security
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PracticeRecordActivity : AppCompatActivity() {
    private var intentselect: String? = null
    private var practicecount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice_record)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val i = intent
        intentselect = i.getStringExtra("gesture_name")
        gestureToRecord = gestureMap.get(intentselect)?.first as String

        // Record Button Listener
        findViewById<View?>(R.id.btnRecord).setOnClickListener { startPracticeRecording() }

        // Upload Button Listener
        findViewById<View?>(R.id.btnUpload).setOnClickListener {
            Toast.makeText(applicationContext, "Sending post Request to Flask Server", Toast.LENGTH_LONG).show()
            postRequest()
        }
    }

//  Start Recording Function (Reference: Prof. Ayan Banerjee's "AndroidHelloWorldThursday" sample code given in class)
    fun startPracticeRecording() {
        println("Start Recording..............................")
        practicecount = gestureMap.get(intentselect)?.second as Int
        val recordedFile = File(getExternalFilesDir(null).toString() + "/" + gestureToRecord + "_PRACTICE_" + practicecount + "_JANA.mp4")
        recordedFileName = recordedFile.absolutePath
        val i = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        // Set Camera to Front Camera (Reference: Stackoverflow.com)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            i.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        } else {
            i.putExtra("android.intent.extras.CAMERA_FACING", 1);
        }
        i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        i.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 4)
        val videoUri = FileProvider.getUriForFile(this, "com.example.smarthomegesturecontrol.provider", recordedFile)
        i.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        startActivityForResult(i, VIDEO_CAPTURE)
    }

//  Check if phone has a camera (Reference: Prof. Ayan Banerjee's "AndroidHelloWorldThursday" sample code given in class)
    private fun hasCamera(): Boolean {
        return packageManager.hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)
    }

//  Inform user if video recorded and saved successfully (Reference: Prof. Ayan Banerjee's "AndroidHelloWorldThursday" sample code given in class)
    override fun onActivityResult(
            rqstCode: Int,
            rsltCode: Int, data: Intent?
    ) {
        super.onActivityResult(rqstCode, rsltCode, data)
        if (rqstCode == VIDEO_CAPTURE) {
            if (rsltCode == RESULT_OK) {
                Toast.makeText(this, """
     Practice Video has been saved to:
     ${data?.getData()}
     """.trimIndent(), Toast.LENGTH_LONG).show()
            } else if (rsltCode == RESULT_CANCELED) {
                Toast.makeText(this, "Practice Video recording cancelled.",
                        Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to record Practice video",
                        Toast.LENGTH_LONG).show()
            }
        }
        practicecount++

        gestureMap.put(intentselect, gestureMap.get(intentselect)?.copy(second = practicecount))
    }

//  Function to post request to Flask Server (Reference: Stackoverflow.com)
    fun postRequest() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        val arrD: Array<String?> = recordedFileName?.split("/".toRegex())!!.toTypedArray()
        val practiceFileName = arrD[8]
        val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", practiceFileName, RequestBody.create("video/mp4".toMediaTypeOrNull(), File(recordedFileName))).build()
        val okHttpClient = OkHttpClient()
        // My Flask Server IP Address = http://192.168.0.120:5000/
        val request: Request = Request.Builder().url("http://192.168.0.120:5000/uploader").post(requestBody).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
                runOnUiThread { Toast.makeText(applicationContext, "Something went wrong:" + " ", Toast.LENGTH_SHORT).show() }
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val response_body = response.body?.string()
                        println(response_body)
                        Toast.makeText(applicationContext, response_body, Toast.LENGTH_LONG).show()
                        try {
                            Thread.sleep(2000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        // Go back to Mainactivity once Practice Video uploaded to Server
                        val gotoMainActivity = Intent(this@PracticeRecordActivity, MainActivity::class.java)
                        startActivity(gotoMainActivity)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    companion object {
        private const val VIDEO_CAPTURE = 101
        private var recordedFileName: String? = null
        private var gestureToRecord: String? = null
        // Hashmap to maintain the gesture name and Practice Count for each guesture
        private val gestureMap: HashMap<String?, Pair<String, Int>?> = object : HashMap<String?, Pair<String, Int>?>() {
            init {
                put("Select a Gesture", Pair("SelectGesture", 1))
                put("Turn On Lights", Pair("LightOn", 1))
                put("Turn Off Lights", Pair("LightOff", 1))
                put("Turn On Fan", Pair("FanOn", 1))
                put("Turn Off Fan", Pair("FanOff", 1))
                put("Increase Fan Speed", Pair("FanUp", 1))
                put("Decrease Fan Speed", Pair("FanDown", 1))
                put("Set Thermostat to specified temperature", Pair("SetThermo", 1))
                put("0", Pair("Num0", 1))
                put("1", Pair("Num1", 1))
                put("2", Pair("Num2", 1))
                put("3", Pair("Num3", 1))
                put("4", Pair("Num4", 1))
                put("5", Pair("Num5", 1))
                put("6", Pair("Num6", 1))
                put("7", Pair("Num7", 1))
                put("8", Pair("Num8", 1))
                put("9", Pair("Num9", 1))
            }
        }
    }
}