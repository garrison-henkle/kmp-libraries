package dev.henkle.stytch.oauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import dev.henkle.stytch.utils.Browser
import dev.henkle.stytch.utils.BrowserSelector

class OAuthActivity : Activity() {
    private var subsequentResume = false
    private var uriToOpen: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            updateState(newState = intent.extras)
        } else {
            updateState(newState = savedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()
        if (subsequentResume) {
            // Subsequent resumes: either receive the callback uri or are cancelled (null intent)
            intent.data?.also { uri -> setSuccessResult(uri = uri) }
                ?: setErrorResult(ex = OAuthException.UserCanceled)
        } else {
            // First resume: launch the OAuth flow in a browser
            BrowserSelector.getBestBrowser(context = this)?.also { browser ->
                uriToOpen?.also { uri ->
                    val browserLaunchIntent = createBrowserIntent(
                        browser = browser,
                        uri = uri,
                    )
                    startActivity(browserLaunchIntent)
                    subsequentResume = true
                    return
                } ?: setErrorResult(ex = OAuthException.NoUriFound)
            } ?: setErrorResult(ex = OAuthException.NoBrowserFound)
        }
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

//    override fun onPause() {
//        super.onPause()
//        subsequentResume = true
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SUBSEQUENT_RESUME, subsequentResume)
    }

    private fun updateState(newState: Bundle?) {
        if (newState == null) return finish()
        subsequentResume = newState.getBoolean(KEY_SUBSEQUENT_RESUME, false)
        newState.getString(KEY_URI)?.also {
            uriToOpen = Uri.parse(it)
        }
    }

    private fun createBrowserIntent(browser: Browser, uri: Uri): Intent =
        if (browser.supportsCustomTabs) {
            CustomTabsIntent.Builder().build().intent
        } else {
            Intent(Intent.ACTION_VIEW)
        }.apply {
            setPackage(browser.packageName)
            data = uri
        }

    private fun setSuccessResult(uri: Uri) {
        val result = Intent().apply {
            data = uri
        }
        setResult(RESULT_OK, result)
    }

    private fun setErrorResult(ex: OAuthException) {
        val result = Intent().apply {
            putExtra(OAuthException.KEY_OAUTH_EXCEPTION, ex)
        }
        setResult(RESULT_CANCELED, result)
    }

    internal companion object {
        internal fun createResponseHandlingIntent(context: Context, responseUri: Uri?): Intent =
            createBaseIntent(context = context).apply {
                data = responseUri
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

        internal fun createUriLaunchingIntent(context: Context, uri: String): Intent =
            createBaseIntent(context = context).putExtra(KEY_URI, uri)

        internal fun createBaseIntent(context: Context): Intent =
            Intent(context, OAuthActivity::class.java)

        private const val KEY_URI = "uri"
        private const val KEY_SUBSEQUENT_RESUME = "subsequentResume"
    }
}