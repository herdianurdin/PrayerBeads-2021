package com.herdaynote.prayerbeads

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.media.SoundPool
import android.os.*
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView

class MainActivity : Activity() {
    private lateinit var sp: SharedPreferences
    companion object {
        const val COUNTER_DATA = "counter_data"
        const val SOUND_STATE = "sound_state"
        const val VIBRATE_STATE = "vibrate_state"
        const val LIMIT_DATA = "limit_data"
    }
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var soundPool: SoundPool
    private var soundSwitch1 = 0
    private var soundSwitch2 = 1
    private var counter = 0
    private var sound = false
    private var vibrate = false
    private var limit = 33

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        val view = View.inflate(this@MainActivity, R.layout.dialog_loading, null)
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.attributes?.windowAnimations = R.style.dialog_animation
        dialog.show()
        dialog.setCancelable(false)

        Handler(Looper.getMainLooper()).postDelayed({
            spInit()

            soundPool = SoundPool.Builder()
                .setMaxStreams(100)
                .build()
            soundSwitch1 = soundPool.load(this, R.raw.switch_1, 1)
            soundSwitch2 = soundPool.load(this, R.raw.switch_2, 1)

            sound()
            vibrate()
            refresh()
            counter()
            limit()

            Handler(Looper.myLooper()!!).postDelayed({
                dialog.dismiss()
            }, 1000)

            Handler(Looper.myLooper()!!).postDelayed({
                findViewById<CardView>(R.id.view_counter).visibility = View.VISIBLE
                findViewById<GridLayout>(R.id.tools).visibility = View.VISIBLE
            }, 1000)
        }, 300)
    }

    private fun spInit() {
        sp = getSharedPreferences(COUNTER_DATA, MODE_PRIVATE)
        counter = sp.getInt(COUNTER_DATA, 0)
        findViewById<TextView>(R.id.btn_counter).text = counter.toString()

        sp = getSharedPreferences(SOUND_STATE, MODE_PRIVATE)
        sound = !sp.getBoolean(SOUND_STATE, false)
        val btnSound: ImageView = findViewById(R.id.btn_sound)
        if (sound) {
            sound = false
            btnSound.setImageResource(R.drawable.ic_volume_off)
        } else {
            sound = true
            btnSound.setImageResource(R.drawable.ic_volume_on)
        }

        sp = getSharedPreferences(VIBRATE_STATE, MODE_PRIVATE)
        vibrate = !sp.getBoolean(VIBRATE_STATE, false)
        val btnVibrate: ImageView = findViewById(R.id.btn_vibrate)
        if (vibrate) {
            vibrate = false
            btnVibrate.setImageResource(R.drawable.ic_vibration_off)
        } else {
            vibrate = true
            btnVibrate.setImageResource(R.drawable.ic_vibration_on)
        }

        sp = getSharedPreferences(LIMIT_DATA, MODE_PRIVATE)
        limit = sp.getInt(LIMIT_DATA, 33)
    }

    private fun sound() {
        val btnSound: ImageView = findViewById(R.id.btn_sound)

        btnSound.setOnClickListener {
            if (sound) {
                sound = false
                btnSound.setImageResource(R.drawable.ic_volume_off)
                Toast.makeText(this, getString(R.string.sound_off), Toast.LENGTH_SHORT).show()
                soundPool.play(soundSwitch2, 1F,1F, 1, 0, 1F)
            } else {
                sound = true
                btnSound.setImageResource(R.drawable.ic_volume_on)
                Toast.makeText(this, getString(R.string.sound_on), Toast.LENGTH_SHORT).show()
                soundPool.play(soundSwitch2, 1F,1F, 1, 0, 1F)
            }

            sp = getSharedPreferences(SOUND_STATE, MODE_PRIVATE)
            editor = sp.edit()
            editor.putBoolean(SOUND_STATE, sound)
            editor.apply()
        }
    }

    private fun vibrate() {
        val btnVibrate: ImageView = findViewById(R.id.btn_vibrate)
        btnVibrate.setOnClickListener {
            if (vibrate) {
                vibrate = false
                btnVibrate.setImageResource(R.drawable.ic_vibration_off)
                Toast.makeText(this, getString(R.string.vibrate_off), Toast.LENGTH_SHORT).show()
                soundPool.play(soundSwitch2, 1F,1F, 1, 0, 1F)
            } else {
                vibrate = true
                btnVibrate.setImageResource(R.drawable.ic_vibration_on)
                Toast.makeText(this, getString(R.string.vibrate_on), Toast.LENGTH_SHORT).show()
                soundPool.play(soundSwitch2, 1F,1F, 1, 0, 1F)
                vibration()
            }

            sp = getSharedPreferences(VIBRATE_STATE, MODE_PRIVATE)
            editor = sp.edit()
            editor.putBoolean(VIBRATE_STATE, vibrate)
            editor.apply()
        }
    }

    private fun vibration(mill: Long = 50) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(mill, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(mill)
        }
    }

    private fun reset() {
        counter = 0

        sp = getSharedPreferences(COUNTER_DATA, MODE_PRIVATE)
        editor = sp.edit()
        editor.putInt(COUNTER_DATA, counter)
        editor.apply()

        findViewById<TextView>(R.id.btn_counter).text = counter.toString()
    }

    private fun refresh() {
        val btnRefresh: ImageView = findViewById(R.id.btn_refresh)
        btnRefresh.setOnClickListener {
            reset()

            soundPool.play(soundSwitch2, 1F,1F, 1, 0, 1F)

            findViewById<TextView>(R.id.btn_counter).text = counter.toString()
            Toast.makeText(this, getString(R.string.counter_reset), Toast.LENGTH_SHORT).show()
        }
    }

    private fun counter() {
        val btnCounter: TextView = findViewById(R.id.btn_counter)
        btnCounter.setOnClickListener {
            counter++

            if (counter <= limit) {
                btnCounter.text = counter.toString()
            }

            if (counter == limit) {
                btnCounter.isEnabled = false

                val view = View.inflate(this@MainActivity, R.layout.dialog_finish, null)
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setView(view)

                val dialog = builder.create()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.window?.attributes?.windowAnimations = R.style.dialog_animation
                dialog.show()
                dialog.setCancelable(false)

                view.findViewById<Button>(R.id.btn_done).setOnClickListener {
                    reset()

                    btnCounter.isEnabled = true

                    dialog.dismiss()
                }

                vibration(300)
            } else {
                sp = getSharedPreferences(COUNTER_DATA, MODE_PRIVATE)
                editor = sp.edit()
                editor.putInt(COUNTER_DATA, counter)
                editor.apply()
            }

            if (sound) {
                soundPool.play(soundSwitch1, 1F,1F, 1, 0, 1F)
            }

            if (vibrate) {
                vibration()
            }

//            if (adFailToLoad) {
//                adLoad()
//            }
        }
    }

    private fun limit() {
        val btnLimit: ImageView = findViewById(R.id.btn_limit)
        btnLimit.setOnClickListener {
            btnLimit.isEnabled = false

            soundPool.play(soundSwitch2, 1F,1F, 1, 0, 1F)

            val view = View.inflate(this@MainActivity, R.layout.dialog_set_limit, null)
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setView(view)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.attributes?.windowAnimations = R.style.dialog_animation
            dialog.show()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(true)

            val editLimit: EditText = dialog.findViewById(R.id.edit_limit)
            editLimit.text = Editable.Factory.getInstance().newEditable(limit.toString())
            editLimit.setSelection(editLimit.text.length)

            view.findViewById<Button>(R.id.btn_ok).setOnClickListener {
                if (editLimit.text.toString().trim().isNotEmpty()) {
                    when {
                        editLimit.text.toString().toInt() <= 0 -> {
                            editLimit.error = getString(R.string.not_zero)
                        }
                        editLimit.text.toString().toLong() > 9999999 -> {
                            editLimit.error = getString(R.string.max_limit)
                        }
                        else -> {
                            limit = editLimit.text.toString().toInt()

                            sp = getSharedPreferences(LIMIT_DATA, MODE_PRIVATE)
                            editor = sp.edit()
                            editor.putInt(LIMIT_DATA, limit)
                            editor.apply()

                            reset()

                            dialog.dismiss()

                            Toast.makeText(this, "${getString(R.string.limit_set)} : $limit", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    editLimit.error = getString(R.string.not_null)
                }
            }

            dialog.setOnDismissListener {
                btnLimit.isEnabled = true
            }
        }
    }
}