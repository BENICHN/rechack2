package fr.benichn.rechack2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {
    private lateinit var settings: SettingsManager
    private lateinit var mainFragmentContainer: FragmentContainerView
    private lateinit var settingsFragmentContainer: FragmentContainerView
    private lateinit var mainFragment: MainFragment
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
        setContentView(R.layout.activity_main)
        settings = SettingsManager(this)
        mainFragment = MainFragment(settings)
        settingsFragment = SettingsFragment(settings)
        mainFragmentContainer = findViewById(R.id.frag_cont)
        settingsFragmentContainer = findViewById(R.id.frag_cont_stgs)
        supportFragmentManager.commit {
            add(R.id.frag_cont, mainFragment)
            add(R.id.frag_cont_stgs, settingsFragment)
        }
        val btn3 = findViewById<ImageButton>(R.id.btn3)
        val btn4 = findViewById<ImageButton>(R.id.btn4)
        val btn5 = findViewById<ImageButton>(R.id.btn5)
        btn3.setOnClickListener {
            mainFragmentContainer.visibility = View.GONE
            settingsFragmentContainer.visibility = View.VISIBLE
            btn3.visibility = View.GONE
            btn4.visibility = View.VISIBLE
            btn5.visibility = View.VISIBLE
        }
        btn3.setOnLongClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("envoyer 'killall ffmpeg' au serveur ?")
            builder.setPositiveButton("oui"){ _, _ ->
                Thread { runSSHCommand(settings, "killall ffmpeg") }.start()
            }

            builder.setNegativeButton("non") { _, _ -> }
            builder.create().show()
            return@setOnLongClickListener true
        }
        btn4.setOnClickListener {
            mainFragmentContainer.visibility = View.VISIBLE
            settingsFragmentContainer.visibility = View.GONE
            btn3.visibility = View.VISIBLE
            btn4.visibility = View.GONE
            btn5.visibility = View.GONE
        }
        btn5.setOnClickListener {
            settingsFragment.save()
            mainFragmentContainer.visibility = View.VISIBLE
            settingsFragmentContainer.visibility = View.GONE
            btn3.visibility = View.VISIBLE
            btn4.visibility = View.GONE
            btn5.visibility = View.GONE
        }
        val urlstr = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (urlstr == null) return
        else {
            mainFragment.urlstr = urlstr
            mainFragment.operate()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mainFragment.isOperating){
            runCommandAsRoot("settings put global airplane_mode_on 0 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")
            runCommandAsRoot("svc wifi enable")
        }
    }
}