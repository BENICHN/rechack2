package fr.benichn.rechack2

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class SetterFragment(val settings: SettingsManager, val name: String, val title: String): Fragment(R.layout.setter_fragment_layout) {
    private lateinit var tb: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val titlev = view.findViewById<TextView>(R.id.title)
        tb = view.findViewById(R.id.tb)
        titlev.text = title
        tb.setText(settings.get(name), TextView.BufferType.EDITABLE)
    }

    fun save() {
        settings.set(name, tb.text.toString())
    }
}