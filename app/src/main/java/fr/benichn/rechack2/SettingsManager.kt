package fr.benichn.rechack2

import android.content.Context

class SettingsManager(context: Context) {
    val prefs = context.getSharedPreferences(prefs_filename, Context.MODE_PRIVATE)
    val editor = prefs.edit()
    fun get(name: String): String =
        prefs.getString(name, default_values.getOrDefault(name, null))
            ?: throw IllegalArgumentException("no default value found for setting : $name")

    fun set(name: String, value: String) {
        editor.putString(name, value)
        editor.commit()
    }

    companion object {
        val prefs_filename = "settings"
        val default_values: Map<String, String> = mapOf(
            "ua" to "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:108.0) Gecko/20100101 Firefox/108.0",
            "sshu" to "",
            "sshp" to "",
            "sshh" to "",
            "sshe" to ""
        )
    }
}