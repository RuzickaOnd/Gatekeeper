package cz.ders.gatekeeper

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreference =
            SharedPreference(context = applicationContext)
        when (sharedPreference.getValueString("theme")) {
            "AppTheme" -> setTheme(R.style.AppTheme)
            "AppTheme2" -> setTheme(R.style.AppTheme2)
            "AppTheme3" -> setTheme(R.style.AppTheme3)
            else -> {
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()

/*
        val chbPassShow = findViewById<CheckBox>(R.id.checkbox)
        chbPassShow.setOnCheckedChangeListener { compoundButton, b ->
            if(b){
                //edit.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                println(message = "checkbox: $b")
            }else{
                //edit.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                println(message = "checkbox: $b")
            }
        }
*/
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

            val themePref = findPreference<ListPreference>("theme")
            themePref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                println("Pref " + preference.key + " changed to " + newValue.toString())
                val sharedPreference =
                    SharedPreference(requireContext())
                sharedPreference.save("theme",newValue.toString())
                activity?.recreate()

                true
            }

            val sharedPreference =
                SharedPreference(this.requireContext())
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
                println(message = "Changed preferences: $s")
                when (s) {
                    "username" -> {
                        sharedPreference.removeValue("sessionid")
                    }
                    "password" -> {
                        sharedPreference.removeValue("sessionid")
                    }
                    else -> {
                    }
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
                    builder.setPositiveButton(getString(R.string.yes)){ dialog, which ->
                        val sharedPreference =
                            SharedPreference(this.requireContext())
                        sharedPreference.removeValue("sessionid")
                        Snackbar.make(this.requireView(),getString(R.string.clearingSession), Snackbar.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton(getString(R.string.no)){ dialog, which ->
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
            return super.onPreferenceTreeClick(preference)
        }


    }

}