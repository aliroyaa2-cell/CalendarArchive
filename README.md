# CalendarArchive

تطبيق Android بسيط لأرشفة التقويم وعرض الأحداث من 2000 إلى المستقبل.

## الميزات
- ✅ مزامنة من Google / Samsung / Outlook / محلي
- ✅ مدى ثابت: 1 يناير 2000 → اليوم + 5 سنين
- ✅ تبويبين: 📅 القادمة / 🕐 السابقة
- ✅ بحث في العنوان والوصف والموقع
- ✅ ضغط على حدث → يفتح في Google Calendar (للتعديل)
- ✅ إدارة المصادر (تفعيل/تعطيل التقاويم)
- ❌ بدون منبهات / إشعارات / خدمة في الخلفية

## البناء
GitHub Actions يبني APK تلقائياً عند كل push على branch `main`.

النتيجة في **Releases** كـ `CalendarArchive Build #N`.

## التشغيل
- Min SDK: 26 (Android 8.0)
- Target SDK: 35 (Android 15)
- اختُبر على Samsung Galaxy S24 Ultra (Android 16, One UI 8)

## الصلاحيات
- `READ_CALENDAR` — قراءة التقويم فقط (لا يكتب ولا يعدّل)
