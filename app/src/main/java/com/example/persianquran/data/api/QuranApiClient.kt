package com.example.persianquran.data.api

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.persianquran.data.model.SearchResultItem
import com.example.persianquran.data.model.Verse
import com.example.persianquran.data.surah.QuranMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class QuranApiClient {

    companion object {
        private const val TAG = "QuranApiClient"
        private const val AUTH_URL = "https://oauth2.quran.foundation/oauth2/token"
        private const val BASE_URL = "https://apis.quran.foundation/content/api/v4"
        const val AUDIO_BASE_URL = "https://audio.qurancdn.com/"
        const val BACKUP_AUDIO_BASE_URL = "https://verses.quran.com/"

        // Default credentials from project configuration
        private const val FALLBACK_CLIENT_ID = "0efe9aa9-9002-4d94-8cf7-5c79ce99b587"
        private const val FALLBACK_CLIENT_SECRET = "qfcs_04ee5682f0f3444ca2a34324cf7554aac1b368619bc4411e9c6864fbcb7a5a43"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val authMutex = Mutex()
    private var cachedToken: String? = null
    private var tokenExpiresAtMs: Long = 0L

    private val clientId: String
        get() {
            return try {
                val field = BuildConfig::class.java.getField("QF_CLIENT_ID")
                val v = field.get(null) as? String
                if (!v.isNullOrBlank()) v else FALLBACK_CLIENT_ID
            } catch (_: Exception) {
                FALLBACK_CLIENT_ID
            }
        }

    private val clientSecret: String
        get() {
            return try {
                val field = BuildConfig::class.java.getField("QF_CLIENT_SECRET")
                val v = field.get(null) as? String
                if (!v.isNullOrBlank()) v else FALLBACK_CLIENT_SECRET
            } catch (_: Exception) {
                FALLBACK_CLIENT_SECRET
            }
        }

    suspend fun getAccessToken(forceRefresh: Boolean = false): String = authMutex.withLock {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedToken != null && now < tokenExpiresAtMs - 60_000) {
            return@withLock cachedToken!!
        }

        withContext(Dispatchers.IO) {
            val credentials = "$clientId:$clientSecret"
            val base64Auth = Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

            val body = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("scope", "content")
                .build()

            val request = Request.Builder()
                .url(AUTH_URL)
                .addHeader("Authorization", "Basic $base64Auth")
                .addHeader("User-Agent", "Mozilla/5.0 (Android; QuranPersian)")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string()
                    Log.e(TAG, "OAuth token error: ${response.code} - $err")
                    throw RuntimeException("خطا در دریافت توکن احراز هویت: ${response.code}")
                }
                val jsonStr = response.body?.string() ?: throw RuntimeException("پاسخ سرور خالی بود")
                val json = JSONObject(jsonStr)
                val token = json.getString("access_token")
                val expiresInSec = json.optLong("expires_in", 3600L)
                cachedToken = token
                tokenExpiresAtMs = now + (expiresInSec * 1000)
                Log.d(TAG, "Access token obtained successfully, expires in $expiresInSec seconds")
                token
            }
        }
    }

    private suspend fun executeGet(url: String, retryOn401: Boolean = true): String {
        val token = getAccessToken(forceRefresh = false)
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .addHeader("x-auth-token", token)
                .addHeader("x-client-id", clientId)
                .addHeader("User-Agent", "Mozilla/5.0 (Android; QuranPersian)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 401 && retryOn401) {
                    Log.w(TAG, "Received 401, refreshing token and retrying...")
                    getAccessToken(forceRefresh = true)
                    return@withContext executeGet(url, retryOn401 = false)
                }
                if (!response.isSuccessful) {
                    val err = response.body?.string()
                    Log.e(TAG, "API request failed: ${response.code} $url - $err")
                    throw RuntimeException("خطا در دریافت اطلاعات از سرور (${response.code})")
                }
                response.body?.string() ?: throw RuntimeException("پاسخ سرور خالی بود")
            }
        }
    }

    suspend fun getVersesByChapter(
        chapterId: Int,
        reciterId: Int = 7,
        translationId: Int = 135
    ): List<Verse> = withContext(Dispatchers.IO) {
        val allVerses = mutableListOf<Verse>()
        var currentPage = 1
        var totalPages = 1

        while (currentPage <= totalPages) {
            val url = "$BASE_URL/verses/by_chapter/$chapterId" +
                    "?language=fa" +
                    "&words=false" +
                    "&translations=$translationId" +
                    "&audio=$reciterId" +
                    "&fields=text_uthmani,chapter_id,verse_number,page_number,juz_number" +
                    "&page=$currentPage" +
                    "&per_page=50"

            val jsonStr = executeGet(url)
            val json = JSONObject(jsonStr)

            val pagination = json.optJSONObject("pagination")
            if (pagination != null) {
                totalPages = pagination.optInt("total_pages", 1)
            }

            val versesArray = json.optJSONArray("verses")
            if (versesArray != null) {
                for (i in 0 until versesArray.length()) {
                    val vObj = versesArray.getJSONObject(i)
                    val vId = vObj.optInt("id", i + 1)
                    val vNum = vObj.optInt("verse_number", i + 1)
                    val vKey = vObj.optString("verse_key", "$chapterId:$vNum")
                    val uthmani = vObj.optString("text_uthmani", "")
                    val pageNum = vObj.optInt("page_number", 1)
                    val juzNum = vObj.optInt("juz_number", 1)

                    // Audio URL
                    var audioUrl: String? = null
                    val audioObj = vObj.optJSONObject("audio")
                    if (audioObj != null) {
                        val relUrl = audioObj.optString("url", "")
                        if (relUrl.isNotBlank()) {
                            audioUrl = if (relUrl.startsWith("http")) {
                                relUrl
                            } else {
                                AUDIO_BASE_URL + relUrl.trimStart('/')
                            }
                        }
                    }

                    // Translation
                    var translationText = ""
                    val trArray = vObj.optJSONArray("translations")
                    if (trArray != null && trArray.length() > 0) {
                        val trObj = trArray.getJSONObject(0)
                        val rawText = trObj.optString("text", "")
                        // Clean HTML tags if any like <sup foot_note=1>...</sup>
                        translationText = rawText.replace(Regex("<[^>]*>"), "").trim()
                    }

                    allVerses.add(
                        Verse(
                            id = vId,
                            verseNumber = vNum,
                            verseKey = vKey,
                            textUthmani = uthmani,
                            chapterId = chapterId,
                            pageNumber = pageNum,
                            juzNumber = juzNum,
                            translation = translationText,
                            audioUrl = audioUrl
                        )
                    )
                }
            }

            currentPage++
            // Safety break to prevent infinite loops
            if (currentPage > 20) break
        }

        allVerses
    }

    suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val encoded = java.net.URLEncoder.encode(trimmed, "UTF-8")
        val url = "$BASE_URL/search?q=$encoded&language=fa&size=25"

        try {
            val jsonStr = executeGet(url)
            val json = JSONObject(jsonStr)
            val searchObj = json.optJSONObject("search") ?: return@withContext emptyList()
            val resultsArray = searchObj.optJSONArray("results") ?: return@withContext emptyList()

            val items = mutableListOf<SearchResultItem>()
            for (i in 0 until resultsArray.length()) {
                val rObj = resultsArray.getJSONObject(i)
                val verseKey = rObj.optString("verse_key", "")
                val text = rObj.optString("text", "").replace(Regex("<[^>]*>"), "").trim()

                val parts = verseKey.split(":")
                val surahNum = parts.getOrNull(0)?.toIntOrNull() ?: 1
                val verseNum = parts.getOrNull(1)?.toIntOrNull() ?: 1

                val surah = QuranMetadata.getSurahById(surahNum)
                val surahName = surah?.namePersian ?: "سوره $surahNum"

                var translationText = ""
                val trArray = rObj.optJSONArray("translations")
                if (trArray != null && trArray.length() > 0) {
                    val trObj = trArray.getJSONObject(0)
                    translationText = trObj.optString("text", "").replace(Regex("<[^>]*>"), "").trim()
                }

                items.add(
                    SearchResultItem(
                        verseKey = verseKey,
                        surahNumber = surahNum,
                        verseNumber = verseNum,
                        surahNamePersian = surahName,
                        textUthmani = text,
                        translation = translationText
                    )
                )
            }
            items
        } catch (e: Exception) {
            Log.e(TAG, "Search error: ${e.message}", e)
            emptyList()
        }
    }
}
