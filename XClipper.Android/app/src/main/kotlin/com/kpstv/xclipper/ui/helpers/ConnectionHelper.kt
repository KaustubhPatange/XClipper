package com.kpstv.xclipper.ui.helpers

import android.content.Intent
import androidx.activity.ComponentActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.utils.Utils
import com.kpstv.xclipper.ui.viewmodels.MainViewModel
import es.dmoral.toasty.Toasty

class ConnectionHelper(
    private val activity: ComponentActivity,
    private val mainViewModel: MainViewModel,
    private val dbConnectionProvider: DBConnectionProvider
) {
    fun parse(requestCode: Int, resultCode: Int, data: Intent?) {
        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result?.contents != null) {
            dbConnectionProvider.processResult(result.contents, ResponseListener(
                complete = { options ->
                    /** Check if auth is needed, if so make a auth2 call */
                    if (options.isAuthNeeded) {
                        AuthenticationHelper(activity, options.authClientId!!).signIn(
                            options = options,
                            responseListener = ResponseListener(
                                complete = {
                                    /** Here we will make a connection request to the database.*/
                                    makeAConnectionRequest(options)
                                },
                                error = {
                                    Toasty.error(activity, "Error: ${it.message}", Toasty.LENGTH_LONG).show()
                                }
                            ))
                    } else {
                        /** Here we will make a connection request to the database.*/
                        makeAConnectionRequest(options)
                    }
                },
                error = {
                    Toasty.error(activity, it.message!!).show()
                }
            ))
        }
    }

    private fun makeAConnectionRequest(options: FBOptions) {
        val dialog = Utils.showConnectionDialog(activity)

        mainViewModel.updateDeviceConnection(options, ResponseListener(
            complete = {
                Toasty.info(activity, activity.getString(R.string.connect_success)).show()
                dialog.dismiss()
            },
            error = { e ->
                dialog.dismiss()
                Toasty.error(activity, e.message!!).show()
            }
        ))
    }
}