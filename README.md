# 🎯 لعبة اصطاد النقطة (Catch The Dot)

لعبة أندرويد بسيطة مبنية بـ **Kotlin + Jetpack Compose**.
تظهر نقطة بأماكن عشوائية، لازم تصطادها بلمسة قبل ما يخلص الوقت (30 ثانية).
كل ما تزيد نتيجتك، النقطة تصغر وتصعب أكثر! 🔥

## 📱 تنزيل الـ APK مباشرة على تلفونك (بدون كمبيوتر)

1. روح لصفحة **Releases** بالمستودع
2. نزّل ملف **app-debug.apk**
3. افتح الملف من التلفون، وإذا سألك "مسموح التثبيت من مصادر غير معروفة؟" اضغط السماح
4. ثبّت واستمتع! 🎉

## 💻 أو افتح المشروع بـ Android Studio

1. نزّل وثبّت Android Studio من: https://developer.android.com/studio
2. نزّل المشروع (Code ← Download ZIP) وفك الضغط
3. افتح Android Studio ← **Open** ← اختر مجلد المشروع ← Trust
4. انتظر أول Build من 5-10 دقائق ثم اضغط **Run ▶**

## 📂 أهم الملفات

- **app/src/main/java/com/example/catchthedot/MainActivity.kt** ← كود اللعبة كله هنا
- **app/src/main/AndroidManifest.xml** ← معلومات التطبيق
- **app/build.gradle.kts** ← إعدادات البناء والمكتبات

## ✏️ عدلات سهلة تجربها

- مدة اللعبة: دوّر على "timeLeft = 30" في MainActivity.kt وغيّر الرقم
- ألوان النقطة: دوّر على Color(0xFFFF6B6B) و Color(0xFFFFD93D)
- أصغر حجم للنقطة: دوّر على maxOf(30, 70 - score)
