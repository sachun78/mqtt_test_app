package com.example.mqtt

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var client: Mqtt5AsyncClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            val client: Mqtt5AsyncClient = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .addConnectedListener {
                    println("addConnectedListener !!!")
                }
                .addDisconnectedListener {
                    println("=== addDisconnectedListener !!!")
                }
                .buildAsync()

            client.connectWith()
                .send()
                .whenComplete { t, u ->
                    if (null != u) {
                        println("connect fail")
                    }
                    else {
                        println("connect success")
                    }
                }

            client.subscribeWith()
                .topicFilter("the/topic/#")
                .callback { publish: Mqtt5Publish? ->
                    println("payload: ${publish?.payloadAsBytes?.let { String(it) }}")
                    println("topic: ${publish?.topic}, usrprop: ${publish?.userProperties}")
                    println("qoc: ${publish?.qos}, type: ${publish?.contentType}")
                    var str_result: String = findViewById<TextView>(R.id.tv_result).text.toString()
                    str_result +=
                        ("payload: ${publish?.payloadAsBytes?.let { String(it) }} \n" +
                        "topic: ${publish?.topic}, usrprop: ${publish?.userProperties} \n" +
                        "qoc: ${publish?.qos}, type: ${publish?.contentType} \n\n")
                    findViewById<TextView>(R.id.tv_result).text = str_result
                }
                .send()
                .whenComplete { subAck: Mqtt5SubAck?, throwable: Throwable? ->
                    if (throwable != null) {
                        println("Handle failure to subscribe")
                    } else {
                        println("subscribe success")
                    }
                }

            this.client = client
        }


        findViewById<Button>(R.id.btn_publish).setOnClickListener {
            var str_publish = findViewById<EditText>(R.id.et_publish).text.toString()
            var str_payload = findViewById<EditText>(R.id.et_payload).text.toString()

            client.publishWith()
                .topic(str_publish)
                .payload(str_payload.toByteArray())
                .send()
                .whenComplete { t, u ->
                    if (u != null) {
                        println("publish fail")
                    } else {
                        println("publish success")
                    }
                }
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            client.unsubscribeWith()
                .topicFilter("the/topic/#")
                .send()
                .whenComplete { t, u ->
                    if (u != null) {
                        println("unsubscribe fail")
                    } else {
                        println("unsubscribe success")
                    }
                }
            Thread.sleep(1000)
            client.disconnect()
            findViewById<TextView>(R.id.tv_result).text = ""
            findViewById<EditText>(R.id.et_publish).text.clear()
            findViewById<EditText>(R.id.et_payload).text.clear()
        }
    }
}