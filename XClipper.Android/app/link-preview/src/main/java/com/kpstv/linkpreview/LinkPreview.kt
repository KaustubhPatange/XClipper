package com.kpstv.linkpreview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import com.kpstv.xclipper.extensions.*
import kotlinx.android.synthetic.main.layout_preview.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.*

class LinkPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val cornerSize = 5 * resources.displayMetrics.density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wrap = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        super.onMeasure(widthMeasureSpec, wrap)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        blendBackground()
    }

    private val view: View = context.layoutInflater().inflate(R.layout.layout_preview, this, true).also { it.collapse() }
    private val webView = WebView(context)
    private var lifecycleScope: CoroutineScope? = null
    private var currentUrl: String = ""
        set(value) {
            if (value != field) {
                field = value
                val scope = lifecycleScope ?: return
                loadPreview(value, scope)
            }
        }

    init {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url != null) currentUrl = url
            }
        }

        context.withStyledAttributes(attrs, R.styleable.LinkPreview, defStyle) {
            if (hasValue(R.styleable.LinkPreview_imgSrc)) {
                view.imageView.setImageResource(getResourceId(R.styleable.LinkPreview_imgSrc, -1))
            }
            if (hasValue(R.styleable.LinkPreview_titleTextColor)) {
                view.tv_title.setTextColor(getColorStateList(R.styleable.LinkPreview_titleTextColor)?.defaultColor!!)
            }
            if (hasValue(R.styleable.LinkPreview_subtitleTextColor)) {
                view.tv_subtitle.setTextColor(getColorStateList(R.styleable.LinkPreview_subtitleTextColor)?.defaultColor!!)
            }
        }
    }

    fun setTitle(value: String) {
        view.tv_title.text = value.replace("&amp;", "&")
    }

    fun setSubtitle(value: String) {
        view.tv_subtitle.text = value.replace("&amp;", "&")
    }

    fun setImage(url: String) {
        view.imageView.load(url)
    }

    fun onClick(block: (String) -> Unit) {
        view.clickableView.setOnClickListener { block.invoke(currentUrl)  }
    }

    private val TAG = javaClass.simpleName

    fun showPreview(url: String, lifecycleScope: CoroutineScope) {
        this.lifecycleScope = lifecycleScope
        webView.loadUrl(url)
    }

    private fun loadPreview(url: String, lifecycleScope: CoroutineScope) {
        lifecycleScope.launch {
            val client = OkHttpClient.Builder()
                .followSslRedirects(true)
                .followRedirects(true)
                .build()
            val response = client.newCall(Request.Builder().url(url).build()).await()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@launch
                response.close()

                val title = Regex(REGEX_TITLE).find(body)?.groups?.get(1)?.value
                val subtitle = Regex(REGEX_DESCRIPTION).find(body)?.groups?.get(1)?.value
                val imageUrl = Regex(REGEX_IMAGE).find(body)?.groups?.get(1)?.value

                if (title != null) setTitle(title)
                if (subtitle != null) setSubtitle(subtitle)
                if (imageUrl != null) {
                    setImage(imageUrl)
                }

                if (title == null) view.collapse() else view.show()
            }
        }
    }

    private fun blendBackground() {
        val background = (parent as? View)?.background
        if (background != null && background is ColorDrawable) {
            val color = background.color
            val darkColor = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
            val gradientDrawable = GradientDrawable().apply {
                setColor(darkColor)
                cornerRadius = cornerSize
            }
            setBackground(gradientDrawable)
        }
    }

    companion object {
        private const val REGEX_TITLE = "property=\"og:title\"\\s?content=\"(.*?)\""
        private const val REGEX_DESCRIPTION = "property=\"og:description\"\\s?content=\"(.*?)\""
        private const val REGEX_IMAGE = "property=\"og:image\"\\s?content=\"(.*?)\""
    }
}