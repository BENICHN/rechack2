package fr.benichn.rechack2

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class SettingsFragment(val settings: SettingsManager): Fragment(R.layout.settings_fragment_layout) {
    val setters = listOf(
        SetterFragment(settings, "ua", "user-agent"),
        SetterFragment(settings, "sshh", "ssh host"),
        SetterFragment(settings, "sshu", "ssh username"),
        SetterFragment(settings, "sshp", "ssh password"),
        SetterFragment(settings, "sshe", "dl executable path")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.commit {
            add(R.id.sc_ua, setters[0])
            add(R.id.sc_sshh, setters[1])
            add(R.id.sc_sshu, setters[2])
            add(R.id.sc_sshp, setters[3])
            add(R.id.sc_sshe, setters[4])
        }
    }

    fun save() {
        for (s in setters) s.save()
    }
}