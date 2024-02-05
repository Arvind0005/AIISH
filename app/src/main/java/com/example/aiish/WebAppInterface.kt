import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebAppInterface(private val context: Context) {

    // This method can be called from JavaScript
       var response="";
    @JavascriptInterface
    fun sendDataToAndroid(data: String) {
        println("heeeeeeeeeeeeeeeeeeloooo---1111111")
        val intent = Intent("session_message")
        intent.putExtra("session_message", "ready")
        context.sendBroadcast(intent);
        response=data;
    }
    @JavascriptInterface
    fun missingAnimations(word: String) {
        println("heeeeeeeeeeeeeeeeeeloooo-222")
        val intent = Intent("missingAnimations")
        intent.putExtra("missingAnimations", "ready")
        context.sendBroadcast(intent);
        response=word;
    }
}
