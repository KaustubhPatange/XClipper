package com.kpstv.xclipper.ui.helpers.connection

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.databinding.DialogConnectBinding
import com.kpstv.xclipper.databinding.DialogProgressViewBinding
import com.kpstv.xclipper.di.CommonReusableEntryPoints
import com.kpstv.xclipper.extensions.layoutInflater
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.ui.helpers.AuthenticationHelper
import es.dmoral.toasty.Toasty

class ConnectionHelper(
    private val fragment: Fragment
) {
    private val context by lazy { fragment.requireContext() }
    private val fragmentActivity by lazy { fragment.requireActivity() }
    private val connectionViewModel by fragment.viewModels<ConnectionViewModel>()

    fun startConnectionRequest() : Unit = with(fragment) {
        val binding = DialogConnectBinding.inflate(layoutInflater)

        val alert = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .show()

        binding.btnScanConnect.setOnClickListener {
            var activityResult : ActivityResultLauncher<Intent?>? = null

            activityResult = fragmentActivity.activityResultRegistry.register("connect_dialog_contract", ActivityResultContracts.StartActivityForResult()) { result ->
                activityResult?.unregister()
                parse(result.resultCode, result.data)
            }

            val scanIntent = IntentIntegrator(requireActivity())
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setOrientationLocked(false)
                .setBeepEnabled(false)
                .setPrompt(getString(R.string.scan_code))
                .setBarcodeImageEnabled(false)
                .createScanIntent()

            activityResult.launch(scanIntent)

            alert.dismiss()
        }
    }

    fun startDisconnectRequest() : Unit = with(fragment) {
        val dialog = showConnectionDialog(requireContext())

        connectionViewModel.removeDeviceConnection(requireContext(), ResponseListener(
            complete = {
                dialog.dismiss()
                Toasty.info(requireContext(), getString(R.string.logout_success)).show()
            },
            error = {
                dialog.dismiss()
                Toasty.error(requireContext(), it.message!!).show()
            }
        ))
    }

    private fun parse(resultCode: Int, data: Intent?) {
        val dbConnectionProvider = CommonReusableEntryPoints.get(context).dbConnectionProvider()
        val result = IntentIntegrator.parseActivityResult(resultCode, data)
        if (result?.contents != null) {
            dbConnectionProvider.processResult(result.contents, ResponseListener(
                complete = { options ->
                    /** Check if auth is needed, if so make a auth2 call */
                    if (options.isAuthNeeded) {
                        AuthenticationHelper(fragmentActivity, options.authClientId!!).signIn(
                            options = options,
                            responseListener = ResponseListener(
                                complete = {
                                    /** Here we will make a connection request to the database.*/
                                    makeAConnectionRequest(options)
                                },
                                error = {
                                    Toasty.error(context, "Error: ${it.message}", Toasty.LENGTH_LONG).show()
                                }
                            ))
                    } else {
                        /** Here we will make a connection request to the database.*/
                        makeAConnectionRequest(options)
                    }
                },
                error = {
                    Toasty.error(context, it.message!!).show()
                }
            ))
        }
    }

    private fun makeAConnectionRequest(options: FBOptions) {
        val dialog = showConnectionDialog(context)

        connectionViewModel.updateDeviceConnection(context, options, ResponseListener(
            complete = {
                Toasty.info(context, context.getString(R.string.connect_success)).show()
                dialog.dismiss()
            },
            error = { e ->
                dialog.dismiss()
                Toasty.error(context, e.message!!).show()
            }
        ))
    }

    private fun showConnectionDialog(context: Context): AlertDialog = with(context) {
        val binding = DialogProgressViewBinding.inflate(layoutInflater())

        val dialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}