package fr.benichn.rechack2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import dalvik.system.DexClassLoader
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream

fun runCommandAsRoot(command: String) {
    val builder = ProcessBuilder("su", "-c", command)
    val process = builder.start()
    process.waitFor()
}

fun runSSHCommand(settings: SettingsManager, command: String): String {
    // Créez une nouvelle instance de JSch
    val jsch = JSch()

    // Ouvrez une session SSH
    val session = jsch.getSession(settings.get("sshu"), settings.get("sshh"), 22)
    session.setPassword(settings.get("sshp"))
    session.setConfig("StrictHostKeyChecking", "no")
    session.connect()

    val channel = session.openChannel("exec") as ChannelExec
    val os = ByteArrayOutputStream()
    channel.outputStream = os
    channel.setCommand(command)
    channel.connect()
    while (!channel.isClosed) {
        Thread.sleep(100);
    }
    channel.disconnect()
    session.disconnect()
    return os.toString()
}

fun openDownloader(context: Context, url: String, fileName: String, ua: String) {
    val aipp = context.packageManager.getApplicationInfo("com.tachibana.downloader", 0).sourceDir
    val aipcl =
        DexClassLoader(aipp, context.getDir("tmp", 0).absolutePath, null, context.classLoader)
    val aipc = aipcl.loadClass("com.tachibana.downloader.ui.adddownload.AddInitParams")
    val aip = aipc.newInstance()
    aipc.getField("url").set(aip, url)
    aipc.getField("fileName").set(aip, fileName)
    aipc.getField("userAgent").set(aip, ua)

    val webIntent = Intent(Intent.ACTION_SEND)
    webIntent.setClassName(
        "com.tachibana.downloader",
        "com.tachibana.downloader.ui.adddownload.AddDownloadActivity"
    )
    webIntent.putExtra("init_params", aip as Parcelable)
    context.startActivity(webIntent)
}

class MainFragment(val settings: SettingsManager): Fragment(R.layout.main_fragment_layout) {
    lateinit var textview: TextView
    lateinit var btn1: Button
    lateinit var btn1_1: Button
    lateinit var btn2: Button
    lateinit var btn3: ImageButton

    var urlstr = ""
    var isOperating = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textview = view.findViewById(R.id.textView)
        textview.addTextChangedListener {
            textview.post {
                val scrollAmount = textview.layout.getLineTop(textview.lineCount) - textview.height
                textview.scrollTo(0, 0.coerceAtLeast(scrollAmount))
            }
        }
        btn1 = view.findViewById(R.id.btn1)
        btn1_1 = view.findViewById(R.id.btn1_1)
        btn2 = view.findViewById(R.id.btn2)
        btn3 = view.findViewById(R.id.btn3)
        btn3.setOnClickListener {
            btn3.visibility = View.GONE
            operate()
        }
    }

    fun operate() {
        val ua = settings.get("ua")
        Thread {
            try {
                isOperating = true
                MainScope().launch { textview.text = resources.getString(R.string.wifi_off) }
                runCommandAsRoot("svc wifi disable")
                MainScope().launch {
                    textview.text = "${textview.text}\n" + resources.getString(R.string.airplane_on)
                }
                runCommandAsRoot("settings put global airplane_mode_on 1 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
                Thread.sleep(1000)
                MainScope().launch {
                    textview.text =
                        "${textview.text}\n" + resources.getString(R.string.airplane_off)
                }
                runCommandAsRoot("settings put global airplane_mode_on 0 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")
                Thread.sleep(5000)

                MainScope().launch {
                    textview.text = "${textview.text}\n" + resources.getString(R.string.ua_is, ua)
                }
                MainScope().launch {
                    textview.text =
                        "${textview.text}\n\n" + resources.getString(R.string.op1, urlstr)
                }
                val doc = Jsoup.connect(urlstr)
                    .header(
                        "User-Agent",
                        ua
                    ).get()
                val btn = doc.select("button#play_button")
                val tk = btn.attr("data-token")
                val id = btn.attr("data-video-id")
                val api_url = "https://recurbate.com/api/get.php?video=${id}&token=${tk}"
                MainScope().launch {
                    textview.text =
                        "${textview.text}\n" + resources.getString(R.string.op2, id, tk, api_url)
                }
                val doc2 = Jsoup.connect(api_url)
                    .header(
                        "User-Agent",
                        ua
                    ).get()
                val video_url = doc2.select("source").attr("src")
                if (video_url == "") {
                    MainScope().launch {
                        textview.text = "${textview.text}\n" + resources.getString(
                            R.string.obtained,
                            doc2.toString()
                        )
                        btn3.visibility = View.VISIBLE
                    }
                    return@Thread
                }

                val perfName = video_url.substringBeforeLast('/').substringAfterLast('/')
                val fileName = video_url.substringAfterLast('/').substringBefore('.')

                MainScope().launch {
                    textview.text =
                        "${textview.text}\n" + resources.getString(R.string.obtained, video_url)
                    btn1.isEnabled = requireContext().packageManager.getInstalledPackages(0)
                        .any { it.packageName == "com.tachibana.downloader" }
                    btn1_1.isEnabled = true
                    btn2.isEnabled = true
                    btn1.setOnClickListener {
                        openDownloader(requireContext(), video_url, fileName, ua)
                    }
                    btn1_1.setOnClickListener {
                        val uri = Uri.parse(video_url)
                        val webIntent = Intent(Intent.ACTION_VIEW)
                        webIntent.setDataAndTypeAndNormalize(uri, "text/plain")
                        startActivity(webIntent)
                    }
                    btn2.setOnClickListener {
                        val cmd =
                            "${settings.get("sshe")} '$perfName' '$fileName' '$video_url' '$ua'"
                        textview.text =
                            "${textview.text}\n\n" + resources.getString(R.string.run_ssh, cmd)
                        Thread {
                            val res = runSSHCommand(settings, cmd)
                            MainScope().launch {
                                textview.text = "${textview.text}\n" + resources.getString(
                                    R.string.obtained,
                                    res
                                )
                            }
                        }.start()
                    }
                }
            } finally {
                runCommandAsRoot("svc wifi enable")
                MainScope().launch {
                    textview.text = "${textview.text}\n\n" + resources.getString(R.string.wifi_on)
                }
                isOperating = false
            }
        }.start()
    }
}