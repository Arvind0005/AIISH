package com.example.aiish

import WebAppInterface
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
//import com.google.android.material.navigation.NavigationView
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.aiish.ui.theme.AIISHTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import java.io.IOException
import java.util.Locale

class MainActivity : ComponentActivity() {
    var language: String = "en-US"
    var sourcelanguage = "TAMIL"
    var selectedLanguage = "Choose language";
    private lateinit var webView: WebView
    private var progressDialog: ProgressDialog? = null
    var ReadyFlag=true;
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    private lateinit var interpretButton: Button
    private val maxWordCount = 100
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private var remainingWordCount = 10
    private var Transcribetext = "";
    private var currentWordCount = 0

    var options = TranslatorOptions.Builder()
    .setSourceLanguage(TranslateLanguage.TELUGU)
    .setTargetLanguage(TranslateLanguage.ENGLISH)
    .build()
    var englishGermanTranslator = Translation.getClient(options)
    val profanityList = Globals.profanityList

    private var mRequestQueue: RequestQueue? = null
    private var mStringRequest: StringRequest? = null
    lateinit var mGoogleSignInClient: GoogleSignInClient



    private fun getProfilePicUri(): String? {
    //    Bugfender.d("MainActivity","getProfilePicUri");
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    //    Bugfender.d("MainActivity",sharedPreferences.getString("PROFILE_PIC_URI", null));
        return sharedPreferences.getString("PROFILE_PIC_URI", null)
    }

    fun openDrawer() {
    //    Bugfender.d("MainActivity","openDrawer");
        val drawerLayout = findViewById<DrawerLayout>(R.id.my_drawer_layout)
        drawerLayout.openDrawer(GravityCompat.END)
    }
    private fun recogniserImage(myId: Int, imageUri: Uri?) {
//        Bugfender.d("MainActivity","recogniserImage");
//        Bugfender.d("recogniserImage",imageUri.toString());
        try {
            //  Bugfender.d("recogniserImage","Try");
//            val inputImage = imageUri?.let { InputImage.fromFilePath(this, it) }
//            //  progress.setMessage("Text...")
//
//            val textTaskResult = inputImage?.let {
//                textRecognizer.process(it)
//                    .addOnSuccessListener { text ->
//                        //        progress.dismiss()
//                        val resultText = text.text
//                        println("tttttttttttttttexxxxxxxxxxxxxxxxxxxxxt");
//                        println(resultText);
//                        //    recognisedText.text = resultText;
//                        showPopupWithEditText(resultText,"Scanned Text");
//                        // Do something with the recognized text (resultText)
//                    }
            //      }
        } catch (e: IOException) {
//            Bugfender.d("recogniserImage","catch");
//            Bugfender.e("recogniserImage",e.toString());
//            Bugfender.d("recogniserImage","Exception");
            Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun sessionAlert(context: Context) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.session_timeout_alert, null)

        val alertDialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        // Set click listener for the "OK" button
        view.findViewById<Button>(R.id.okButton).setOnClickListener {
          //  Bugfender.d("showLogoutAlert","Yes");
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, Signin_page::class.java)
                Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
                startActivity(intent)
                finish()
            }
            alertDialog.dismiss()
            // Add any logic you need to perform when the user clicks "OK"
        }

        alertDialog.show()
    }

    private fun getData(url: String) {
        println("yessssssssssssssssssssssssssssjbhvbdhbvhjbjdbhfg")

        // RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this)

        // String Request initialized
        mStringRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                // This code will be executed upon a successful response
                println("response got from server")
                println(response)
               // Bugfender.d("getData","respoonse from Server: $response");
            },
            { error ->
                println(error)
//                if(error.networkResponse.statusCode!=null) {
//                    if (error!!.networkResponse!!.statusCode.toString() == "401") {
//                        sessionAlert(this@MainActivity);
//                    }
//                }
//                Log.i(ContentValues.TAG, "Error: ${error.networkResponse.statusCode}")
            //    Bugfender.d("getData","error from Server: $error");
            }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                val statusCode = response.statusCode
                println("resssssssssssssssssssponssssssssssssseeeeeee code");
                println("Response Code: $statusCode")
                if(statusCode.toString()=="401")
                {
                    sessionAlert(this@MainActivity);
                }
                return super.parseNetworkResponse(response)
            }
        }

        mRequestQueue!!.add(mStringRequest)
    }


    fun sanitizeText(text: String): String {
//            Bugfender.d("MainActivity","sanitizeText");
//            Bugfender.d("sanitizeText",text);
        var text0 = text.lowercase(Locale.ROOT)
        var sanitizedText = text0
        for (word in profanityList) {
            val replacement = "*".repeat(word.length)
            sanitizedText = sanitizedText.replace(Regex("(?i)\\b$word\\b"), replacement)
            sanitizedText = sanitizedText.replace(Regex("(?i)\\n$word\\n"), replacement)
        }
        // Bugfender.d("sanitizeText",sanitizedText);
        return sanitizedText
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)
        val scale = resources.displayMetrics.density
        var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)
        val scanButton = findViewById<ImageView>(R.id.webview_scan_ic);
        val languages = arrayOf(
            "English",
            "Tamil",
            "Telugu",
            "Kannada",
            "Hindi",
            "Gujarati",
            "Marathi",
            "Bengali"
        )
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1056955407882-m2fv7ko571ndsu9bsh2irnbnb6354gb1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // Create an ArrayAdapter using the string array and a default spinner layout
        val sadapter = ArrayAdapter(this, R.layout.spinner_iteam, languages);
        var sortedAppInfos = mutableListOf<AppInfo>()
        // Specify the layout to use when the list of choices appears
        val adapter = MyArrayAdapter(this, R.id.iteamlistview, sortedAppInfos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        languageSpinner.adapter = sadapter


        // Set up the OnItemSelectedListener
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Get the selected item from the spinner
                selectedLanguage = languages[position]

//                val englishGermanTranslator = Translation.getClient(options)
                if (selectedLanguage != "Choose language")
                    Toast.makeText(
                        this@MainActivity,
                        "Selected Language: $selectedLanguage",
                        Toast.LENGTH_SHORT
                    ).show();

//                if(selectedLanguage=="English") {
//                    language = "en-us";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.ENGLISH)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }
//                else if(selectedLanguage=="Tamil") {
//                    language = "ta-IN";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.TAMIL)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }
//                else if(selectedLanguage=="Telugu") {
//                    language = "te-IN";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.TELUGU)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }
//                else if(selectedLanguage=="Kannada") {
//                    language = "kn-IN";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.KANNADA)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }
//                else if(selectedLanguage=="Malayalam") {
//                    language = "ml-IN";
////                    options = TranslatorOptions.Builder()
////                        .setSourceLanguage(TranslateLanguage.M)
////                        .setTargetLanguage(TranslateLanguage.ENGLISH)
////                        .build()
//                }
//                else if(selectedLanguage=="Hindi") {
//                    language = "hi-IN";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.HINDI)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }
//                else if(selectedLanguage=="Gujarati") {
//                    language = "gu-IN";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.GUJARATI)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }
//                else if(selectedLanguage=="Marathi") {
//                    language = "mr-IN";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.MARATHI)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }
//                else if(selectedLanguage=="Bengali") {
//                    language = "bn-IN";
//                    options = TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.BENGALI)
//                        .setTargetLanguage(TranslateLanguage.ENGLISH)
//                        .build()
//                }

                // this@MainActivity.englishGermanTranslator = Translation.getClient(options)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Handle the case where nothing is selected (optional)
            }
        }
        var profilePicUriString = intent.getStringExtra("profilepic");
        var profilePicUri="";
        if(getProfilePicUri()!=null)
            profilePicUri = Uri.parse(getProfilePicUri()).toString();
        val imageViewProfilePicture = findViewById<ImageView>(R.id.profile_ic)
        if (profilePicUri != null) {
//        Bugfender.d("MainActivity","profilePicUri");
//        Bugfender.d("profilePicUri",profilePicUri);
            println("profile pic uri-"+profilePicUri);
            Glide.with(this)
                .load(profilePicUri)
                .placeholder(R.drawable.ic_profile) // Placeholder image while loading
                .error(R.drawable.ic_profile) // Error image if loading fails
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageViewProfilePicture)
        }
        imageViewProfilePicture.setOnClickListener {
            //   Bugfender.d("MainActivity","imageViewProfilePicture");
            openDrawer();
        }

        webView = findViewById(R.id.micView)

        // Enable JavaScript in the WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Load the URL
        val userurl = "https://letstalksign.org/extension/page2.html"
//        webView.webViewClient = MyWebViewClient()

        webView.settings.javaScriptEnabled = true
        webView.settings.userAgentString = "useragentstring"
        webView.getSettings().setSupportMultipleWindows(true);

        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.settings.domStorageEnabled = true

        webView.loadUrl(userurl)
        // webView.loadUrl("file:///android_asset/webview.html");


        webView.clearCache(true) // Clears the cache, including disk and memory caches.
        webView.clearFormData()  // Clears any stored form data in the WebView.
        webView.clearHistory()
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(
                    "WebView Console.log",
                    consoleMessage.sourceId() + " " + consoleMessage.lineNumber() + " " + consoleMessage.message()
                );
                return true
            }
        }

        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")

        val webviewTitle: TextView = findViewById(R.id.title_webview_text);
        val iv_mic: ImageView = findViewById<ImageView>(R.id.webview_mic_ic)
        val text_button: ImageView = findViewById(R.id.webview_text_ic);
        val text_title: TextView = findViewById(R.id.texttitle);
        tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
        iv_mic.layoutParams.width = (35 * scale + 0.5f).toInt();
        text_button.layoutParams.width = (35 * scale + 0.5f).toInt();
        scanButton.layoutParams.width = (35 * scale + 0.5f).toInt();
        text_button.setOnClickListener {
           // Bugfender.d("MainActivity", "text_button")
        //    getData("https://trrain4-web.letstalksign.org/app_log?mode=text_opened&language=$selectedLanguage&customer_id=10009&device_id=$deviceId&gmail_id=$userEmail&token=$token");
            webviewTitle.text = "Entered Text:";
            val text_button: ImageView = findViewById(R.id.webview_text_ic)
            val text_title: TextView = findViewById(R.id.texttitle)
            text_title.setTextSize(18F)
            fun dpToPx(dp: Int): Int {
                val density = resources.displayMetrics.density
                return (dp * density).toInt()
            }
            text_button.layoutParams.height=dpToPx(42);
            text_button.layoutParams.width=dpToPx(42);

            val mic_button: ImageView = findViewById(R.id.webview_mic_ic)
            val mic_title: TextView = findViewById(R.id.speakTitle)
            mic_title.setTextSize(15F)
            mic_button.layoutParams.height=dpToPx(35) // Increase height by 10dp
            mic_button.layoutParams.width=dpToPx(35)

            val scan_button: ImageView = findViewById(R.id.webview_scan_ic)
            val scan_title: TextView = findViewById(R.id.scanTitle)
            scan_title.setTextSize(15F)
            scan_button.layoutParams.height=dpToPx(35)
            scan_button.layoutParams.width=dpToPx(35);

            tv_Speech_to_text.setText("Click the text button above to type text and initiate interpretation.")
            showPopupWithEditText("","Text to Interpret")
        }
        iv_mic?.let { micButton ->
            micButton.setOnClickListener(View.OnClickListener {
//                getData("https://trrain4-web.letstalksign.org/app_log?mode=audio_opened&language=$selectedLanguage&customer_id=10009&device_id=$deviceId&gmail_id=$userEmail&token=$token")
//                Bugfender.d("MainActivity","micButton");

                val text_button: ImageView = findViewById(R.id.webview_text_ic)
                val text_title: TextView = findViewById(R.id.texttitle)
                text_title.setTextSize(15F)
                fun dpToPx(dp: Int): Int {
                    val density = resources.displayMetrics.density
                    return (dp * density).toInt()
                }
                text_button.layoutParams.height=dpToPx(35);
                text_button.layoutParams.width=dpToPx(35);

                val mic_button: ImageView = findViewById(R.id.webview_mic_ic)
                val mic_title: TextView = findViewById(R.id.speakTitle)
                mic_title.setTextSize(18F)
                mic_button.layoutParams.height=dpToPx(42) // Increase height by 10dp
                mic_button.layoutParams.width=dpToPx(42)

                val scan_button: ImageView = findViewById(R.id.webview_scan_ic)
                val scan_title: TextView = findViewById(R.id.scanTitle)
                scan_title.setTextSize(15F)
                scan_button.layoutParams.height=dpToPx(35)
                scan_button.layoutParams.width=dpToPx(35);
                webviewTitle.setText("Spoken Text:")
                tv_Speech_to_text.setText("Click the mic button above perform speach to sign interpretation.")
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)

                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
                } catch (e: Exception) {
                    println(e.toString())
                }

            })
        }

//        galleryActivityLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//            }
//        }
        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
//            Bugfender.d("Scan","Camera activity launcher");
//            Bugfender.d("ScanResult",result.toString());
//            Bugfender.d("ScanResult",result.resultCode.toString()+imageUri.toString());

            if (result.resultCode == Activity.RESULT_OK) {
                // Bugfender.d("ImageOK",imageUri.toString());
                imageUri?.let { recogniserImage(0, it) };
                // Handle the result here
                // The captured image is usually available via the 'imageUri' property
            } else {
                //Bugfender.e("ScanError",result.toString());
            }
        }
//        scanButton.setOnClickListener{
//          //  Bugfender.d("MainActivity","scanButton");
//        //    getData("https://trrain4-web.letstalksign.org/app_log?mode=scan_opened&language=english&customer_id=10009&device_id=$deviceId&gmail_id=$userEmail&token=$token")
//            webviewTitle.setText("Scanned Text:");
//            val text_button: ImageView = findViewById(R.id.webview_text_ic)
//            val text_title: TextView = findViewById(R.id.texttitle)
//            text_title.setTextSize(15F)
//            fun dpToPx(dp: Int): Int {
//                val density = resources.displayMetrics.density
//                return (dp * density).toInt()
//            }
//            text_button.layoutParams.height=dpToPx(35);
//            text_button.layoutParams.width=dpToPx(35);
//
//            val mic_button: ImageView = findViewById(R.id.webview_mic_ic)
//            val mic_title: TextView = findViewById(R.id.speakTitle)
//            mic_title.setTextSize(15F)
//            mic_button.layoutParams.height=dpToPx(35) // Increase height by 10dp
//            mic_button.layoutParams.width=dpToPx(35)
//
//            val scan_button: ImageView = findViewById(R.id.webview_scan_ic)
//            val scan_title: TextView = findViewById(R.id.scanTitle)
//            scan_title.setTextSize(18F)
//            scan_button.layoutParams.height=dpToPx(42)
//            scan_button.layoutParams.width=dpToPx(42)
//            tv_Speech_to_text.setText("Click the scan button above to scan text and initiate interpretation.")
//            if(selectedLanguage!="English")
//            {
//              //  Bugfender.d("Scan","Not English")
//                showLanguageNotSupportedDialog(this);
//            }
//            else
//            {
//
//                if (isCameraPermissionGranted()) {
//                 //   Bugfender.d("Scan","camera permission granted");
//                    PickImageCamera();
//                }
//                else {
//                  //  Bugfender.d("Scan","camera permission Not granted");
//                    requestCameraPermission()
//                }
//            }
//
//        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.my_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        navigationView.setNavigationItemSelectedListener { menuItem ->
           // Bugfender.d("MainActivity","navigationView.setNavigationItemSelectedListener");
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    openAccessibilitySettings();
                    println("sssssssssseeeeeeeeeeeeeeeeetttttttttttttttttt");
                    // Handle settings click
                    // Add your logic here
                    true
                }
                R.id.nav_logout -> {
                    showLogoutAlert();

                    // Handle logout click
                    // Add your logic here
                    true
                }
                else -> false
            }
        }

    }
    private fun openAccessibilitySettings() {
       // Bugfender.d("MainActivity","openAccessibilitySettings");
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent,1);
    }

    private fun showLogoutAlert() {
      //  Bugfender.d("MainActivity","showLogoutAlert");
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
//            Bugfender.d("showLogoutAlert","Yes");
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, Signin_page::class.java)
                Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
             //   getData("https://trrain4-web.letstalksign.org/app_log?mode=logout&customer_id=10009&device_id=$deviceId&gmail_id=$userEmail&token=$token");
                startActivity(intent)
                finish()
            }
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
         //   Bugfender.d("showLogoutAlert","Cancel");
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun showProgressDialog() {
      //  Bugfender.d("MainActivity","showProgressDialog");
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Downloading translation model please wait this may take few seconds...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }
    private fun dismissProgressDialog() {
     //   Bugfender.d("MainActivity","dismissProgressDialog");
        progressDialog?.dismiss()
        progressDialog = null
    }

    fun translatetoEnglish(msg: CharSequence, callback: (String) -> Unit) {
        showProgressDialog()

        englishGermanTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                dismissProgressDialog()

                englishGermanTranslator.translate(msg.toString())
                    .addOnSuccessListener { translatedText ->
                        println("Translation success: $translatedText")
                        callback(translatedText)
                    }
                    .addOnFailureListener { exception: Exception ->
                        println("Translation failure: $exception")
                        callback("Translation failed")
                    }
            }
            .addOnFailureListener { exception: Exception ->
                dismissProgressDialog()
                println("Model download failure: $exception")
                callback("Model download failed")
            }
    }
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopupWithEditText(initialText: CharSequence, Title:String) {
        //Bugfender.d("MainActivity","showPopupWithEditText");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        val builder = AlertDialog.Builder(this)
        val layout = layoutInflater.inflate(R.layout.popup_layout, null)
        interpretButton = layout.findViewById<Button>(R.id.interpretButton);
        val languagetext: TextView = layout.findViewById(R.id.type_title);
        var editText = layout.findViewById<EditText>(R.id.popupEditText)
        var title = layout.findViewById<TextView>(R.id.titlePopup);
        var remainingWords = layout.findViewById<TextView>(R.id.remainingWordCount);

        val cancelButton = layout.findViewById<Button>(R.id.cancelButton);
        var titletext = languagetext.text;
        var ogtext = titletext;
        println("jkdsvhjbfhjbvfhbjbfvdjhbdjhdbhbf " + initialText)
        if (initialText == "") {
            interpretButton.isEnabled = false;
            println("sdbvhjbschsbhdbdhbdcnvjfvj");
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Calculate the current word count
                val words = charSequence?.trim()?.split(Regex("\\s+"))
                currentWordCount = words?.size ?: 0

                // Update the remaining word count
                remainingWordCount = maxWordCount - currentWordCount
                // Assuming you have a TextView to display the remaining count
                // You can replace R.id.remainingWordCount with the actual ID of your TextView
                remainingWords.text = "RemainingWords: $remainingWordCount"

                // Enable/disable the interpret button based on the word count
                if (remainingWordCount < 0) {
                    interpretButton.isEnabled = false
                    ReadyFlag = false
                }
                if (interpretButton.text == "Interpret" && remainingWordCount >= 0) {
                    interpretButton.isEnabled = true
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        if (selectedLanguage == "English") {
            languagetext.visibility = View.GONE;
            editText.setImeHintLocales(LocaleList(Locale("en", "us")))
        } else if (selectedLanguage == "Tamil") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "vanakam for " + "வணக்கம். This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("ta", "IN")))
        } else if (selectedLanguage == "Telugu") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Halō for " + "హలో. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("te", "IN")))
        } else if (selectedLanguage == "Kannada") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Namaskara for " + "ನಮಸ್ಕಾರ. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("kn", "IN")))
        } else if (selectedLanguage == "Malayalam") {
            editText.setImeHintLocales(LocaleList(Locale("ml", "IN")))
        } else if (selectedLanguage == "Hindi") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "namaste for " + "नमस्ते. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("hi", "IN")))
        } else if (selectedLanguage == "Gujarati") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "namaste for " + "નમસ્તે. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("gu", "IN")))
        } else if (selectedLanguage == "Marathi") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Namaskāra for " + "नमस्कार. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("mr", "IN")))
        } else if (selectedLanguage == "Bengali") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Hyālō for " + "হ্যালো. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("bn", "IN")))
        }


        title.setText(Title)
        //editText.setText("");
        if (initialText != "")
            editText.setText(sanitizeText(initialText.toString()))
//        if(!ReadyFlag){
//            interpretButton.setText("Loading..")
//            interpretButton.isEnabled=false;
//        }
//        else{
        interpretButton.setText("Interpret")
        if (remainingWordCount > 0) {
            interpretButton.isEnabled = true;
        }
        //  }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                interpretButton.isEnabled = (!charSequence.isNullOrBlank())
//                if(!ReadyFlag)
//                {
//                    interpretButton.isEnabled=false;
//                }

            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        // Make the EditText scrollable
//        editText.setOnTouchListener { _, event ->
//            editText.parent.requestDisallowInterceptTouchEvent(true)
//            false
//        }

        builder.setView(layout)
        val dialog = builder.create();
        dialog.show();
        interpretButton.setOnClickListener {

            if (Title == "Scanned Text") {
              //  getData("https://trrain4-web.letstalksign.org/app_log?mode=scan_interpreted&language=english&customer_id=10009&device_id=$deviceId&gmail_id=$userEmail&token=$token")
            } else if (Title == "Text to Interpret") {
             //   getData("https://trrain4-web.letstalksign.org/app_log?mode=text_interpreted&language=$selectedLanguage&customer_id=10009&device_id=$deviceId&gmail_id=$userEmail&token=$token")
            } else if (Title == "Recognised Text") {
            //    getData("https://trrain4-web.letstalksign.org/app_log?mode=audio_interpreted&language=$selectedLanguage&customer_id=10009&device_id=$deviceId&gmail_id=$userEmail&token=$token")
            }
            if (selectedLanguage != "English") {
                if (selectedLanguage == "Tamil") {
                    language = "ta-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.TAMIL)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                } else if (selectedLanguage == "Telugu") {
                    language = "te-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.TELUGU)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                } else if (selectedLanguage == "Kannada") {
                    language = "kn-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.KANNADA)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                } else if (selectedLanguage == "Hindi") {
                    language = "hi-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.HINDI)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                } else if (selectedLanguage == "Gujarati") {
                    language = "gu-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.GUJARATI)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                } else if (selectedLanguage == "Marathi") {
                    language = "mr-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.MARATHI)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                } else if (selectedLanguage == "Bengali") {
                    language = "bn-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.BENGALI)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                }
                this@MainActivity.englishGermanTranslator = Translation.getClient(options)
                translatetoEnglish(editText.text.toString())
                { text ->
                    Transcribetext = text
                    println("translated text" + text);
                    //   Bugfender.d("MainAcEdittextPopup",text.toString());
                    var ftext = text
                    ftext = ftext.replace("\n", "");
                    ftext = ftext.replace("\b", "");
                    var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
                    if (!ReadyFlag) {
                        tv_Speech_to_text?.setText("Please wait the model is loading...!")
                    } else {
                        tv_Speech_to_text?.setText(ftext);
                        tv_Speech_to_text?.visibility = View.VISIBLE;
                    }
                    //val jsCode = "sendMessage('${ftext}');"
                    val jsCode = "sendMessage(\"${ftext}\")";
                    println(jsCode);
                    webView.evaluateJavascript(jsCode, null)
                }
            } else {
                // Bugfender.d("EditText",editText.text.toString());
                println(editText.text);
                Transcribetext = editText.text.toString();
                var ftext = editText.text.toString()
                ftext = ftext.replace("\n", "");
                ftext = ftext.replace("\b", "");
                var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
                println("wefhibgufhdvbhfdbhjbhjsd");
                if (!ReadyFlag) {
                    println("djncjsdjcsbdhjbsjhbs")
                    tv_Speech_to_text?.setText("Please wait the model is loading...!")
                    tv_Speech_to_text?.visibility = View.VISIBLE;
                } else {
                    tv_Speech_to_text?.setText(sanitizeText(ftext));
                    tv_Speech_to_text?.visibility = View.VISIBLE;
                }
                val jsCode = "sendMessage(\"${ftext}\")";
                println(jsCode);
                webView.evaluateJavascript(jsCode, null)
                dialog.dismiss()
            }

            // Handle the Ok button click

//            val msg=removeQuotes(msg)+"thankyou";

            // Do something with the new text
             dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        fun showLanguageNotSupportedDialog(context: Context) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            val builder = AlertDialog.Builder(this)
            val layout = layoutInflater.inflate(R.layout.language_support_alert, null)
            val okButoon: Button = layout.findViewById(R.id.okButton);
            builder.setView(layout)
            val dialog = builder.create();
            okButoon.setOnClickListener {
                dialog.dismiss();
            }
            dialog.show();
        }
    }
}
