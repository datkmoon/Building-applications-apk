package com.example.aiflix

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

// ===================== الألوان والثوابت =====================
val BG = Color(0xFF12101F)
val CARD = Color(0xFF1D1A2E)
val GOLD = Color(0xFFFFD93D)
val ACCENT = Color(0xFFFF6B6B)
val TXT = Color(0xFFEDEBF5)
val SUB = Color(0xFF9A96B3)

val FS_PROJECT = "gen-lang-client-0218887039"
val FS_DB = "ai-studio-remixaicinemahub-acbcef0c-aaf6-4ffc-8861-aab0a44de2ff"
val TMDB_KEY = "3ddf430dc71e432b8a635e59cc7a8d3e"

fun fsUrl(): String =
    "https://firestore.googleapis.com/v1/projects/" + FS_PROJECT + "/databases/" + FS_DB + "/documents:runQuery"

// ===================== بيانات المستخدم =====================
data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)

// ===================== أدوات الشبكة =====================
fun httpGet(url: String): String? = try {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 15000
    conn.readTimeout = 20000
    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
    val code = conn.responseCode
    if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else null
} catch (e: Exception) { null }

fun httpPostJson(url: String, body: String): String? = try {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.connectTimeout = 15000
    conn.readTimeout = 20000
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
    conn.doOutput = true
    conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
    val code = conn.responseCode
    if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else null
} catch (e: Exception) { null }

fun sha256Hex(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    val sb = StringBuilder()
    for (b in digest) {
        val v = b.toInt() and 0xFF
        if (v < 16) sb.append('0')
        sb.append(Integer.toHexString(v))
    }
    return sb.toString()
}

fun jsonEscape(s: String): String {
    return s.replace("\\", "\\\\").replace("\"", "\\"")
}

// ===================== تسجيل الدخول (نفس منطق موقع AIFLIX) =====================
fun usersQuery(field: String, value: String): String =
    "{\"structuredQuery\":{\"from\":[{\"collectionId\":\"users\"}],\"where\":{\"fieldFilter\":{\"field\":{\"fieldPath\":\"" + field + "\"},\"op\":\"EQUAL\",\"value\":{\"stringValue\":\"" + jsonEscape(value) + "\"}}},\"limit\":1}}"

fun queryUserDoc(field: String, value: String): JSONObject? {
    val resp = httpPostJson(fsUrl(), usersQuery(field, value)) ?: return null
    return try {
        val arr = JSONArray(resp)
        for (i in 0 until arr.length()) {
            val doc = arr.optJSONObject(i)?.optJSONObject("document")
            if (doc != null) return doc
        }
        null
    } catch (e: Exception) { null }
}

// يرجع المستخدم أو رسالة خطأ
fun loginAttempt(identifier: String, password: String): Pair<UserProfile?, String?> {
    val trimmed = identifier.trim()
    val lower = trimmed.lowercase()
    if (lower.isEmpty() || password.isEmpty()) return Pair(null, "الرجاء إدخال البريد الإلكتروني وكلمة المرور.")

    var doc = queryUserDoc("email", lower)
    if (doc == null && lower.contains("gamil.com")) doc = queryUserDoc("email", lower.replace("gamil.com", "gmail.com"))
    if (doc == null && lower.contains("gmail.com")) doc = queryUserDoc("email", lower.replace("gmail.com", "gamil.com"))
    if (doc == null) doc = queryUserDoc("username", trimmed)
    if (doc == null) {
        val direct = httpGet("https://firestore.googleapis.com/v1/projects/" + FS_PROJECT + "/databases/" + FS_DB + "/documents/users/" + URLEncoder.encode(trimmed, "UTF-8"))
        if (direct != null) {
            try { doc = JSONObject(direct) } catch (e: Exception) { }
        }
    }
    if (doc == null) return Pair(null, "البريد الإلكتروني أو كلمة المرور غير صحيحة.")

    val fields = doc.optJSONObject("fields") ?: return Pair(null, "حدث خطأ، جرب مرة ثانية.")
    fun str(f: String): String = fields.optJSONObject(f)?.optString("stringValue") ?: ""

    var valid = false
    val hash = str("passwordHash")
    val salt = str("passwordSalt")
    if (hash.isNotEmpty() && salt.isNotEmpty()) {
        valid = sha256Hex(salt + password) == hash.lowercase()
    }
    if (!valid) {
        val plain = str("password")
        if (plain.isNotEmpty() && plain.trim() == password.trim()) valid = true
    }
    if (!valid) return Pair(null, "البريد الإلكتروني أو كلمة المرور غير صحيحة.")

    val status = str("status")
    val banned = fields.optJSONObject("isBanned")?.optBoolean("booleanValue") ?: false
    if (status == "banned" || banned) return Pair(null, "هذا الحساب موقوف من إدارة المنصة.")

    val docId = doc.optString("name").substringAfterLast("/")
    val email = str("email").ifEmpty { lower }
    val name = str("displayName").ifEmpty { str("username").ifEmpty { email.substringBefore("@") } }
    val role = str("role").ifEmpty { "member" }
    return Pair(UserProfile(docId, email, name, role), null)
}

// ===================== بحث TMDB (نفس محرك موقعك) =====================
fun genreNames(ids: JSONArray): String {
    val map = mapOf(
        28 to "أكشن", 12 to "مغامرة", 16 to "أنيميشن", 35 to "كوميديا", 80 to "جريمة",
        99 to "وثائقي", 18 to "دراما", 10751 to "عائلي", 14 to "فانتازيا", 36 to "تاريخي",
        27 to "رعب", 10402 to "موسيقى", 9648 to "غموض", 10749 to "رومانسية",
        878 to "خيال علمي", 53 to "إثارة", 10752 to "حرب", 37 to "غربي",
        10759 to "أكشن ومغامرة", 10765 to "خيال علمي و فانتازيا", 10768 to "حرب وسياسة"
    )
    val out = ArrayList<String>()
    for (i in 0 until ids.length()) {
        val n = map[ids.optInt(i)]
        if (n != null) out.add(n)
        if (out.size >= 3) break
    }
    return out.joinToString("، ")
}

fun tmdbSearchSync(query: String): List<MovieData> {
    val list = ArrayList<MovieData>()
    if (query.trim().isEmpty()) return list
    val url = "https://api.themoviedb.org/3/search/multi?api_key=" + TMDB_KEY +
        "&language=ar-SA&query=" + URLEncoder.encode(query.trim(), "UTF-8")
    val resp = httpGet(url) ?: return list
    val results = try { JSONObject(resp).optJSONArray("results") } catch (e: Exception) { null } ?: return list
    val n = Math.min(results.length(), 15)
    for (i in 0 until n) {
        val r = results.optJSONObject(i) ?: continue
        val media = r.optString("media_type")
        if (media != "movie" && media != "tv") continue
        val isTv = media == "tv"
        val tmdbId = r.optInt("id")
        var title = r.optString(if (isTv) "name" else "title")
        if (title.isEmpty()) title = r.optString(if (isTv) "original_name" else "original_title")
        if (title.isEmpty()) continue
        val date = r.optString(if (isTv) "first_air_date" else "release_date")
        val year = if (date.length >= 4) date.substring(0, 4) else ""
        val score = r.optDouble("vote_average", 0.0)
        val rating = if (score > 0) (Math.round(score * 10.0) / 10.0).toString() + "/10" else ""
        val overview = r.optString("overview")
        val genreStr = genreNames(r.optJSONArray("genre_ids") ?: JSONArray())
        var poster = ""
        val pp = r.optString("poster_path")
        if (pp.isNotEmpty()) poster = "https://image.tmdb.org/t/p/w500" + pp
        var backdrop = poster
        val bp = r.optString("backdrop_path")
        if (bp.isNotEmpty()) backdrop = "https://image.tmdb.org/t/p/w780" + bp
        var imdb = ""
        try {
            val ext = httpGet("https://api.themoviedb.org/3/" + media + "/" + tmdbId + "/external_ids?api_key=" + TMDB_KEY)
            if (ext != null) imdb = JSONObject(ext).optString("imdb_id")
        } catch (e: Exception) { }
        var stream: String
        if (imdb.startsWith("tt")) {
            stream = "https://embed.smashystream.com/playere.php?imdb=" + imdb
            if (isTv) stream = stream + "&season=1&episode=1"
        } else {
            stream = "https://embed.smashystream.com/playere.php?tmdb=" + tmdbId
            if (isTv) stream = stream + "&season=1&episode=1"
        }
        list.add(
            MovieData(
                titleAr = title,
                titleEn = "",
                isSeries = isTv,
                year = year,
                rating = rating,
                genres = genreStr,
                cast = "",
                synopsis = overview,
                poster = poster,
                backdrop = backdrop,
                imdbId = imdb,
                streamUrl = stream
            )
        )
    }
    return list
}

// ===================== تحميل الصور =====================
object ImageCache {
    val map = HashMap<String, Bitmap>()
}

fun loadBitmapSync(url: String): Bitmap? {
    if (url.isEmpty()) return null
    ImageCache.map[url]?.let { return it }
    return try {
        val bmp = BitmapFactory.decodeStream(URL(url).openStream())
        if (bmp != null) ImageCache.map[url] = bmp
        bmp
    } catch (e: Exception) { null }
}

// ===================== التنقل =====================
sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    data class Details(val movie: MovieData) : Screen()
    data class Player(val movie: MovieData) : Screen()
}

@Composable
fun WebImage(url: String, modifier: Modifier) {
    var bmp by remember(url) { mutableStateOf(ImageCache.map[url]) }
    LaunchedEffect(url) {
        if (bmp == null) {
            bmp = withContext(Dispatchers.IO) { loadBitmapSync(url) }
        }
    }
    if (bmp != null) {
        Image(
            bitmap = bmp!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier.background(CARD))
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(color = BG) {
                    AiflixApp()
                }
            }
        }
    }
}

@Composable
fun AiflixApp() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("aiflix_prefs", Context.MODE_PRIVATE) }
    var user by remember { mutableStateOf<UserProfile?>(null) }
    var checked by remember { mutableStateOf(false) }
    val stack = remember { mutableStateListOf<Screen>() }

    LaunchedEffect(Unit) {
        val id = prefs.getString("user_id", null)
        if (id != null) {
            user = UserProfile(
                id = id,
                email = prefs.getString("user_email", "")!!,
                name = prefs.getString("user_name", "")!!,
                role = prefs.getString("user_role", "member")!!
            )
            stack.add(Screen.Home)
        }
        checked = true
    }

    if (!checked) {
        Box(Modifier.fillMaxSize().background(BG))
        return
    }

    if (user == null) {
        LoginScreen(onSuccess = { u ->
            user = u
            prefs.edit()
                .putString("user_id", u.id)
                .putString("user_email", u.email)
                .putString("user_name", u.name)
                .putString("user_role", u.role)
                .apply()
            stack.clear()
            stack.add(Screen.Home)
        })
        return
    }

    val current: Screen = if (stack.isEmpty()) Screen.Home else stack.last()

    BackHandler(enabled = stack.size > 1) { stack.removeAt(stack.size - 1) }

    when (val scr = current) {
        is Screen.Login -> { }
        is Screen.Home -> HomeScreen(user!!, onLogout = {
            prefs.edit().clear().apply()
            user = null
            stack.clear()
        }, onOpen = { m -> stack.add(Screen.Details(m)) })
        is Screen.Details -> DetailsScreen(scr.movie, onPlay = { m -> stack.add(Screen.Player(m)) })
        is Screen.Player -> PlayerScreen(scr.movie)
    }
}

// ===================== شاشة تسجيل الدخول =====================
@Composable
fun LoginScreen(onSuccess: (UserProfile) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(BG).padding(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AIFLIX", color = GOLD, fontSize = 46.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text("منصة الأفلام والمسلسلات #1", color = SUB, fontSize = 14.sp)
            Spacer(Modifier.height(36.dp))
            Text("تسجيل الدخول", color = TXT, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("البريد الإلكتروني") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            if (loading) {
                CircularProgressIndicator(color = GOLD)
            } else {
                Button(
                    onClick = {
                        loading = true
                        error = null
                        Thread {
                            val (u, err) = loginAttempt(email, password)
                            if (u != null) {
                                onSuccess(u)
                            } else {
                                error = err
                            }
                            loading = false
                        }.start()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GOLD, contentColor = Color(0xFF1A1608)),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("دخول", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (error != null) {
                Spacer(Modifier.height(14.dp))
                Text(error!!, color = ACCENT, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(28.dp))
            Text("نفس حسابك في موقع AIFLIX يعمل هنا", color = SUB, fontSize = 12.sp)
        }
    }
}

// ===================== الشاشة الرئيسية =====================
@Composable
fun HomeScreen(user: UserProfile, onLogout: () -> Unit, onOpen: (MovieData) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<MovieData>?>(null) }
    var searching by remember { mutableStateOf(false) }
    val catalog = remember { catalogMovies() }

    LaunchedEffect(query) {
        if (query.trim().isEmpty()) {
            results = null
            searching = false
        } else {
            searching = true
            val q = query
            delay(600)
            val found = withContext(Dispatchers.IO) { tmdbSearchSync(q) }
            results = found
            searching = false
        }
    }

    Column(Modifier.fillMaxSize().background(BG)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("AIFLIX", color = GOLD, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("مرحباً " + user.name, color = SUB, fontSize = 13.sp)
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "خروج", tint = SUB)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("ابحث عن فيلم أو مسلسل...") },
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "مسح", tint = SUB)
                    }
                } else {
                    Icon(Icons.Filled.Search, contentDescription = "بحث", tint = SUB)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        if (searching) {
            CircularProgressIndicator(
                color = GOLD,
                modifier = Modifier.padding(18.dp)
            )
        }
        val list = results ?: catalog
        val showEmpty = results != null && results!!.isEmpty() && !searching
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            if (results == null) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                    Text(
                        "الأكثر رواجاً",
                        color = TXT, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp, top = 14.dp, bottom = 6.dp)
                    )
                }
            }
            if (showEmpty) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                    Text(
                        "ماكو نتائج للبحث... جرب اسم ثاني",
                        color = SUB, fontSize = 14.sp,
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(list) { m ->
                MovieCard(m, onOpen)
            }
        }
    }
}

@Composable
fun MovieCard(m: MovieData, onOpen: (MovieData) -> Unit) {
    Column(
        modifier = Modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CARD)
            .clickable { onOpen(m) }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(10.dp))
        ) {
            WebImage(m.poster, Modifier.fillMaxSize())
            Text(
                m.rating,
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GOLD)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
            Text(
                if (m.isSeries) "مسلسل" else "فيلم",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC221F35))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Text(
            m.titleAr,
            color = TXT, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        )
    }
}

// ===================== شاشة التفاصيل =====================
@Composable
fun DetailsScreen(m: MovieData, onPlay: (MovieData) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BG)
            .verticalScroll(rememberScrollState())
    ) {
        Box(Modifier.fillMaxWidth().height(230.dp)) {
            WebImage(m.backdrop, Modifier.fillMaxSize())
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, BG)))
            )
        }
        Row(Modifier.padding(horizontal = 16.dp)) {
            Box(
                Modifier
                    .width(105.dp)
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                WebImage(m.poster, Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.padding(top = 8.dp)) {
                Text(m.titleAr, color = TXT, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                if (m.titleEn.isNotEmpty()) {
                    Text(m.titleEn, color = SUB, fontSize = 13.sp)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    (if (m.isSeries) "مسلسل" else "فيلم") + "  •  " + m.year + (if (m.rating.isNotEmpty()) "  •  ⭐ " + m.rating else ""),
                    color = GOLD, fontSize = 13.sp
                )
                if (m.genres.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(m.genres, color = SUB, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = { onPlay(m) },
            colors = ButtonDefaults.buttonColors(containerColor = GOLD, contentColor = Color(0xFF1A1608)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
        ) {
            Text("▶   شاهد الآن", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        if (m.synopsis.isNotEmpty()) {
            Spacer(Modifier.height(22.dp))
            Text("القصة", color = TXT, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(6.dp))
            Text(m.synopsis, color = SUB, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
        if (m.cast.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text("طاقم العمل", color = TXT, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(6.dp))
            Text(m.cast, color = SUB, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ===================== شاشة المشغل =====================
@Composable
fun PlayerScreen(m: MovieData) {
    var loading by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Text(
            "← " + m.titleAr,
            color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp)
        )
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                loading = false
                            }
                        }
                        webChromeClient = WebChromeClient()
                        loadUrl(m.streamUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (loading) {
                CircularProgressIndicator(
                    color = GOLD,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
