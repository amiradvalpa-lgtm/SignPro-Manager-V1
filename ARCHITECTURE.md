# مستندات معماری و تصمیمات مهندسی (Architecture Decision Records - ADR)
## پروژه مدیریت کارگاه تابلوسازی آلپا (Alpa Signage Manager)

این سند مرجع رسمی تصمیمات مهندسی، استانداردها، ساختار لایه‌ها و قوانین پایداری پروژه است و در هر فاز توسعه توسط دستیار ارشد به‌روزرسانی می‌شود.

---

### ۱. اصول و استانداردهای طراحی (Core Principles)
- **معماری:** الگو MVVM (Model-View-ViewModel) به همراه Clean Architecture در تفکیک Data Layer، Domain و Presentation.
- **تکنولوژی هسته:** زبان Kotlin و رابط کاربری ۱۰۰٪ مبتنی بر Jetpack Compose و Material Design 3.
- **پایداری داده‌ها (Data Persistence):** پایگاه داده محلی Room با روابط کلید خارجی (Foreign Keys)، نمایه (Indexing) بهینه، و مدل Soft Delete برای حفظ یکپارچگی داده‌ها.
- **سازگاری تقویم و زبان:** پشتیبانی بومی و دقیق از گاه‌شماری هجری شمسی (Jalali) و جهت‌بندی راست‌به‌چپ (RTL) در تمامی فرم‌ها، تاریخ‌انتخاب‌کن‌ها و گزارشات.
- **سازگاری سخت‌افزاری و اندروید:** پشتیبانی پایدار از اندروید ۷.۰ (API 24) تا جدیدترین نسخه‌های اندروید (API 36+).

---

### ۲. تصمیمات مهندسی کلیدی (ADRs)

#### تصمیم ADR-001: سازگاری تم و لایه ریشه اکتیویتی (AppCompat & Biometrics Compatibility)
- **وضعیت:** تصویب شده
- **مسئله:** استفاده از `FragmentActivity` و کتابخانه `androidx.biometric` در ترکیب با تم خام سیستم (`android:Theme.Material.Light.NoActionBar`) در لحظه اجرای برنامه به خطای `IllegalStateException: You need to use a Theme.AppCompat theme` و کرش در ثانیه اول آغاز به کار منجر می‌شد.
- **تصمیم:** والد تم برنامه در `themes.xml` به `Theme.AppCompat.DayNight.NoActionBar` تغییر یافت تا با تمامی کامپوننت‌های فرگمنت، دیالوگ‌ها و بیومتریک در تمامی نسخه‌های اندروید سازگاری ۱۰۰٪ داشته باشد.

#### تصمیم ADR-002: عدم اتکا به java.time در سطوح پایه API بدون Desugaring
- **وضعیت:** تصویب شده
- **مسئله:** کلاس `java.time.LocalDate` فقط در اندروید ۸ به بالا (API 26+) پشتیبانی می‌شود. استفاده مستقیم از آن در پروژه با `minSdk = 24` بدون Desugaring باعث بروز خطای مرگبار `NoClassDefFoundError: java.time.LocalDate` روی دستگاه‌های قدیمی‌تر می‌شود.
- **تصمیم:** ماژول گاه‌شماری جلالی (`JalaliDate`) از کلاس فوق‌العاده پایدار `java.util.Calendar` استفاده می‌کند تا روی تمامی دستگاه‌های اندروید از API 24 به بالا بدون نیاز به کتابخانه‌های اضافی کار کند.

#### تصمیم ADR-003: سازوکار سراسری ثبت خطا و صفحه کرش (Crash Reporting & Recovery)
- **وضعیت:** تصویب شده
- **مسئله:** کرش‌های اولیه بدون گزارش فنی، شناسایی ریشه مشکل در دستگاه کاربر را ناممکن می‌سازد.
- **تصمیم:**
  1. پیاده‌سازی کلاس سفارشی `Application` (`AlpaApplication`) و فعال‌سازی `Thread.setDefaultUncaughtExceptionHandler` در اولین میلی‌ثانیه قبل از هر اکتیویتی و دیتابیس.
  2. ذخیره خودکار متن کامل خطا، ردپای پشته (Stack Trace)، مشخصات سخت‌افزاری و نسخه اندروید در فایل `crash_log.txt` در حافظه عمومی و پوشه اختصاصی برنامه (`getExternalFilesDir` و پوشه دانلود عمومی).
  3. هدایت کاربر به `CrashReportActivity` جهت جلوگیری از بسته شدن ناگهانی برنامه و فراهم کردن دکمه‌های «کپی گزارش خطا» و «ارسال لاگ» برای انتقال سریع به توسعه‌دهنده.

#### تصمیم ADR-004: رفع باگ چرخه مقداردهی اولیه استاتیک کاتلین (Sealed Class & Companion Object Deadlock)
- **وضعیت:** تصویب شده
- **مسئله:** در کاتلین، قرار دادن اشیاء فرزندی یک کلاس Sealed در لیست ثابت ایستا در `companion object` همان کلاس والد (`val bottomNavItems = listOf(...)`) به دلیل اجرای متد `<clinit>` پیش از اتمام مقداردهی فرزندان، باعث ارجاع `null` به آبجکت‌ها و بروز خطای `NullPointerException: Attempt to invoke virtual method 'Screen.getRoute()' on a null object reference` در لحظه اجرای اولیه و ترسیم NavigationBar می‌شد.
- **تصمیم:** تعریف `bottomNavItems` به عنوان Property با Getter پویا (`get() = listOf(...)`) تغییر یافت و فیلتر ایمنی `filterNotNull()` در حلقه ناوبری اضافه شد تا از لود ۱۰۰٪ امن اشیاء در زمان فراخوانی اطمینان حاصل شود.

---

### ۳. ساختار پکیج‌ها و وظایف
- `com.example.AlpaApplication`: نقطه ورود و لایه ثبت خطای سراسری اپلیکیشن.
- `com.example.MainActivity`: اکتیویتی اصلی برنامه میزبان کامپوز و سیستم بارها.
- `com.example.ui.screens.crash.CrashReportActivity`: صفحه اختصاصی نمایش و اشتراک‌گذاری کرش در صورت بروز خطای ناشناخته.
- `com.example.data.local`: انتیتی‌ها، دیتابیس Room و نمونه داده‌های اولیه تابلوسازی.
- `com.example.util`: ابزارهای کمکی (تبدیل تاریخ جلالی، تولید PDF فاکتور، پشتیبان‌گیری، رمزنگاری و ثبت کرش).
