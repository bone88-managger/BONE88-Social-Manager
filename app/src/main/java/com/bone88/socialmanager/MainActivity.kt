package com.bone88.socialmanager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("bone88", MODE_PRIVATE) }
    private val gold = Color.rgb(212, 175, 55)
    private val dark = Color.rgb(18, 18, 22)
    private lateinit var root: LinearLayout
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun text(s:String,size:Float=16f)=TextView(this).apply { text=s; textSize=size; setTextColor(Color.WHITE); setPadding(dp(8),dp(8),dp(8),dp(8)) }
    private fun button(s:String, action:()->Unit)=Button(this).apply { text=s; setOnClickListener { action() } }
    private fun field(hint:String)=EditText(this).apply { this.hint=hint; setTextColor(Color.WHITE); setHintTextColor(0xffaaaaaa.toInt()); setPadding(dp(12),dp(10),dp(12),dp(10)) }
    override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); showHome() }
    private fun base(title:String) {
        root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(16),dp(16),dp(16),dp(16)); setBackgroundColor(dark) }
        root.addView(text(title,25f).apply { setTextColor(gold) })
        val scroll=ScrollView(this); scroll.addView(root); setContentView(scroll)
    }
    private fun showHome() {
        base("BONE88  |  SOCIAL MANAGER")
        root.addView(text("Tu centro de publicaciones",17f))
        root.addView(button("＋  Crear publicación") { showCreate() })
        root.addView(button("▣  Borradores y cola") { showQueue() })
        root.addView(button("✈  Configurar Telegram") { showSettings() })
        root.addView(text("REDES",13f).apply { setTextColor(gold) })
        listOf("Telegram  •  Envío mediante Cloudflare Worker","TikTok  •  Pendiente de API oficial","Instagram  •  Pendiente de API oficial","YouTube  •  Pendiente de API oficial").forEach { root.addView(text(it,14f)) }
        root.addView(text("Todas las publicaciones requieren tu aprobación manual.",13f))
    }
    private fun items():JSONArray = try { JSONArray(prefs.getString("items","[]")) } catch(e:Exception){ JSONArray() }
    private fun save(a:JSONArray) { prefs.edit().putString("items",a.toString()).apply() }
    private fun showCreate() {
        base("Nueva publicación")
        val body=field("Escribe el texto de tu publicación"); body.minLines=5; body.gravity=Gravity.TOP
        root.addView(body)
        val channel=Spinner(this); channel.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("Telegram","TikTok","Instagram","YouTube")); root.addView(channel)
        root.addView(button("Guardar como borrador") {
            val value=body.text.toString().trim()
            if(value.isEmpty()) { toast("Escribe algo primero"); return@button }
            val a=items(); a.put(JSONObject().put("text",value).put("channel",channel.selectedItem.toString()).put("status","Borrador"))
            save(a); toast("Borrador guardado"); showQueue()
        })
        root.addView(button("Volver") { showHome() })
    }
    private fun showQueue() {
        base("Borradores y publicaciones")
        val a=items()
        if(a.length()==0) root.addView(text("Todavía no tienes publicaciones."))
        for(i in a.length()-1 downTo 0) {
            val obj=a.getJSONObject(i); val index=i
            root.addView(text("${obj.optString("channel")}  •  ${obj.optString("status")}",14f).apply { setTextColor(gold) })
            root.addView(text(obj.optString("text"),15f))
            if(obj.optString("status")!="Publicado") {
                root.addView(button("Aprobar y enviar") {
                    if(obj.optString("channel")=="Telegram") sendTelegram(obj.optString("text"),index)
                    else { obj.put("status","Aprobado (pendiente de API oficial)"); save(a); toast("Aprobado. Falta conectar la API oficial."); showQueue() }
                })
                root.addView(button("Eliminar") { a.remove(index); save(a); showQueue() })
            }
            root.addView(View(this).apply { setBackgroundColor(0xff444444.toInt()); layoutParams=LinearLayout.LayoutParams(-1,dp(1)) })
        }
        root.addView(button("Volver") { showHome() })
    }
    private fun sendTelegram(message:String,index:Int) {
        val endpoint=prefs.getString("worker","")!!.trim()
        if(endpoint.isEmpty()) { toast("Configura primero la URL del Worker"); showSettings(); return }
        thread {
            try {
                val conn=URL(endpoint.trimEnd('/')+"/telegram/send").openConnection() as HttpURLConnection
                conn.requestMethod="POST"; conn.connectTimeout=15000; conn.readTimeout=15000; conn.doOutput=true
                conn.setRequestProperty("Content-Type","application/json")
                conn.outputStream.use { it.write(JSONObject().put("text",message).toString().toByteArray(Charsets.UTF_8)) }
                val code=conn.responseCode
                val response=(if(code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() } ?: ""
                runOnUiThread {
                    if(code in 200..299) { val data=items(); data.getJSONObject(index).put("status","Publicado"); save(data); toast("Enviado a Telegram"); showQueue() }
                    else toast("Telegram respondió HTTP $code: $response")
                }
                conn.disconnect()
            } catch(e:Exception) { runOnUiThread { toast("No se pudo conectar: ${e.message}") } }
        }
    }
    private fun showSettings() {
        base("Conexión Telegram")
        root.addView(text("Pega la dirección base de tu Cloudflare Worker. La app añadirá /telegram/send.",14f))
        val url=field("https://tu-worker.tu-cuenta.workers.dev"); url.setText(prefs.getString("worker",""))
        root.addView(url)
        root.addView(button("Guardar conexión") { prefs.edit().putString("worker",url.text.toString().trim()).apply(); toast("Dirección guardada"); showHome() })
        root.addView(button("Volver") { showHome() })
    }
    private fun toast(s:String) { Toast.makeText(this,s,Toast.LENGTH_LONG).show() }
}
