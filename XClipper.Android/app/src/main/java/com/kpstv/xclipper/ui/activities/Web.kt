package com.kpstv.xclipper.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.databinding.ActivityWebBinding
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.await
import com.kpstv.xclipper.extensions.utils.ThemeUtils
import com.kpstv.xclipper.extensions.viewBinding
import kotlinx.android.synthetic.main.activity_web.*
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request

@SuppressLint("SetJavaScriptEnabled")
class Web : AppCompatActivity() {

    companion object {
        const val ARG_URL = "com.kpstv.xclipper.ARG_URL"

        private const val DESKTOP_AGENT =
            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0"
        private const val EXCLUDE_URL_PATTERN = "(cdn)"
    }

    private val TAG = javaClass.simpleName
    private val binding by viewBinding(ActivityWebBinding::inflate)

    private var contentUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeUtils.setTheme(this)

        setContentView(binding.root)

        clearWebViewData()

        contentUrl = intent?.getStringExtra(ARG_URL) ?: run { finish(); return }

        setToolbar()

        setSwipeRefreshLayout()

        binding.webView.settings.apply {
            allowFileAccess = true
            javaScriptEnabled = true
            loadsImagesAutomatically = true
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_NO_CACHE
            setAppCacheEnabled(false)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            private var previousUrl: String? = ""
            private var forceLoad = false
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                contentUrl = url!!
                binding.toolbarHeader.text = Uri.parse(url).host

                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                previousUrl = url
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                /** A dirty way to prevent redirect as well as open enables private browsing */
                if (Build.VERSION.SDK_INT >= 24 && request?.isRedirect == true && !forceLoad) {
                    Coroutines.io {
                        val responseBody = OkHttpClient.Builder().build()
                            .newCall(
                                Request.Builder().cacheControl(CacheControl.FORCE_NETWORK)
                                    .url(request.url.toString()).build()
                            ).await().body?.string()
                        Coroutines.main {
                            val url =
                                App.URL_PATTERN_REGEX.toRegex().find(responseBody ?: "")?.value
                            if (url != null) {
                                if (url.contains(EXCLUDE_URL_PATTERN.toRegex())) {
                                    forceLoad = true
                                    view?.loadUrl(previousUrl)
                                }
                                else {
                                    view?.loadUrl(url)
                                    previousUrl = url
                                }
                            } else {
                                view?.loadUrl(request.url.toString())
                                previousUrl = request.url.toString()
                            }
                        }
                    }
                } else {
                    forceLoad = false
                    view?.loadUrl(request?.url?.toString())
                    previousUrl = request?.url.toString()
                }
                return true
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress < 100 && binding.progressBar.visibility == ProgressBar.GONE) {
                    binding.progressBar.visibility = ProgressBar.VISIBLE
                }
                binding.progressBar.progress = progress
                if (progress == 100) {
                    binding.progressBar.visibility = ProgressBar.GONE
                }
                super.onProgressChanged(view, progress)
            }
        }

        binding.webView.loadUrl(contentUrl)
    }

    private fun setSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            webView?.reload()
        }
    }

    private fun clearWebViewData() {
        CookieManager.getInstance().setAcceptCookie(false)
        binding.webView.clearCache(true)
        binding.webView.clearHistory()

        binding.webView.clearFormData()
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.inflateMenu(R.menu.web_menu)

        if (Build.VERSION.SDK_INT < 29) binding.toolbar.menu.removeItem(R.id.action_force_dark)

        binding.toolbar.setOnMenuItemClickListener { item ->
            item.isChecked = !item.isChecked

            when (item.itemId) {
                R.id.action_open -> {
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        data = Uri.parse(contentUrl)
                    })
                }
               /* R.id.action_desktop_site -> {
                    binding.webView.settings.apply {
                        userAgentString =
                            if (item.isChecked) DESKTOP_AGENT else WebSettings.getDefaultUserAgent(
                                this@Web
                            )
                        loadWithOverviewMode = item.isChecked
                        useWideViewPort = item.isChecked
                    }
                }*/
                R.id.action_force_dark -> {
                    if (Build.VERSION.SDK_INT >= 29) {
                        binding.webView.settings.forceDark = if (item.isChecked) 2 else 0
                    }
                }
                else -> {
                }
            }

            true
        }
    }
}