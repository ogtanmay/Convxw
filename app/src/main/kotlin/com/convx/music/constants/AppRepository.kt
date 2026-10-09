package com.convx.music.constants

/** Canonical links used by the app for its repository, releases, and update feeds. */
object AppRepository {
    const val OWNER = "ogtanmay"
    const val NAME = "Convxw"

    const val OWNER_URL = "https://github.com/$OWNER"
    const val URL = "$OWNER_URL/$NAME"
    const val API_URL = "https://api.github.com/repos/$OWNER/$NAME"
    const val NIGHTLY_DOWNLOAD_URL =
        "https://nightly.link/$OWNER/$NAME/workflows/nightly.yml/main/convx-gms-nightly.zip"

    fun releaseApkUrl(version: String): String =
        "$URL/releases/download/$version/convx-$version.apk"

    fun releaseChangelogUrl(tag: String): String =
        "$URL/releases/download/$tag/changelog.json"
}
