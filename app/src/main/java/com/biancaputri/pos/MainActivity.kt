package com.biancaputri.pos

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val greetingText = findViewById<TextView>(R.id.greetingTextView)
        val dateText = findViewById<TextView>(R.id.dateTextView)

        // 🔥 Ambil waktu sekarang
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // 🔥 Logic greeting berdasarkan jam
        val greeting = when (hour) {
            in 0..10 -> getString(R.string.sapa_pagi)
            in 11..14 -> getString(R.string.sapa_siang)
            in 15..17 -> getString(R.string.sapa_sore)
            else -> getString(R.string.sapa_malam)
        }

        greetingText.text = greeting

        // 🔥 Format tanggal (Indonesia)
        val localeID = Locale("id", "ID")
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", localeID)
        val currentDate = dateFormat.format(Date())

        dateText.text = currentDate
    }
}