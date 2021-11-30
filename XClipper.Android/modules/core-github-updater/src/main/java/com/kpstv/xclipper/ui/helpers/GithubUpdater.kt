package com.kpstv.xclipper.ui.helpers

import com.kpstv.update.Updater
import com.kpstv.xclipper.github_updater.BuildConfig

class GithubUpdater {
    companion object {
        const val SETTINGS_URL = "https://github.com/KaustubhPatange/XClipper/raw/master/XClipper.Android/settings.json"
        private const val REPO_OWNER = "KaustubhPatange"
        private const val REPO_NAME = "XClipper"
        private const val UPDATE_REQUEST_CODE = 555

        const val ACTION_FORCE_CHECK_UPDATE = "action_force_check_update"

        fun createUpdater() : Updater {
            return Updater.Builder()
                .setCurrentAppVersion(BuildConfig.VERSION_NAME)
                .setRepoOwner(REPO_OWNER)
                .setRepoName(REPO_NAME)
                .create()
        }
    }
}