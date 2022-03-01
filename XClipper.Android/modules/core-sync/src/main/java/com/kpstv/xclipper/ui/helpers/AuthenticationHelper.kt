package com.kpstv.xclipper.ui.helpers

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
 * @param activity [ComponentActivity] to listen [ActivityResultContract].
 */
internal class AuthenticationHelper(
    private val activity: ComponentActivity,
    lifecycleOwner: LifecycleOwner
) {
    /**
     * An authentication clientId provided by [FBOptions].
     */
    private lateinit var clientId: String
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var responseListener: ResponseListener<Unit>
    private lateinit var auth: FirebaseAuth

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val customContract = object : ActivityResultContract<Intent, Task<GoogleSignInAccount>?>() {
            override fun createIntent(context: Context, input: Intent?): Intent {
                return input!!
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
                return GoogleSignIn.getSignedInAccountFromIntent(
                    intent
                )
            }
        }

    init {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                if (::activityResultLauncher.isInitialized) {
                    activityResultLauncher.unregister()
                }
            }
        })
    }

    // Register to listen onActivityResult() to carry out QR code scanning result.
    fun init() {
        activityResultLauncher = activity.activityResultRegistry
            .register("AuthenticationHelper", customContract) { task ->
                parseActivityResultForTask(task)
            }
    }

    fun setClientId(clientId: String) {
        this.clientId = clientId
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
            Toasty.error(this, getString(R.string.auth_error_initialize_fb)).show()
            return@with
        }

        googleSignInClient = GoogleSignIn.getClient(
            this,
            defaultGoogleSignInOptions(clientId)
        )

        fun performSignIn() {
            auth = Firebase.auth(app)

            val signInIntent = googleSignInClient.signInIntent
            activityResultLauncher.launch(signInIntent)
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
                GoogleSignIn.getClient(
                    context,
                    defaultGoogleSignInOptions(clientId)
                )
            googleSignInClient.signOut()
            return true
        }
    }
}