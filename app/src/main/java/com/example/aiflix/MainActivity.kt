package com.example.aiflix

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ==================== الألوان والثوابت ====================
val BG = Color(0xFF12101F)
val CARD = Color(0xFF1D1A2E)
val CARD2 = Color(0xFF262242)
val GOLD = Color(0xFFFFD93D)
val ACCENT = Color(0xFFFF6B6B)
val TEXT = Color(0xFFEDEAF8)
val SUB = Color(0xFF9B95B5)

// الموقع الرئيسي — التطبيق ما يحتوي أي مفتاح أو توكن، كل شي يجي من السيرفر
val BASE = "https://aiflex2.vercel.app"

val MovieData.displayTitle: String get() = if (titleAr.isNotEmpty()) titleAr else titleEn
val MovieData.favKey: String get() = if (titleEn.isNotEmpty()) titleEn else titleAr

// ==================== HTTP ====================
fun httpGet(urlStr: String): String? {
    return try {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 20000
        conn.setRequestProperty("User-Agent", "AIFLIX-App/2.0")
        conn.setRequestProperty("Accept", "application/json")
        try {
            if (conn.responseCode in 200..299) conn.inputStream.bufferedReader().readText() else null
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        null
    }
}

fun httpPostJson(urlStr: String, body: JSONObject): String? {
    return try {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 25000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("User-Agent", "AIFLIX-App/2.0")
        val out = body.toString().toByteArray(Charsets.UTF_8)
        conn.outputStream.use { it.write(out) }
        if (conn.responseCode in 200..299) conn.inputStream.bufferedReader().readText() else null
    } catch (e: Exception) {
        null
    }
}

// ==================== تحويل بيانات الأفلام من السيرفر ====================
fun parseMovieArray(arr: JSONArray): List<MovieData> {
    val list = ArrayList<MovieData>()
    for (i in 0 until arr.length()) {
        val o = arr.optJSONObject(i) ?: continue
        val poster = o.optString("p", "")
        val bd = o.optString("bd", "")
        val backdrop = if (bd.isNotEmpty()) bd else poster.replace("_FMjpg_UX600_", "")
        list.add(
            MovieData(
                titleAr = o.optString("a", ""),
                titleEn = o.optString("e", ""),
                isSeries = o.optString("t", "m") == "s",
                year = o.optString("y", ""),
                rating = o.optString("r", ""),
                genres = o.optString("g", ""),
                cast = o.optString("c", ""),
                synopsis = o.optString("syn", ""),
                poster = poster,
                backdrop = backdrop,
                imdbId = o.optString("i", ""),
                streamUrl = o.optString("s", "")
            )
        )
    }
    return list
}

fun parseMovies(jsonText: String): List<MovieData> {
    return try {
        val trimmed = jsonText.trim()
        if (trimmed.startsWith("[")) parseMovieArray(JSONArray(trimmed))
        else {
            val obj = JSONObject(trimmed)
            val arr = obj.optJSONArray("movies")
            if (arr != null) parseMovieArray(arr) else ArrayList()
        }
    } catch (e: Exception) {
        ArrayList()
    }
}

// ==================== المفضلة (مثل الموقع — محفوظة على الجهاز) ====================
fun favoritesKey(userId: String): String = "aiflix_favorites_" + userId

fun loadFavorites(ctx: Context, userId: String): List<MovieData> {
    if (userId.isEmpty()) return ArrayList()
    return try {
        val stored = ctx.getSharedPreferences("aiflix_prefs", Context.MODE_PRIVATE).getString(favoritesKey(userId), "[]")
        parseMovieArray(JSONArray(stored ?: "[]"))
    } catch (e: Exception) {
        ArrayList()
    }
}

fun movieToCompactJson(m: MovieData): JSONObject {
    val o = JSONObject()
    o.put("a", m.titleAr)
    o.put("e", m.titleEn)
    o.put("t", if (m.isSeries) "s" else "m")
    o.put("y", m.year)
    o.put("r", m.rating)
    o.put("g", m.genres)
    o.put("c", m.cast)
    o.put("syn", m.synopsis)
    o.put("i", m.imdbId)
    o.put("p", m.poster)
    o.put("bd", m.backdrop)
    o.put("s", m.streamUrl)
    return o
}

fun toggleFavoriteInPrefs(ctx: Context, userId: String, m: MovieData): List<MovieData> {
    val prefs = ctx.getSharedPreferences("aiflix_prefs", Context.MODE_PRIVATE)
    val current = ArrayList(loadFavorites(ctx, userId))
    val idx = current.indexOfFirst { it.favKey == m.favKey }
    if (idx >= 0) current.removeAt(idx) else current.add(m)
    val arr = JSONArray()
    for (x in current) arr.put(movieToCompactJson(x))
    prefs.edit().putString(favoritesKey(userId), arr.toString()).apply()
    return current
}

// ==================== حساب المستخدم ====================
class UserData(val id: String, val email: String, val name: String, val role: String)
class AuthResult(val success: Boolean, val user: UserData?, val error: String?)

fun parseAuth(txt: String?): AuthResult {
    if (txt == null) return AuthResult(false, null, "ما نكدر نوصل للسيرفر، تأكد من الإنترنت وحاول مرة ثانية")
    return try {
        val obj = JSONObject(txt)
        if (obj.optBoolean("success", false)) {
            val u = obj.optJSONObject("user") ?: JSONObject()
            var name = u.optString("displayName", "")
            if (name.isEmpty()) name = u.optString("username", "")
            if (name.isEmpty()) {
                val em = u.optString("email", "")
                name = if (em.contains("@")) em.substring(0, em.indexOf("@")) else "عضو"
            }
            AuthResult(true, UserData(u.optString("id", ""), u.optString("email", ""), name, u.optString("role", "member")), null)
        } else {
            AuthResult(false, null, obj.optString("error", "فشلت العملية، حاول مرة ثانية"))
        }
    } catch (e: Exception) {
        AuthResult(false, null, "صار خطأ بالاتصال، حاول مرة ثانية")
    }
}

suspend fun apiLogin(identifier: String, password: String): AuthResult = withContext(Dispatchers.IO) {
    val body = JSONObject()
    body.put("identifier", identifier)
    body.put("password", password)
    parseAuth(httpPostJson(BASE + "/api/app/login", body))
}

suspend fun apiRegister(name: String, email: String, password: String, confirm: String, key: String): AuthResult = withContext(Dispatchers.IO) {
    val body = JSONObject()
    body.put("name", name)
    body.put("email", email)
    body.put("password", password)
    body.put("confirmPassword", confirm)
    body.put("key", key)
    parseAuth(httpPostJson(BASE + "/api/app/register", body))
}

// ==================== الكتالوج والبحث والمواسم من السيرفر ====================
suspend fun apiCatalog(): List<MovieData> = withContext(Dispatchers.IO) {
    val txt = httpGet(BASE + "/api/app/catalog")
    val parsed = if (txt != null) parseMovies(txt) else ArrayList()
    if (parsed.isNotEmpty()) parsed else catalogMovies()
}

suspend fun apiSearch(q: String): List<MovieData> = withContext(Dispatchers.IO) {
    val enc = URLEncoder.encode(q, "UTF-8")
    val txt = httpGet(BASE + "/api/app/search?q=" + enc)
    if (txt != null) parseMovies(txt) else ArrayList()
}

suspend fun apiSeriesEpisodes(imdbId: String): Map<Int, List<Int>> = withContext(Dispatchers.IO) {
    val map = HashMap<Int, List<Int>>()
    try {
        if (imdbId.isNotEmpty()) {
            val txt = httpGet(BASE + "/api/series/episodes?imdbId=" + URLEncoder.encode(imdbId, "UTF-8"))
            if (txt != null) {
                val obj = JSONObject(txt)
                val seasonsObj = obj.optJSONObject("seasons")
                if (seasonsObj != null) {
                    val keys = seasonsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val sn = k.toIntOrNull() ?: continue
                        val eps = seasonsObj.optJSONArray(k) ?: continue
                        val list = ArrayList<Int>()
                        for (i in 0 until eps.length()) list.add(eps.optInt(i, i + 1))
                        map[sn] = list
                    }
                }
            }
        }
    } catch (e: Exception) {
    }
    map
}

// ==================== تحميل الصور ====================
object ImageCache {
    val cache = HashMap<String, Bitmap>()
    fun get(url: String): Bitmap? = cache[url]
    fun put(url: String, bmp: Bitmap) {
        if (cache.size > 80) cache.clear()
        cache[url] = bmp
    }
}

suspend fun loadBitmap(url: String): Bitmap? {
    if (url.isEmpty()) return null
    ImageCache.get(url)?.let { return it }
    return withContext(Dispatchers.IO) {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            val bmp = BitmapFactory.decodeStream(conn.inputStream)
            conn.disconnect()
            if (bmp != null) ImageCache.put(url, bmp)
            bmp
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun WebImage(url: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    var bmp by remember(url) { mutableStateOf<ImageBitmap?>(ImageCache.get(url)?.asImageBitmap()) }
    LaunchedEffect(url) {
        if (bmp == null) {
            val b = loadBitmap(url)
            if (b != null) bmp = b.asImageBitmap()
        }
    }
    val b = bmp
    if (b != null) {
        Image(bitmap = b, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier.background(CARD2))
    }
}

// ==================== أشكال الحقول والأزرار ====================
@Composable
fun darkFieldColors(): androidx.compose.material3.TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GOLD,
    unfocusedBorderColor = Color(0xFF3A3358),
    focusedLabelColor = GOLD,
    unfocusedLabelColor = SUB,
    cursorColor = GOLD,
    focusedTextColor = TEXT,
    unfocusedTextColor = TEXT
)

@Composable
fun goldButtonColors(): androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors(
    containerColor = GOLD,
    contentColor = Color(0xFF1A1500)
)

// ==================== التطبيق الرئيسي ====================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiflixApp()
        }
    }
}

fun sectionTitle(section: String): String = when (section) {
    "movies" -> "الأفلام"
    "series" -> "المسلسلات"
    "fav" -> "المفضلة"
    else -> "الرئيسية"
}

@Composable
fun AiflixApp() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("aiflix_prefs", Context.MODE_PRIVATE) }
    var userId by remember { mutableStateOf<String?>(prefs.getString("user_id", null)) }
    var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var userEmail by remember { mutableStateOf(prefs.getString("user_email", "") ?: "") }
    var userRole by remember { mutableStateOf(prefs.getString("user_role", "member") ?: "member") }
    var stack by remember { mutableStateOf(if (userId != null) listOf("home") else listOf("login")) }
    var selectedMovie by remember { mutableStateOf<MovieData?>(null) }
    var favorites by remember(userId) { mutableStateOf<List<MovieData>>(loadFavorites(ctx, userId ?: "")) }

    fun saveSession(id: String, email: String, name: String, role: String) {
        prefs.edit().putString("user_id", id).putString("user_email", email).putString("user_name", name).putString("user_role", role).apply()
        userId = id
        userEmail = email
        userName = name
        userRole = role
    }

    fun doLogout() {
        prefs.edit().remove("user_id").remove("user_email").remove("user_name").remove("user_role").apply()
        userId = null
        userName = ""
        userEmail = ""
        userRole = "member"
        selectedMovie = null
        stack = listOf("login")
    }

    val current = if (stack.isEmpty()) "login" else stack.last()
    BackHandler(enabled = current != "home" && current != "login") {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    when (current) {
        "login" -> LoginScreen(
            onSuccess = { u ->
                saveSession(u.id, u.email, u.name, u.role)
                stack = listOf("home")
            },
            onSignup = { stack = stack + "signup" }
        )
        "signup" -> SignupScreen(
            onBack = { if (stack.size > 1) stack = stack.dropLast(1) },
            onCreated = { u ->
                saveSession(u.id, u.email, u.name, u.role)
                stack = listOf("home")
            }
        )
        "home" -> HomeScreen(
            userName = userName,
            userEmail = userEmail,
            userRole = userRole,
            favorites = favorites,
            onLogout = { doLogout() },
            onOpen = { m ->
                selectedMovie = m
                stack = stack + "details"
            }
        )
        "details" -> {
            val m = selectedMovie
            if (m != null) {
                DetailsScreen(
                    movie = m,
                    isFavorite = favorites.any { it.favKey == m.favKey },
                    onToggleFavorite = {
                        val id = userId
                        if (id != null) favorites = toggleFavoriteInPrefs(ctx, id, m)
                    },
                    onBack = { if (stack.size > 1) stack = stack.dropLast(1) },
                    onPlay = { stack = stack + "player" }
                )
            }
        }
        "player" -> {
            val m = selectedMovie
            if (m != null) {
                PlayerScreen(movie = m, onBack = { if (stack.size > 1) stack = stack.dropLast(1) })
            } else {
                if (stack.size > 1) stack = stack.dropLast(1)
            }
        }
        else -> LoginScreen(
            onSuccess = { u ->
                saveSession(u.id, u.email, u.name, u.role)
                stack = listOf("home")
            },
            onSignup = { stack = stack + "signup" }
        )
    }
}

// ==================== شاشة تسجيل الدخول ====================
@Composable
fun LoginScreen(onSuccess: (UserData) -> Unit, onSignup: () -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().background(BG).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))
        Text("AIFLIX", color = GOLD, fontSize = 44.sp, fontWeight = FontWeight.Black)
        Text("منصة الأفلام والمسلسلات", color = SUB, fontSize = 14.sp)
        Spacer(Modifier.height(40.dp))
        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text("البريد الإلكتروني") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = darkFieldColors()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = darkFieldColors()
        )
        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Text(error ?: "", color = ACCENT, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (loading) return@Button
                val idf = identifier.trim()
                if (idf.isEmpty() || password.isEmpty()) {
                    error = "اكتب البريد الإلكتروني وكلمة المرور"
                    return@Button
                }
                loading = true
                error = null
                scope.launch {
                    val res = apiLogin(idf, password)
                    loading = false
                    if (res.success && res.user != null) onSuccess(res.user!!) else error = res.error ?: "فشل تسجيل الدخول"
                }
            },
            enabled = !loading,
            colors = goldButtonColors(),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color(0xFF1A1500), strokeWidth = 2.dp)
            } else {
                Text("تسجيل الدخول", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(14.dp))
        TextButton(onClick = onSignup) {
            Text("ما عندك حساب؟ إنشاء حساب جديد", color = GOLD, fontSize = 14.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text("الإصدار 2.0.1", color = SUB, fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))
    }
}

// ==================== شاشة إنشاء الحساب ====================
@Composable
fun SignupScreen(onBack: () -> Unit, onCreated: (UserData) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().background(BG).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = GOLD) }
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text("إنشاء حساب جديد", color = TEXT, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("انضم إلى AIFLIX وابدأ بالمشاهدة فوراً", color = SUB, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("الاسم بالكامل") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = darkFieldColors()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("البريد الإلكتروني") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = darkFieldColors()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور (6 أحرف على الأقل)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = darkFieldColors()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("تأكيد كلمة المرور") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = darkFieldColors()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = key,
            onValueChange = { key = it.uppercase().filter { c -> !c.isWhitespace() } },
            label = { Text("مفتاح إنشاء الحساب (8 خانات)") },
            singleLine = true,
            supportingText = { Text("تحصل عليه من المالك — يتم تغييره كل 60 ثانية", color = SUB, fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = darkFieldColors()
        )
        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Text(error ?: "", color = ACCENT, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (loading) return@Button
                val n = name.trim()
                val em = email.trim().lowercase()
                if (n.isEmpty()) {
                    error = "اكتب اسمك بالكامل"
                    return@Button
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
                    error = "صيغة البريد الإلكتروني غير صالحة"
                    return@Button
                }
                if (password.length < 6) {
                    error = "كلمة المرور لازم تكون 6 أحرف على الأقل"
                    return@Button
                }
                if (password != confirm) {
                    error = "كلمتا المرور غير متطابقتين"
                    return@Button
                }
                if (key.length != 8) {
                    error = "المفتاح لازم يكون 8 خانات بالضبط"
                    return@Button
                }
                loading = true
                error = null
                scope.launch {
                    val res = apiRegister(n, em, password, confirm, key)
                    loading = false
                    if (res.success && res.user != null) onCreated(res.user!!) else error = res.error ?: "فشل إنشاء الحساب"
                }
            },
            enabled = !loading,
            colors = goldButtonColors(),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color(0xFF1A1500), strokeWidth = 2.dp)
            } else {
                Text("إنشاء حساب وابدأ بالمشاهدة فوراً", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ==================== عنصر القائمة الجانبية ====================
@Composable
fun DrawerItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) CARD2 else Color.Transparent).clickable { onClick() }.padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = if (selected) GOLD else TEXT, fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

// ==================== بطاقة الفيلم ====================
@Composable
fun MovieCard(m: MovieData, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(6.dp).clip(RoundedCornerShape(12.dp)).background(CARD).clickable { onClick() }
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.72f).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
            WebImage(url = if (m.poster.isNotEmpty()) m.poster else m.backdrop, modifier = Modifier.fillMaxSize())
            if (m.rating.isNotEmpty()) {
                Text(
                    m.rating,
                    color = GOLD,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).background(Color(0xAA000000)).clip(RoundedCornerShape(6.dp)).padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
        Column(Modifier.padding(8.dp)) {
            Text(m.displayTitle, color = TEXT, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            val sub = ArrayList<String>()
            if (m.year.isNotEmpty()) sub.add(m.year)
            if (m.isSeries) sub.add("مسلسل")
            if (sub.isNotEmpty()) {
                Text(sub.joinToString(" • "), color = SUB, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ==================== الشاشة الرئيسية ====================
@Composable
fun HomeScreen(
    userName: String,
    userEmail: String,
    userRole: String,
    favorites: List<MovieData>,
    onLogout: () -> Unit,
    onOpen: (MovieData) -> Unit
) {
    var drawerOpen by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf("all") }
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<MovieData>?>(null) }
    var searching by remember { mutableStateOf(false) }
    var serverCatalog by remember { mutableStateOf<List<MovieData>?>(null) }
    var loadingCatalog by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        serverCatalog = apiCatalog()
        loadingCatalog = false
    }

    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            searchResults = null
            searching = false
        } else {
            searching = true
            delay(600)
            val res = apiSearch(q)
            searchResults = res
            searching = false
        }
    }

    val fullCatalog = serverCatalog ?: catalogMovies()
    val baseList = when (section) {
        "movies" -> fullCatalog.filter { !it.isSeries }
        "series" -> fullCatalog.filter { it.isSeries }
        "fav" -> favorites
        else -> fullCatalog
    }
    val displayList = searchResults ?: baseList

    Box(Modifier.fillMaxSize().background(BG)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { drawerOpen = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "القائمة", tint = GOLD)
                }
                Text("AIFLIX", color = GOLD, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp))
                Text(sectionTitle(section), color = SUB, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                if (loadingCatalog || searching) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = GOLD, strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث عن فيلم أو مسلسل...", color = SUB) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SUB) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = "مسح", tint = SUB) }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = darkFieldColors()
            )
            Spacer(Modifier.height(6.dp))
            if (displayList.isEmpty() && !searching) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val msg = when {
                        searchResults != null -> "ما كو نتائج للبحث"
                        section == "fav" -> "ما عندك أفلام بالمفضلة بعد — اضغط على القلب بصفحة الفيلم"
                        else -> "ما كو محتوى حالياً"
                    }
                    Text(msg, color = SUB, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 4.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(displayList) { m ->
                        MovieCard(m, onClick = { onOpen(m) })
                    }
                }
            }
        }

        if (drawerOpen) {
            Box(Modifier.fillMaxSize().background(Color(0xCC000000)).clickable { drawerOpen = false })
            Column(
                Modifier.fillMaxHeight().width(300.dp).background(CARD).padding(20.dp)
            ) {
                Text("AIFLIX", color = GOLD, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text(userName, color = TEXT, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(userEmail, color = SUB, fontSize = 12.sp)
                if (userRole == "owner") {
                    Text("المالك ★", color = ACCENT, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
                DrawerItem("الرئيسية", section == "all") {
                    section = "all"
                    query = ""
                    drawerOpen = false
                }
                DrawerItem("الأفلام", section == "movies") {
                    section = "movies"
                    query = ""
                    drawerOpen = false
                }
                DrawerItem("المسلسلات", section == "series") {
                    section = "series"
                    query = ""
                    drawerOpen = false
                }
                DrawerItem("المفضلة (" + favorites.size + ")", section == "fav") {
                    section = "fav"
                    query = ""
                    drawerOpen = false
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFF2E2950))
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onLogout() }.padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = ACCENT)
                    Spacer(Modifier.width(8.dp))
                    Text("تسجيل الخروج", color = ACCENT, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== شاشة التفاصيل ====================
@Composable
fun DetailsScreen(
    movie: MovieData,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onPlay: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(BG).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(280.dp)) {
            WebImage(url = if (movie.backdrop.isNotEmpty()) movie.backdrop else movie.poster, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x33000000), BG))))
            IconButton(onClick = onBack, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
            }
            IconButton(onClick = onToggleFavorite, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "مفضلة",
                    tint = if (isFavorite) ACCENT else Color.White
                )
            }
        }
        Column(Modifier.padding(20.dp)) {
            Text(movie.displayTitle, color = TEXT, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            val meta = ArrayList<String>()
            if (movie.year.isNotEmpty()) meta.add(movie.year)
            meta.add(if (movie.isSeries) "مسلسل" else "فيلم")
            if (movie.rating.isNotEmpty()) meta.add("★ " + movie.rating)
            Text(meta.joinToString("  •  "), color = GOLD, fontSize = 14.sp)
            if (movie.genres.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(movie.genres, color = SUB, fontSize = 13.sp)
            }
            if (movie.cast.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("بطولة: " + movie.cast, color = SUB, fontSize = 13.sp)
            }
            if (movie.synopsis.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(movie.synopsis, color = Color(0xFFC9C4E0), fontSize = 14.sp, lineHeight = 22.sp)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onPlay,
                enabled = movie.streamUrl.isNotEmpty(),
                colors = goldButtonColors(),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (movie.streamUrl.isEmpty()) "غير متوفر حالياً" else "شاهد الآن", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ==================== شاشة المشغل ====================
@Composable
fun PlayerScreen(movie: MovieData, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var season by remember { mutableStateOf(1) }
    var episode by remember { mutableStateOf(1) }
    var seasons by remember { mutableStateOf<Map<Int, List<Int>>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var fullscreen by remember { mutableStateOf(false) }
    var pickDialog by remember { mutableStateOf("") }

    BackHandler(enabled = fullscreen) { fullscreen = false }

    val baseStream = if (movie.isSeries) movie.streamUrl.substringBefore("&season=").substringBefore("?season=") else movie.streamUrl
    val currentUrl = if (movie.isSeries) baseStream + "&season=" + season + "&episode=" + episode else movie.streamUrl

    val activity = ctx as? Activity
    DisposableEffect(fullscreen) {
        val w = activity?.window
        if (w != null) {
            val controller = WindowCompat.getInsetsController(w, w.decorView)
            if (fullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose { }
    }

    val webView = remember {
        val wv = WebView(ctx)
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.settings.mediaPlaybackRequiresUserGesture = false
        wv.settings.loadWithOverviewMode = true
        wv.settings.useWideViewPort = true
        wv.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        wv.settings.setSupportMultipleWindows(true)
        wv.settings.javaScriptCanOpenWindowsAutomatically = false
        var ua = wv.settings.userAgentString
        ua = ua.replace("; wv", "").replace("wv,", "")
        wv.settings.userAgentString = ua
        wv.setBackgroundColor(0xFF000000.toInt())
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val u = request?.url
                if (u == null) return false
                val s = u.toString()
                return !(s.startsWith("http://") || s.startsWith("https://"))
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                loading = false
            }
        }
        wv.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                return false
            }
        }
        wv
    }

    LaunchedEffect(currentUrl) {
        if (currentUrl.isNotEmpty()) {
            loading = true
            webView.loadUrl(currentUrl)
        }
    }

    if (movie.isSeries) {
        LaunchedEffect(movie.imdbId) {
            seasons = apiSeriesEpisodes(movie.imdbId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { webView.destroy() }
    }

    if (currentUrl.isEmpty()) {
        Column(
            Modifier.fillMaxSize().background(BG),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ما كو رابط بث متوفر لهذا العنوان حالياً", color = SUB, fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, colors = ButtonDefaults.outlinedButtonColors(contentColor = GOLD)) {
                Text("رجوع")
            }
        }
        return
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        if (!fullscreen) {
            Row(
                Modifier.fillMaxWidth().height(52.dp).background(CARD).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = TEXT)
                }
                Column(Modifier.weight(1f)) {
                    Text(movie.displayTitle, color = TEXT, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (movie.isSeries) {
                        Text("الموسم " + season + " — الحلقة " + episode, color = SUB, fontSize = 11.sp)
                    }
                }
                TextButton(onClick = { fullscreen = true }) {
                    Text("ملء الشاشة", color = GOLD, fontSize = 12.sp)
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center).size(34.dp), color = GOLD, strokeWidth = 3.dp)
            }
        }
        if (!fullscreen) {
            Column(
                Modifier.fillMaxWidth().background(CARD).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (movie.isSeries) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { pickDialog = "season" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GOLD)
                        ) {
                            Text("الموسم " + season, fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = { pickDialog = "episode" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GOLD)
                        ) {
                            Text("الحلقة " + episode, fontSize = 13.sp)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            loading = true
                            webView.loadUrl(currentUrl)
                        },
                        colors = goldButtonColors(),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("سيرفر البث المباشر", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            loading = true
                            webView.reload()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GOLD)
                    ) {
                        Text("تحديث 🔄", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (pickDialog == "season") {
        val seasonKeys = seasons?.keys?.sorted() ?: ArrayList<Int>()
        AlertDialog(
            onDismissRequest = { pickDialog = "" },
            title = { Text("اختر الموسم", color = TEXT) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (seasonKeys.isEmpty()) {
                        Text(
                            if (seasons == null) "جاري التحميل..." else "ما كو بيانات مواسم لهذا المسلسل",
                            color = SUB,
                            fontSize = 14.sp
                        )
                    }
                    for (s in seasonKeys) {
                        Text(
                            "الموسم " + s,
                            color = if (s == season) GOLD else TEXT,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth().clickable {
                                season = s
                                episode = 1
                                pickDialog = ""
                            }.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickDialog = "" }) { Text("إغلاق", color = GOLD) }
            },
            containerColor = CARD
        )
    }

    if (pickDialog == "episode") {
        val eps = seasons?.get(season) ?: (1..24).toList()
        AlertDialog(
            onDismissRequest = { pickDialog = "" },
            title = { Text("اختر الحلقة — الموسم " + season, color = TEXT) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    for (e in eps) {
                        Text(
                            "الحلقة " + e,
                            color = if (e == episode) GOLD else TEXT,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth().clickable {
                                episode = e
                                pickDialog = ""
                            }.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickDialog = "" }) { Text("إغلاق", color = GOLD) }
            },
            containerColor = CARD
        )
    }
}
