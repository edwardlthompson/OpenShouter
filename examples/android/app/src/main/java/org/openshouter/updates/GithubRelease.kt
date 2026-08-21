package org.openshouter.updates

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GithubRelease {
    data class Parsed(val htmlUrl: String, val assets: List<ProductUpdate.NamedAsset>)

    fun parse(json: String): Parsed? {
        return try {
            val root = JSONObject(json)
            val htmlUrl = root.optString("html_url", ProductUpdate.RELEASES_PAGE)
            val assets = mutableListOf<ProductUpdate.NamedAsset>()
            val arr = root.optJSONArray("assets") ?: return Parsed(htmlUrl, assets)
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val name = item.optString("name")
                val url = item.optString("browser_download_url")
                if (name.isNotBlank() && url.isNotBlank()) {
                    assets.add(ProductUpdate.NamedAsset(name, url))
                }
            }
            Parsed(htmlUrl, assets)
        } catch (_: Exception) {
            null
        }
    }

    fun fetchLatest(userAgentVersion: String): Parsed? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(ProductUpdate.RELEASES_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "OpenShouter/$userAgentVersion")
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return null
            parse(conn.inputStream.bufferedReader().use { it.readText() })
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
