package com.kpstv.xclipper.ui.fragments.sheets

import android.content.ClipData
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.kpstv.xclipper.data.model.UrlInfo
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.elements.CustomRoundedBottomSheetFragment
import com.kpstv.xclipper.extensions.hide
import com.kpstv.xclipper.extensions.listeners.ResponseResult
import com.kpstv.xclipper.extensions.show
import com.kpstv.xclipper.extensions.viewBinding
import com.kpstv.xclipper.feature_special.R
import com.kpstv.xclipper.feature_special.databinding.BottomSheetUrlBinding
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import com.kpstv.xclipper.ui.utils.LaunchUtils
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ShortenUriSheet(
    private val onClick: SimpleFunction = {}
) : CustomRoundedBottomSheetFragment(R.layout.bottom_sheet_url) {

    @Inject lateinit var tinyUrlApiHelper: TinyUrlApiHelper
    @Inject lateinit var clipboardProvider: ClipboardProvider

    val binding: BottomSheetUrlBinding by viewBinding(BottomSheetUrlBinding::bind)

    private val job = SupervisorJob()
    private lateinit var longUrl: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rootMain.hide()

        longUrl = arguments?.getString(LONG_URL) ?: run {
            Toasty.error(requireContext(), getString(R.string.invalid_url)).show()
            return
        }

        CoroutineScope(job + Dispatchers.IO + Dispatchers.Main).launch {
            val response: ResponseResult<UrlInfo> = tinyUrlApiHelper.createShortenUrl(longUrl)
            when(response) {
                is ResponseResult.Complete -> {
                    updateUI(response.data)
                }
                is ResponseResult.Error -> {
                    Toasty.error(requireContext(), "Error: ${response.error.message}").show()
                    dismiss()
                }
            }
        }
    }

    private fun updateUI(urlInfo: UrlInfo) {
        binding.progressBar.hide()
        binding.tvShortUrl.text = urlInfo.shortUrl
        binding.tvLongUrl.text = urlInfo.longUrl
        binding.ivCopyUrl.setOnClickListener {
            clipboardProvider.setClipboard(ClipData.newRawUri(urlInfo.shortUrl, Uri.parse(urlInfo.shortUrl)))
            Toasty.info(requireContext(), getString(R.string.ctc)).show()

            onClick.invoke()
            dismiss()
        }
        binding.ivOpenUrl.setOnClickListener {
            LaunchUtils.commonUrlLaunch(requireContext(), urlInfo.shortUrl)

            onClick.invoke()
            dismiss()
        }
        binding.rootMain.show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (job.isActive) job.cancel()
        super.onDismiss(dialog)
    }

    companion object {
        const val LONG_URL = "sheet.long_url"
    }
}