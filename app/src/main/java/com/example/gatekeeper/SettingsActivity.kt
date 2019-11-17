package com.example.gatekeeper

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val passPref = findPreference<EditTextPreference>("password")
            passPref?.summary = toStars(passPref?.text?:getString(R.string.not_set))
            passPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                println("Pref " + preference.key + " changed to " + newValue.toString())
                preference.summary = toStars(newValue.toString())
                true
            }

            val sharedPreference = SharedPreference(this.requireContext())
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
                when (s) {
                    "username" -> {
                        println(message = "Changed preferences: $s")
                        sharedPreference.removeValue("sessionid")
                    }
                    "password" -> {
                        println(message = "Changed preferences: $s")
                        sharedPreference.removeValue("sessionid")
                    }
                    else -> println(message = "Changed preferences: $s")
                }
            }
        }


        private fun toStars(text: String): String {
            val starText: String
            val sb = StringBuilder()
            for (i in text.indices) {
                sb.append('*')
            }
            starText = if(sb.toString().isEmpty()){
                getString(R.string.not_set)
            }else{
                sb.toString()
            }
            return starText
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            when{
                preference?.key.equals("clearSession") -> {

                    val builder = AlertDialog.Builder(this.requireContext())
                    builder.setTitle(getString(R.string.clearSession_title))
                    builder.setMessage(getString(R.string.clearSession_question))
                    builder.setPositiveButton(getString(R.string.yes)){dialog, which ->
                        val sharedPreference = SharedPreference(this.requireContext())
                        sharedPreference.removeValue("sessionid")
                        Snackbar.make(this.requireView(),getString(R.string.clearingSession), Snackbar.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton(getString(R.string.no)){dialog,which ->
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
            return super.onPreferenceTreeClick(preference)
        }


    }

}