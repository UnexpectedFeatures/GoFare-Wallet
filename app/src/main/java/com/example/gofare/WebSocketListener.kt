package com.example.gofare

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.stripe.android.paymentsheet.PaymentSheet
import org.json.JSONObject

class WebsocketConnection(
    private val activity: FragmentActivity,
    private val paymentSheet: PaymentSheet
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Connected")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (!text.startsWith("[NOTIFY]")) {
            try {
                val json = JSONObject(text)
                val clientSecret = json.getString("clientSecret")

                val configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "GoFare Top-Up",
                    allowsDelayedPaymentMethods = true
                )

                activity.runOnUiThread {
                    paymentSheet.presentWithPaymentIntent(clientSecret, configuration)
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Failed to parse WebSocket message: ${e.message}")
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("WebSocket", "Received bytes: $bytes")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        Log.d("WebSocket", "Closing: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "Error: " + t.message)
    }
}
