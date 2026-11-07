package com.example.catchthedot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

// ================== شاشة التشغيل ==================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatchTheDotGame()
        }
    }
}

// حالات اللعبة: القائمة / يلعب / انتهت
enum class GameState { MENU, PLAYING, GAME_OVER }

// ================== اللعبة الرئيسية ==================
@Composable
fun CatchTheDotGame() {
    var gameState by remember { mutableStateOf(GameState.MENU) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }

    // المؤقت: ينقص ثانية كل ثانية، وإذا خلص الوقت تنتهي اللعبة
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            if (score > bestScore) bestScore = score
            gameState = GameState.GAME_OVER
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1E1B2E)) {
            when (gameState) {
                GameState.MENU -> MenuScreen(bestScore) {
                    score = 0
                    timeLeft = 30
                    gameState = GameState.PLAYING
                }
                GameState.PLAYING -> GameScreen(
                    score = score,
                    timeLeft = timeLeft,
                    onDotCaught = { score++ }
                )
                GameState.GAME_OVER -> GameOverScreen(
                    score = score,
                    bestScore = bestScore,
                    onPlayAgain = {
                        score = 0
                        timeLeft = 30
                        gameState = GameState.PLAYING
                    }
                )
            }
        }
    }
}

// ================== شاشة البداية ==================
@Composable
fun MenuScreen(bestScore: Int, onStartGame: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎯", fontSize = 80.sp)
        Text(
            "اصطاد النقطة",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "المس النقطة قبل ما يخلص الوقت!\nكل ما تزيد نتيجتك، النقطة تصغر وتصعب أكثر 🔥",
            color = Color(0xFFB8B5CC),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("أفضل نتيجة: $bestScore 🏆", color = Color(0xFFFFD93D), fontSize = 20.sp)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ابدأ اللعب 🚀", fontSize = 22.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

// ================== شاشة اللعب ==================
@Composable
fun GameScreen(score: Int, timeLeft: Int, onDotCaught: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // شريط الأعلى: النتيجة والوقت المتبقي
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("النتيجة: $score", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "⏱ $timeLeft",
                color = if (timeLeft <= 5) Color(0xFFFF6B6B) else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ساحة اللعب
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // حجم النقطة يصغر كل ما تزيد النتيجة (أصغر حجم 30)
            val dotSizeDp = maxOf(30, 70 - score).dp

            // مكان النقطة عشوائي من أول لحظة
            var dotX by remember {
                mutableFloatStateOf(Random.nextFloat() * (maxWidth.value - dotSizeDp.value).coerceAtLeast(0f))
            }
            var dotY by remember {
                mutableFloatStateOf(Random.nextFloat() * (maxHeight.value - dotSizeDp.value).coerceAtLeast(0f))
            }

            // تحريك النقطة لمكان جديد عشوائي
            fun moveDot() {
                dotX = Random.nextFloat() * (maxWidth.value - dotSizeDp.value).coerceAtLeast(0f)
                dotY = Random.nextFloat() * (maxHeight.value - dotSizeDp.value).coerceAtLeast(0f)
            }

            // حدود ساحة اللعب (شكل دائري خفيف)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF28243F))
            )

            // النقطة نفسها
            Box(
                modifier = Modifier
                    .offset { IntOffset(dotX.roundToInt(), dotY.roundToInt()) }
                    .size(dotSizeDp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF6B6B), Color(0xFFFFD93D))
                        )
                    )
                    .clickable {
                        onDotCaught()
                        moveDot()
                    }
            )
        }
    }
}

// ================== شاشة النهاية ==================
@Composable
fun GameOverScreen(score: Int, bestScore: Int, onPlayAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (score >= bestScore && score > 0) "🏆" else "😅", fontSize = 80.sp)
        Text(
            "انتهى الوقت!",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("نتيجتك: $score", color = Color(0xFFFFD93D), fontSize = 28.sp)
        Text("أفضل نتيجة: $bestScore", color = Color(0xFFB8B5CC), fontSize = 20.sp)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onPlayAgain,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("العب مرة ثانية 🔄", fontSize = 22.sp, modifier = Modifier.padding(8.dp))
        }
    }
}
