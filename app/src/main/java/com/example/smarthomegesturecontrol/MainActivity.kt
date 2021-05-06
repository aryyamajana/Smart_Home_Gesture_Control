package com.example.smarthomegesturecontrol

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//       Spinner Setup
        val gestureDropDown = findViewById<Spinner?>(R.id.GestureList)
        gestureDropDown.onItemSelectedListener = this
        val gestureAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.smart_home_gestures))
        gestureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gestureDropDown.adapter = gestureAdapter
    }

//       Move to PlayExpertVideo Screen once a gesture is selected from dropdown
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinnerItm = parent?.getItemAtPosition(position).toString()
        if (spinnerItm != "Select a Gesture") {
            val playExpertIntent = Intent(this@MainActivity, PlayExpertVideoActivity::class.java)
            playExpertIntent.putExtra("gesture_name", spinnerItm)
            startActivity(playExpertIntent)
        }
    }

    override fun onNothingSelected(arg0: AdapterView<*>?) {

    }
}