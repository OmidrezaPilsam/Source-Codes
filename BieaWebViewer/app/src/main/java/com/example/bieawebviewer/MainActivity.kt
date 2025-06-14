package com.example.bieawebviewer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Main activity for the BieaWebViewer application.
 * Displays a WebView to load and show web content from "www.biea.xyz".
 * Includes a refresh button and basic error handling.
 */
class MainActivity : AppCompatActivity() {

    // Declare UI elements that will be initialized in onCreate
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var refreshButton: Button
    private lateinit var layoutRoot: ConstraintLayout // Root layout, useful for Snackbar if implemented

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Sets the UI layout for this activity

        // Initialize UI elements from the layout file
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        refreshButton = findViewById(R.id.refreshButton)
        layoutRoot = findViewById(R.id.root_constraint_layout)

        // Configure WebView settings
        webView.settings.javaScriptEnabled = true // Enable JavaScript execution in WebView
        webView.settings.domStorageEnabled = true   // Enable DOM Storage API (localStorage, sessionStorage)
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // Use cache if available, otherwise load from network

        // Set up WebViewClient to handle page loading events and URL overrides
        webView.webViewClient = object : WebViewClient() {
            /**
             * Called when a page starts loading.
             * Shows the progress bar and hides the WebView.
             */
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE // Show loading indicator
                webView.visibility = View.GONE      // Hide WebView until page starts rendering
            }

            /**
             * Called when a page finishes loading.
             * Hides the progress bar and shows the WebView if content is available.
             * Handles cases where the page might be empty or an error occurred not caught by onReceivedError.
             */
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Check if page has content (contentHeight > 0) or if loading progress reached 100%
                if ((view?.contentHeight ?: 0) > 0 || (view?.progress ?: 0) == 100) {
                    progressBar.visibility = View.GONE   // Hide loading indicator
                    webView.visibility = View.VISIBLE    // Show WebView with loaded content
                } else {
                    // This block handles cases where onPageFinished is called, but the page content is missing
                    // or an error occurred that wasn't trapped by onReceivedError for the main frame.
                    progressBar.visibility = View.GONE
                    webView.visibility = View.GONE // Keep WebView hidden
                    if (isNetworkAvailable()) {
                        showError("Failed to load page content. The page may be empty or an issue occurred.")
                    } else {
                        // If no network, this is likely the cause
                        showError("No internet connection. Please check your network settings and try again.")
                    }
                }
            }

            /**
             * Called when the WebView encounters an error loading a resource.
             * Shows an error message and hides the WebView.
             * Focuses on errors related to the main frame.
             */
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                // We are primarily interested in errors for the main frame (the top-level URL).
                if (request?.isForMainFrame == true) {
                    progressBar.visibility = View.GONE // Hide loading indicator
                    webView.visibility = View.GONE   // Hide WebView
                    val errorMessage = "Error: ${error?.description}"
                    showError(errorMessage) // Display a generic error message

                    // Provide more specific messages for common network errors
                    if (error?.errorCode == ERROR_HOST_LOOKUP ||
                        error?.errorCode == ERROR_CONNECT ||
                        error?.errorCode == ERROR_TIMEOUT ||
                        error?.errorCode == ERROR_UNSUPPORTED_SCHEME) {
                        showError("Failed to connect or load the page. Please check your internet connection or the URL.")
                    }
                }
            }

            /**
             * Determines how to handle URL loading.
             * URLs for "www.biea.xyz" are loaded within the WebView.
             * Other URLs are opened in an external browser.
             */
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                // Check if the URL is for the target website
                return if (url.startsWith("http://www.biea.xyz") || url.startsWith("https://www.biea.xyz")) {
                    false // Load within this WebView (default behavior)
                } else {
                    // For external links, try to open in a default browser or other app
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        // Handle cases where no app can handle the Intent (e.g., no browser installed)
                        Toast.makeText(this@MainActivity, "Could not open external link.", Toast.LENGTH_SHORT).show()
                    }
                    true // Indicate that we have handled the URL loading
                }
            }
        }

        // Set up WebChromeClient to handle UI-related events like progress changes, JavaScript alerts, etc.
        webView.webChromeClient = object : WebChromeClient() {
            /**
             * Called when the page loading progress changes.
             * Can be used for a more granular progress bar if needed.
             */
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // If loading is not complete and progress bar is hidden, show it.
                if (newProgress < 100 && progressBar.visibility == View.GONE) {
                    progressBar.visibility = View.VISIBLE
                }
                // onPageFinished is generally more reliable for hiding the progress bar
                // and showing the WebView, so we don't do that here based solely on newProgress == 100.
            }
        }

        // Set up the refresh button's click listener
        refreshButton.setOnClickListener {
            loadUrlInWebView() // Reload the current page or the initial URL
        }

        // Perform the initial URL load when the activity is created
        loadUrlInWebView()
    }

    /**
     * Loads the predefined URL ("https://www.biea.xyz") into the WebView.
     * Checks for network connectivity before attempting to load.
     * Shows/hides UI elements based on loading state.
     */
    private fun loadUrlInWebView() {
        if (isNetworkAvailable()) {
            webView.visibility = View.GONE      // Hide WebView before starting load/reload
            progressBar.visibility = View.VISIBLE // Show progress bar
            webView.loadUrl("https://www.biea.xyz") // Load the target URL
        } else {
            // Network is not available
            progressBar.visibility = View.GONE // Hide progress bar
            webView.visibility = View.GONE   // Keep WebView hidden
            showError("No internet connection. Please check your network settings and try again.")
        }
    }

    /**
     * Checks if a network connection is available (Wi-Fi, Cellular, Ethernet).
     * @return True if a network connection is available, false otherwise.
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Get the currently active network
        val network = connectivityManager.activeNetwork ?: return false
        // Get the capabilities of the active network
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        // Check if the network has transport capability for Wi-Fi, Cellular, or Ethernet
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    /**
     * Displays an error message to the user.
     * Currently uses a Toast message. Could be extended to use Snackbar or a custom UI.
     * @param message The error message to display.
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // Example of using Snackbar (would require layoutRoot to be a CoordinatorLayout or similar):
        // import com.google.android.material.snackbar.Snackbar
        // Snackbar.make(layoutRoot, message, Snackbar.LENGTH_INDEFINITE)
        //    .setAction("RETRY") { loadUrlInWebView() } // Optional retry action
        //    .show()
    }

    /**
     * Handles the system back button press.
     * If the WebView can navigate back (has a history), it goes back.
     * Otherwise, performs the default back button action (typically exiting the activity).
     */
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack() // Navigate to the previous page in WebView history
        } else {
            super.onBackPressed() // Default behavior (e.g., close activity)
        }
    }
}
