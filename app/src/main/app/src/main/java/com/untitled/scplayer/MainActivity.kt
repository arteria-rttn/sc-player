package com.untitled.scplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val clientIds = listOf(
        "iZIs9mchVcX5lhVRyQGGAYlNPVldzAoX",
        "fDoItMDbsbZz8dY16ZzARCZmzgHBPotA",
        "2t9loNQH90kzJcsFCODdigxfp325aq4z"
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(SoundCloudBridge(), "NativeSC")
        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class SoundCloudBridge {
        @JavascriptInterface
        fun searchTracks(query: String): String {
            val encoded = URLEncoder.encode(query, "UTF-8")
            for (cid in clientIds) {
                try {
                    val url = "https://api-v2.soundcloud.com/search/tracks?q=$encoded&client_id=$cid&limit=25"
                    val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
                    client.newCall(req).execute().use { res ->
                        if (res.isSuccessful) {
                            val body = res.body?.string() ?: ""
                            if (body.contains("collection")) {
                                val json = JSONObject(body)
                                json.put("used_client_id", cid)
                                return json.toString()
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
            return "{\"error\": \"Не удалось подключиться\"}"
        }

        @JavascriptInterface
        fun getDirectStream(transcodingUrl: String, clientId: String): String {
            try {
                val url = "$transcodingUrl?client_id=$clientId"
                val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
                client.newCall(req).execute().use { res ->
                    if (res.isSuccessful) return res.body?.string() ?: "{}"
                }
            } catch (_: Exception) {}
            return "{}"
        }
    }
}
