package com.kpstv.update

import com.google.gson.annotations.SerializedName

data class Release(
    val assets: List<Asset>,
    @SerializedName("assets_url")
    val assetsUrl: String,
    val author: Author,
    val body: String,
    @SerializedName("created_at")
    val createdAt: String,
    val draft: Boolean,
    @SerializedName("html_url")
    val htmlUrl: String,
    val id: Int,
    val name: String,
    @SerializedName("node_id")
    val nodeId: String,
    val prerelease: Boolean,
    @SerializedName("published_at")
    val publishedAt: String,
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("tarball_url")
    val tarballUrl: String,
    @SerializedName("target_commitish")
    val targetCommitish: String,
    @SerializedName("upload_url")
    val uploadUrl: String,
    val url: String,
    @SerializedName("zipball_url")
    val zipballUrl: String
) {
    data class Asset(
        @SerializedName("browser_download_url")
        val browserDownloadUrl: String,
        @SerializedName("content_type")
        val contentType: String,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("download_count")
        val downloadCount: Int,
        val id: Int,
        val label: Any?,
        val name: String,
        @SerializedName("node_id")
        val nodeId: String,
        val size: Int,
        val state: String,
        @SerializedName("updated_at")
        val updatedAt: String,
        val uploader: Uploader,
        val url: String
    ) {
        data class Uploader(
            @SerializedName("avatar_url")
            val avatarUrl: String,
            @SerializedName("events_url")
            val eventsUrl: String,
            @SerializedName("followers_url")
            val followersUrl: String,
            @SerializedName("following_url")
            val followingUrl: String,
            @SerializedName("gists_url")
            val gistsUrl: String,
            @SerializedName("gravatar_id")
            val gravatarId: String,
            @SerializedName("html_url")
            val htmlUrl: String,
            val id: Int,
            val login: String,
            @SerializedName("node_id")
            val nodeId: String,
            @SerializedName("organizations_url")
            val organizationsUrl: String,
            @SerializedName("received_events_url")
            val receivedEventsUrl: String,
            @SerializedName("repos_url")
            val reposUrl: String,
            @SerializedName("site_admin")
            val siteAdmin: Boolean,
            @SerializedName("starred_url")
            val starredUrl: String,
            @SerializedName("subscriptions_url")
            val subscriptionsUrl: String,
            val type: String,
            val url: String
        )
    }

    data class Author(
        @SerializedName("avatar_url")
        val avatarUrl: String,
        @SerializedName("events_url")
        val eventsUrl: String,
        @SerializedName("followers_url")
        val followersUrl: String,
        @SerializedName("following_url")
        val followingUrl: String,
        @SerializedName("gists_url")
        val gistsUrl: String,
        @SerializedName("gravatar_id")
        val gravatarId: String,
        @SerializedName("html_url")
        val htmlUrl: String,
        val id: Int,
        val login: String,
        @SerializedName("node_id")
        val nodeId: String,
        @SerializedName("organizations_url")
        val organizationsUrl: String,
        @SerializedName("received_events_url")
        val receivedEventsUrl: String,
        @SerializedName("repos_url")
        val reposUrl: String,
        @SerializedName("site_admin")
        val siteAdmin: Boolean,
        @SerializedName("starred_url")
        val starredUrl: String,
        @SerializedName("subscriptions_url")
        val subscriptionsUrl: String,
        val type: String,
        val url: String
    )
}