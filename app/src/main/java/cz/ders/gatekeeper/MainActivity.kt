package cz.ders.gatekeeper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreference = SharedPreference(context = applicationContext)

        when (sharedPreference.getValueString("theme")) {
            "AppTheme" -> {
                setTheme(R.style.AppTheme)
                setTheme(R.style.AppTheme_NoActionBar)
            }
            "AppTheme2" -> {
                setTheme(R.style.AppTheme2)
                setTheme(R.style.AppTheme2_NoActionBar)
            }
            "AppTheme3" -> {
                setTheme(R.style.AppTheme3)
                setTheme(R.style.AppTheme3_NoActionBar)
            }
            "AppTheme4" -> {
                setTheme(R.style.AppTheme4)
                setTheme(R.style.AppTheme4_NoActionBar)
            }
            else -> {
                setTheme(R.style.AppTheme)
                setTheme(R.style.AppTheme_NoActionBar)
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        supportActionBar?.title = getString(R.string.title)
//        supportActionBar?.setDisplayUseLogoEnabled(true)

        val gs = GateService()
        gs.openGate(0,findViewById(android.R.id.content), this)

        button_gate_one.setOnClickListener{
            gs.openGate(1, it, this)
            Snackbar.make(it,getString(R.string.call_gate_no)+"1",Snackbar.LENGTH_SHORT).show()
        }

        button_gate_two.setOnClickListener{
            gs.openGate(2, it, this)
            Snackbar.make(it,getString(R.string.call_gate_no)+"2",Snackbar.LENGTH_SHORT).show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this,
                    SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
