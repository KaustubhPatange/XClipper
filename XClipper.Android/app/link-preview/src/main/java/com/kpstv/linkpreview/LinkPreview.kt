package com.kpstv.linkpreview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import com.kpstv.linkpreview.databinding.LayoutPreviewBinding
import com.kpstv.xclipper.extensions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import okhttp3.*

class LinkPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    fun interface LinkPreviewListener {
        fun onLoadComplete(title: String, subtitle: String?, imageUrl: String?)
    }

    var loadCompleteListener : LinkPreviewListener? = null

    private val cornerSize = 5 * resources.displayMetrics.density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wrap = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        super.onMeasure(widthMeasureSpec, wrap)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        blendBackground()
    }

    private val binding: LayoutPreviewBinding = LayoutPreviewBinding.inflate(context.layoutInflater(), this, true).also { it.root.collapse() }

    private val webView = WebView(context)
    private var lifecycleScope: CoroutineScope? = null
    private var currentUrl: String = ""
        set(value) {
            if (value != field) {
                field = value
            }
        }

    init {
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(this, "HTMLOUT")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url != null) currentUrl = url
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        }

        context.withStyledAttributes(attrs, R.styleable.LinkPreview, defStyle) {
            if (hasValue(R.styleable.LinkPreview_imgSrc)) {
                binding.imageView.setImageResource(getResourceId(R.styleable.LinkPreview_imgSrc, -1))
            }
            if (hasValue(R.styleable.LinkPreview_titleTextColor)) {
                binding.tvTitle.setTextColor(getColorStateList(R.styleable.LinkPreview_titleTextColor)?.defaultColor!!)
            }
            if (hasValue(R.styleable.LinkPreview_subtitleTextColor)) {
                binding.tvSubtitle.setTextColor(getColorStateList(R.styleable.LinkPreview_subtitleTextColor)?.defaultColor!!)
            }
        }
    }

    fun setTitle(value: String) {
        binding.tvTitle.text = value.replace("&amp;", "&")
        binding.root.show()
    }

    fun setSubtitle(value: String) {
        binding.tvSubtitle.text = value.replace("&amp;", "&")
    }

    fun setImage(url: String) {
        binding.imageView.load(url)
    }

    fun onClick(block: () -> Unit) {
        binding.clickableView.setOnClickListener { block.invoke()  }
    }

    private val TAG = javaClass.simpleName

    fun showPreview(url: String, lifecycleScope: CoroutineScope) {
        this.lifecycleScope = lifecycleScope
        webView.loadUrl(url)
    }

    @Suppress("unused")
    @JavascriptInterface
    fun processHTML(html: String) {
        val scope = lifecycleScope ?: return
        loadContent(html, scope)
    }

    private fun loadContent(body: String, lifecycleScope: CoroutineScope) {
        lifecycleScope.launch {
            ensureActive()

            val title = Regex(REGEX_TITLE).find(body)?.groups?.get(1)?.value
            val subtitle = Regex(REGEX_DESCRIPTION).find(body)?.groups?.get(1)?.value
            val imageUrl = Regex(REGEX_IMAGE).find(body)?.groups?.get(1)?.value

            if (title != null) setTitle(title)
            if (subtitle != null) setSubtitle(subtitle)
            if (imageUrl != null) {
                setImage(imageUrl)
            }

            if (title == null) {
                binding.root.collapse()
            } else {
                loadCompleteListener?.onLoadComplete(title, subtitle, imageUrl)
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