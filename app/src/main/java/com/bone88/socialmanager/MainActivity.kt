package com.bone88.socialmanager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val bg = Color.rgb(9, 14, 27)
    private val surface = Color.rgb(18, 27, 47)
    private val surface2 = Color.rgb(25, 37, 62)
    private val cyan = Color.rgb(35, 211, 244)
    private val violet = Color.rgb(139, 111, 255)
    private val muted = Color.rgb(153, 169, 195)
    private val prefs by lazy { getSharedPreferences("bone88", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int = 18, stroke: Int = Color.TRANSPARENT) =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (stroke != Color.TRANSPARENT) setStroke(dp(1), stroke) }
    private fun label(value: String, size: Float = 15f, color: Int = Color.WHITE, bold: Boolean = false) =
        TextView(this).apply { text = value; textSize = size; setTextColor(color); if (bold) setTypeface(typeface, Typeface.BOLD); setPadding(dp(2), dp(3), dp(2), dp(3)) }
    private fun actionButton(value: String, primary: Boolean = false, action: () -> Unit) =
        TextView(this).apply {
            text = value; textSize = 15f; gravity = Gravity.CENTER
            setTextColor(if (primary) bg else cyan); setTypeface(typeface, Typeface.BOLD)
            background = rounded(if (primary) cyan else surface2, 14, if (primary) cyan else 0xff30415f.toInt())
            setPadding(dp(16), dp(14), dp(16), dp(14))
            isClickable = true; isFocusable = true; setOnClickListener { action() }
            val lp = LinearLayout.LayoutParams(-1, -2); lp.topMargin = dp(9); layoutParams = lp
        }
    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(14), dp(16), dp(14))
        background = rounded(surface, 18, 0xff263653.toInt())
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
    }
    private fun field(h: String) = EditText(this).apply {
        hint = h; textSize = 15f; setTextColor(Color.WHITE); setHintTextColor(muted)
        setPadding(dp(14), dp(12), dp(14), dp(12)); background = rounded(surface2, 12, 0xff354766.toInt())
    }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor = bg; window.navigationBarColor = bg; showHome() }
    private fun base(section: String) {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(22)); setBackgroundColor(bg) }
        setContentView(ScrollView(this).apply { clipToPadding = false; addView(root) })
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(4), 0, dp(20)) }
        top.addView(TextView(this).apply { text = "B88"; textSize = 19f; gravity = Gravity.CENTER; setTextColor(bg); setTypeface(typeface, Typeface.BOLD); background = rounded(cyan, 12); setPadding(dp(11), dp(8), dp(11), dp(8)) })
        val names = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(11), 0, 0, 0) }
        names.addView(label("BONE88", 17f, Color.WHITE, true)); names.addView(label("SOCIAL MANAGER", 10f, muted, true)); top.addView(names)
        top.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f)); top.addView(label("● ONLINE", 10f, cyan, true)); root.addView(top)
        root.addView(label(section, 12f, violet, true))
        root.addView(View(this).apply { setBackgroundColor(0xff263653.toInt()); layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(9); bottomMargin = dp(18) } })
    }
    private fun items(): JSONArray = try { JSONArray(prefs.getString("items", "[]")) } catch (_: Exception) { JSONArray() }
    private fun save(a: JSONArray) { prefs.edit().putString("items", a.toString()).apply() }
    private fun channelLine(name: String, detail: String, color: Int): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(5)) }
        row.addView(label("●  " + name, 13f, color, true)); row.addView(label(detail, 12f, muted)); return row
    }
    private fun showHome() {
        val all = items(); var drafts = 0
        for (i in 0 until all.length()) if (all.optJSONObject(i)?.optString("status") != "Publicado") drafts++
        base("PANEL DE CONTROL")
        val hero = card(); hero.addView(label("TU CONTENIDO.\nTUS REGLAS.", 25f, Color.WHITE, true))
        hero.addView(label("Organiza, revisa y aprueba antes de publicar.", 14f, muted))
        hero.addView(actionButton("＋  CREAR PUBLICACIÓN", true) { showCreate() }); root.addView(hero)
        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val c1 = card().apply { layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(6) } }
        c1.addView(label("EN COLA", 10f, muted, true)); c1.addView(label(drafts.toString(), 25f, cyan, true))
        val c2 = card().apply { layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(6) } }
        c2.addView(label("GUARDADAS", 10f, muted, true)); c2.addView(label(all.length().toString(), 25f, violet, true))
        stats.addView(c1); stats.addView(c2); root.addView(stats)
        val q = card(); q.addView(label("GESTIÓN DE CONTENIDO", 15f, Color.WHITE, true))
        q.addView(label("Revisa tus borradores y controla cada envío.", 13f, muted))
        q.addView(actionButton("ABRIR PUBLICACIONES   →") { showQueue() }); root.addView(q)
        val channels = card(); channels.addView(label("CANALES CONECTADOS", 15f, Color.WHITE, true))
        channels.addView(channelLine("TELEGRAM", "Envío mediante tu Worker", cyan))
        channels.addView(channelLine("TIKTOK", "Integración pendiente", muted))
        channels.addView(channelLine("INSTAGRAM", "Integración pendiente", muted))
        channels.addView(channelLine("YOUTUBE", "Integración pendiente", muted)); root.addView(channels)
        val settings = card(); settings.addView(label("CONFIGURACIÓN", 15f, Color.WHITE, true))
        settings.addView(label("Conecta Telegram y revisa la información de privacidad.", 13f, muted))
        settings.addView(actionButton("AJUSTES Y CONEXIONES") { showSettings() })
        settings.addView(actionButton("PRIVACIDAD Y DATOS") { showPrivacy() }); root.addView(settings)
        root.addView(label("BONE88 SOCIAL MANAGER  •  VERSIÓN 1.1", 10f, muted).apply { gravity = Gravity.CENTER })
    }
    private fun showCreate() {
        base("NUEVA PUBLICACIÓN")
        val panel = card(); panel.addView(label("PREPARA TU CONTENIDO", 16f, Color.WHITE, true))
        panel.addView(label("Escribe el mensaje y selecciona el canal de destino.", 13f, muted))
        val body = field("Escribe aquí tu publicación").apply { minLines = 5; gravity = Gravity.TOP }; panel.addView(body)
        panel.addView(label("CANAL", 11f, muted, true))
        val channel = Spinner(this); channel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Telegram", "TikTok", "Instagram", "YouTube")); panel.addView(channel)
        panel.addView(actionButton("GUARDAR BORRADOR", true) {
            val value = body.text.toString().trim()
            if (value.isEmpty()) { toast("Escribe algo primero"); return@actionButton }
            val a = items(); a.put(JSONObject().put("text", value).put("channel", channel.selectedItem.toString()).put("status", "Borrador"))
            save(a); toast("Borrador guardado"); showQueue()
        })
        root.addView(panel); root.addView(actionButton("←  VOLVER AL PANEL") { showHome() })
    }
    private fun showQueue() {
        base("TUS PUBLICACIONES"); val a = items()
        if (a.length() == 0) { val empty = card(); empty.addView(label("AÚN NO HAY PUBLICACIONES", 15f, Color.WHITE, true)); empty.addView(label("Crea tu primer borrador para empezar.", 13f, muted)); root.addView(empty) }
        for (i in a.length() - 1 downTo 0) {
            val obj = a.getJSONObject(i); val index = i; val panel = card()
            panel.addView(label(obj.optString("channel").uppercase(), 11f, cyan, true))
            panel.addView(label(obj.optString("status"), 12f, violet, true)); panel.addView(label(obj.optString("text"), 14f, Color.WHITE))
            if (obj.optString("status") != "Publicado") {
                panel.addView(actionButton("APROBAR Y ENVIAR", true) {
                    if (obj.optString("channel") == "Telegram") sendTelegram(obj.optString("text"), index)
                    else { obj.put("status", "Aprobado (pendiente de API oficial)"); save(a); toast("Aprobado. Falta conectar la API oficial."); showQueue() }
                })
                panel.addView(actionButton("ELIMINAR BORRADOR") { a.remove(index); save(a); showQueue() })
            }
            root.addView(panel)
        }
        root.addView(actionButton("←  VOLVER AL PANEL") { showHome() })
    }
    private fun sendTelegram(message: String, index: Int) {
        val endpoint = prefs.getString("worker", "")!!.trim()
        if (endpoint.isEmpty()) { toast("Configura primero la URL del Worker"); showSettings(); return }
        thread {
            try {
                val conn = URL(endpoint.trimEnd('/') + "/telegram/send").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"; conn.connectTimeout = 15000; conn.readTimeout = 15000; conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(JSONObject().put("text", message).toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val response = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
                runOnUiThread {
                    if (code in 200..299) {
                        val data = items(); if (index < data.length()) { data.getJSONObject(index).put("status", "Publicado"); save(data) }
                        toast("Enviado a Telegram"); showQueue()
                    } else toast("Telegram respondió HTTP " + code + ": " + response)
                }; conn.disconnect()
            } catch (e: Exception) { runOnUiThread { toast("No se pudo conectar: " + e.message) } }
        }
    }
    private fun showSettings() {
        base("AJUSTES Y CONEXIONES"); val panel = card()
        panel.addView(label("TELEGRAM + CLOUDFLARE", 15f, Color.WHITE, true))
        panel.addView(label("Introduce la dirección base de tu Worker. La aplicación añadirá /telegram/send.", 13f, muted))
        val url = field("https://tu-worker.tu-cuenta.workers.dev"); url.setText(prefs.getString("worker", "")); panel.addView(url)
        panel.addView(actionButton("GUARDAR DIRECCIÓN", true) { prefs.edit().putString("worker", url.text.toString().trim()).apply(); toast("Dirección guardada"); showHome() })
        root.addView(panel); root.addView(actionButton("←  VOLVER AL PANEL") { showHome() })
    }
    private fun showPrivacy() {
        base("PRIVACIDAD Y DATOS"); val panel = card()
        panel.addView(label("QUÉ GUARDA ESTA VERSIÓN", 15f, Color.WHITE, true))
        panel.addView(label("Los borradores y la dirección del Worker se guardan localmente en este dispositivo. Al aprobar un mensaje de Telegram, el texto se envía a la dirección de Cloudflare Worker que hayas configurado para que pueda entregarlo a Telegram.", 13f, muted))
        panel.addView(label("La conexión con servicios externos puede implicar el tratamiento de datos por esos proveedores. No introduzcas contraseñas ni datos sensibles en tus publicaciones.", 13f, muted))
        panel.addView(label("Antes de publicar en Google Play habrá que completar y alojar una política de privacidad pública que describa exactamente el funcionamiento final de la app.", 13f, cyan))
        root.addView(panel); root.addView(actionButton("←  VOLVER AL PANEL") { showHome() })
    }
    private fun toast(message: String) { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
}
