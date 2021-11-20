package com.kpstv.xclipper.ui.helpers

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import es.dmoral.toasty.Toasty

/**
 * Helper class to make Firebase auth process simpler
 * @param activity [ComponentActivity] to listen [ActivityResultContract]
 * @param clientId An authentication clientId provided by [FBOptions]
 */
class AuthenticationHelper(
    private val activity: ComponentActivity,
    private var clientId: String
) {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var responseListener: ResponseListener<Unit>
    private lateinit var auth: FirebaseAuth

    private val customContract =
        object : ActivityResultContract<Intent, Task<GoogleSignInAccount>?>() {
            override fun createIntent(context: Context, input: Intent?): Intent {
                return input!!
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
                if (resultCode != RESULT_OK)
                    return null
                return GoogleSignIn.getSignedInAccountFromIntent(intent)
            }
        }

    /**
     * Start the auth process.
     * @param options FBOptions that [DBConnectionProvider] provides.
     * @param responseListener Listener to notify the calling activity about result.
     */
    fun signIn(options: FBOptions, responseListener: ResponseListener<Unit>) = with(activity) {
        this@AuthenticationHelper.responseListener = responseListener

        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(options.apiKey)
            .setApplicationId(options.appId)
            .setDatabaseUrl(options.endpoint)
            .build()

        if (!FirebaseSyncHelper.isRegistered(this)) {
            FirebaseSyncHelper.register(this, firebaseOptions)
        }

        val app = FirebaseSyncHelper.get() ?: run {
            Toasty.error(this, getString(R.string.error_initialize_fb)).show()
            return@with
        }

        googleSignInClient = GoogleSignIn.getClient(this, defaultGoogleSignInOptions(clientId))

        fun performSignIn() {
            var getResult : ActivityResultLauncher<Intent?>? = null
            getResult = activity.activityResultRegistry.register("myContract_1", customContract) { task ->
                getResult?.unregister()
                parseActivityResultForTask(task)
            }

            auth = Firebase.auth(app)

            val signInIntent = googleSignInClient.signInIntent
            getResult.launch(signInIntent)
        }

        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            googleSignInClient.signOut().addOnCompleteListener { performSignIn() }
        } else performSignIn()
    }

    private fun parseActivityResultForTask(task: Task<GoogleSignInAccount>?) {
        try {
            val account = task?.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: Exception) {
            unregister()
            responseListener.onError(e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) = with(activity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                unregister()
                if (task.isSuccessful) {
                    responseListener.onComplete(Unit)
                } else {
                    responseListener.onError(
                        task.exception ?: Exception("Unexpected error occurred")
                    )
                }
            }
    }

    private fun unregister() {
        FirebaseSyncHelper.unregister()
    }

    companion object {
        fun defaultGoogleSignInOptions(clientId: String): GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build()

        fun signOutGoogle(context: Context, clientId: String?): Boolean {
            if (clientId == null) return false
            val googleSignInClient =
                GoogleSignIn.getClient(context, defaultGoogleSignInOptions(clientId))
            googleSignInClient.signOut()
            return true
        }
    }
}