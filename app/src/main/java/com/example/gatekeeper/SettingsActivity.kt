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
                //println( "Pref " + preference.key + " changed to " + newValue.toString())
                preference.summary = toStars(newValue.toString())
                true
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
                    builder.setTitle("Clear session")
                    builder.setMessage("Are you want to clear current session?")
                    builder.setPositiveButton("Yes"){dialog, which ->
                        val sharedPreference = SharedPreference(this.requireContext())
                        sharedPreference.clearSharedPreference()
                        Snackbar.make(this.requireView(),"Clear session ...", Snackbar.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No"){dialog,which ->
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
            return super.onPreferenceTreeClick(preference)
        }


    }

}