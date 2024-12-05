package com.example.eitg01_projekt_tcp

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import android.util.Log
import androidx.core.content.FileProvider
import java.io.OutputStreamWriter
import java.io.PrintWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.content.Intent

public class MainActivity : AppCompatActivity() {
    private var serverIp = "192.168.0.46"
    //private var serverIp = "172.20.10.4";
    private var portNmbr = 4000;
    private lateinit var socket:Socket
    private lateinit var inputstream:InputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var recievedData = findViewById<TextView>(R.id.recievedTxt)
        var serverIp = "192.168.0.46" //findViewById<TextView>(R.id.editServerName)
        var serverPortNmbr = findViewById<TextView>(R.id.editPortNmbr)
        val connectButton = findViewById<Button>(R.id.connectToServerButton)

        connectButton.setOnClickListener{
            Log.d("MainActivity", "Hej från Android!")
            Thread{
                try {
                    socket = Socket(serverIp,4000); //String för ip, int för portnmbr
                    val outputstream = socket.getOutputStream()
                    val printwriter = PrintWriter(outputstream,true)
                    printwriter.println("Android: Connection accepted") // Skicka till datorn
                    val buffer = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                    var recievedTxt = buffer.readLine()
                    Log.d("MainActivity", "tog emot $recievedTxt")
                    runOnUiThread{
                        recievedData.text = recievedTxt;

                    }
                    //Log.d("MainActivity", "Data received from server: $recievedTxt")
                    printwriter.println("Android: Recieved $recievedTxt")
                    if (recievedTxt.equals("Server: Connection confirmed.")) {
                        println("tja")
                        printwriter.println("ready for file transfer")
                        fileTransfer()
                    }


                    //printwriter.flush()
                    //buffer.close()
                    //printwriter.close()
                    //connection.close()


                } catch (e: Exception) {
                    Log.e("MainActivity","Connection failed",e)
                    runOnUiThread {
                        recievedData.text = "connection failed: ${e.message ?: "Unknown error"}"
                    }

                }
            }.start()
        }
    }

    private fun openDownloadedFile(path:String){
        val file = File(path)
        val uri: Uri = FileProvider.getUriForFile(this,"$packageName.fileprovider",file)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Hantera fallet där ingen PDF-läsare är installerad
            println("No PDF reader found")
        }
    }


    private fun downloadFile(serverIp:String,portNmbr:Int,path:String){
        try {
            val inputstream:InputStream = socket.getInputStream()
            val fileOutputStream = FileOutputStream(path)
            val buffer = ByteArray(4096)
            var bytesRead:Int

            while (inputstream.read(buffer).also { bytesRead = it } != -1) {
                fileOutputStream.write(buffer,0,bytesRead)
            }

        }
        catch (e:Exception){
            Log.e("MainActivity","downloadFile failed.",e)
        }

    }

    private fun fileTransfer() {
        val path = getExternalFilesDir(null)?.absolutePath+"/downloaded.pdf"
        CoroutineScope(Dispatchers.IO).launch{
            downloadFile(serverIp,portNmbr,path!!)
        }
    }
    fun onClickConnect(view: TextView){

    }
}