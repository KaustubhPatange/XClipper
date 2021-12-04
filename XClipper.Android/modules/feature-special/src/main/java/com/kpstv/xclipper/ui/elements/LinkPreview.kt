package com.kpstv.xclipper.ui.elements

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import com.google.android.material.shape.ShapeAppearanceModel
import com.kpstv.xclipper.utils.LinkUtils
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.feature_special.R
import com.kpstv.xclipper.feature_special.databinding.LayoutPreviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    init {
        val appearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(resources.getDimension(R.dimen.corner_radius))
            .build()
        binding.imageView.shapeAppearanceModel = appearanceModel

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
            if (hasValue(R.styleable.LinkPreview_urlTextColor)) {
                binding.tvHost.setTextColor(getColorStateList(R.styleable.LinkPreview_urlTextColor)?.defaultColor!!)
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

    fun setHostUrl(url: String) {
        val host = Uri.parse(url).host
        binding.tvHost.text = host
    }

    fun setImage(url: String?) {
        if (url != null) {
            binding.imageView.show()
            binding.imageView.load(url)
        } else {
            binding.imageView.collapse()
        }
    }

    fun onClick(block: () -> Unit) {
        binding.clickableView.setOnClickListener { block.invoke()  }
    }

    private val TAG = javaClass.simpleName

    fun loadPreview(url: String, lifecycleScope: CoroutineScope) {
        lifecycleScope.launch {
            val data = LinkUtils.fetchUrl(url)

            val title = data?.title
            val subtitle = data?.description
            val imageUrl = data?.imageUrl

            if (title != null) setTitle(title)
            if (subtitle != null)
                setSubtitle(subtitle)
            else if (title != null)
                setSubtitle(title)
            setHostUrl(url)
            setImage(imageUrl)

            if (title.isNullOrEmpty()) {
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
}