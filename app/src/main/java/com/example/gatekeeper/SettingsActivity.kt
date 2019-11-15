package com.example.gatekeeper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            when{
                preference?.key.equals("clearSession") -> {
                    val sharedPreference = SharedPreference(this.requireContext())
                    sharedPreference.clearSharedPreference()
                    Snackbar.make(this.requireView(),"Clear session ...", Snackbar.LENGTH_SHORT).show()
                }
            }
            return super.onPreferenceTreeClick(preference)
        }
    }
}