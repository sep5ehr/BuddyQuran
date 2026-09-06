package com.example.persianquran.data.surah

import com.example.persianquran.data.model.Reciter
import com.example.persianquran.data.model.Surah

fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
fun Long.toPersianDigits(): String = this.toString().toPersianDigits()

fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder(this.length)
    for (ch in this) {
        if (ch in '0'..'9') {
            builder.append(persianDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

object QuranMetadata {

    val surahs: List<Surah> = listOf(
        Surah(1, "حمد", "الفاتحة", 7, "مکی", 5, 1, false),
        Surah(2, "بقره", "البقرة", 286, "مدنی", 87, 2),
        Surah(3, "آل‌عمران", "آل عمران", 200, "مدنی", 89, 50),
        Surah(4, "نساء", "النساء", 176, "مدنی", 92, 77),
        Surah(5, "مائده", "المائدة", 120, "مدنی", 112, 106),
        Surah(6, "انعام", "الأنعام", 165, "مکی", 55, 128),
        Surah(7, "اعراف", "الأعراف", 206, "مکی", 39, 151),
        Surah(8, "انفال", "الأنفال", 75, "مدنی", 88, 177),
        Surah(9, "توبه", "التوبة", 129, "مدنی", 113, 187, false),
        Surah(10, "یونس", "يونس", 109, "مکی", 51, 208),
        Surah(11, "هود", "هود", 123, "مکی", 52, 221),
        Surah(12, "یوسف", "يوسف", 111, "مکی", 53, 235),
        Surah(13, "رعد", "الرعد", 43, "مدنی", 96, 249),
        Surah(14, "ابراهیم", "ابراهيم", 52, "مکی", 72, 255),
        Surah(15, "حجر", "الحجر", 99, "مکی", 54, 262),
        Surah(16, "نحل", "النحل", 128, "مکی", 70, 267),
        Surah(17, "اسراء", "الإسراء", 111, "مکی", 50, 282),
        Surah(18, "کهف", "الكهف", 110, "مکی", 69, 293),
        Surah(19, "مریم", "مريم", 98, "مکی", 44, 305),
        Surah(20, "طه", "طه", 135, "مکی", 45, 312),
        Surah(21, "انبیاء", "الأنبياء", 112, "مکی", 73, 322),
        Surah(22, "حج", "الحج", 78, "مدنی", 103, 332),
        Surah(23, "مؤمنون", "المؤمنون", 118, "مکی", 74, 342),
        Surah(24, "نور", "النور", 64, "مدنی", 102, 350),
        Surah(25, "فرقان", "الفرقان", 77, "مکی", 42, 359),
        Surah(26, "شعراء", "الشعراء", 227, "مکی", 47, 367),
        Surah(27, "نمل", "النمل", 93, "مکی", 48, 377),
        Surah(28, "قصص", "القصص", 88, "مکی", 49, 385),
        Surah(29, "عنکبوت", "العنكبوت", 69, "مکی", 85, 396),
        Surah(30, "روم", "الروم", 60, "مکی", 84, 404),
        Surah(31, "لقمان", "لقمان", 34, "مکی", 57, 411),
        Surah(32, "سجده", "السجدة", 30, "مکی", 75, 415),
        Surah(33, "احزاب", "الأحزاب", 73, "مدنی", 90, 418),
        Surah(34, "سبأ", "سبإ", 54, "مکی", 58, 428),
        Surah(35, "فاطر", "فاطر", 45, "مکی", 43, 434),
        Surah(36, "یاسین", "يس", 83, "مکی", 41, 440),
        Surah(37, "صافات", "الصافات", 182, "مکی", 56, 446),
        Surah(38, "ص", "ص", 88, "مکی", 38, 453),
        Surah(39, "زمر", "الزمر", 75, "مکی", 59, 458),
        Surah(40, "غافر", "غافر", 85, "مکی", 60, 467),
        Surah(41, "فصلت", "فصلت", 54, "مکی", 61, 477),
        Surah(42, "شوری", "الشورى", 53, "مکی", 62, 483),
        Surah(43, "زخرف", "الزخرف", 89, "مکی", 63, 489),
        Surah(44, "دخان", "الدخان", 59, "مکی", 64, 496),
        Surah(45, "جاثیه", "الجاثية", 37, "مکی", 65, 499),
        Surah(46, "احقاف", "الأحقاف", 35, "مکی", 66, 502),
        Surah(47, "محمد", "محمد", 38, "مدنی", 95, 507),
        Surah(48, "فتح", "الفتح", 29, "مدنی", 111, 511),
        Surah(49, "حجرات", "الحجرات", 18, "مدنی", 106, 515),
        Surah(50, "ق", "ق", 45, "مکی", 34, 518),
        Surah(51, "ذاریات", "الذاريات", 60, "مکی", 67, 520),
        Surah(52, "طور", "الطور", 49, "مکی", 76, 523),
        Surah(53, "نجم", "النجم", 62, "مکی", 23, 526),
        Surah(54, "قمر", "القمر", 55, "مکی", 37, 528),
        Surah(55, "الرحمن", "الرحمن", 78, "مدنی", 97, 531),
        Surah(56, "واقعه", "الواقعة", 96, "مکی", 46, 534),
        Surah(57, "حدید", "الحديد", 29, "مدنی", 94, 537),
        Surah(58, "مجادله", "المجادلة", 22, "مدنی", 105, 542),
        Surah(59, "حشر", "الحشر", 24, "مدنی", 101, 545),
        Surah(60, "ممتحنه", "الممتحنة", 13, "مدنی", 91, 549),
        Surah(61, "صف", "الصف", 14, "مدنی", 109, 551),
        Surah(62, "جمعه", "الجمعة", 11, "مدنی", 110, 553),
        Surah(63, "منافقون", "المنافقون", 11, "مدنی", 104, 554),
        Surah(64, "تغابن", "التغابن", 18, "مدنی", 108, 556),
        Surah(65, "طلاق", "الطلاق", 12, "مدنی", 99, 558),
        Surah(66, "تحریم", "التحريم", 12, "مدنی", 107, 560),
        Surah(67, "ملک", "الملك", 30, "مکی", 77, 562),
        Surah(68, "قلم", "القلم", 52, "مکی", 2, 564),
        Surah(69, "حاقه", "الحاقة", 52, "مکی", 78, 566),
        Surah(70, "معارج", "المعارج", 44, "مکی", 79, 568),
        Surah(71, "نوح", "نوح", 28, "مکی", 71, 570),
        Surah(72, "جن", "الجن", 28, "مکی", 40, 572),
        Surah(73, "مزمل", "المزمل", 20, "مکی", 3, 574),
        Surah(74, "مدثر", "المدثر", 56, "مکی", 4, 575),
        Surah(75, "قیامت", "القيامة", 40, "مکی", 31, 577),
        Surah(76, "انسان", "الانسان", 31, "مدنی", 98, 578),
        Surah(77, "مرسلات", "المرسلات", 50, "مکی", 33, 580),
        Surah(78, "نبأ", "النبإ", 40, "مکی", 80, 582),
        Surah(79, "نازعات", "النازعات", 46, "مکی", 81, 583),
        Surah(80, "عبس", "عبس", 42, "مکی", 24, 585),
        Surah(81, "تکویر", "التكوير", 29, "مکی", 7, 586),
        Surah(82, "انفطار", "الإنفطار", 19, "مکی", 82, 587),
        Surah(83, "مطففین", "المطففين", 36, "مکی", 86, 587),
        Surah(84, "انشقاق", "الإنشقاق", 25, "مکی", 83, 589),
        Surah(85, "بروج", "البروج", 22, "مکی", 27, 590),
        Surah(86, "طارق", "الطارق", 17, "مکی", 36, 591),
        Surah(87, "اعلی", "الأعلى", 19, "مکی", 8, 591),
        Surah(88, "غاشیه", "الغاشية", 26, "مکی", 68, 592),
        Surah(89, "فجر", "الفجر", 30, "مکی", 10, 593),
        Surah(90, "بلد", "البلد", 20, "مکی", 35, 594),
        Surah(91, "شمس", "الشمس", 15, "مکی", 26, 595),
        Surah(92, "لیل", "الليل", 21, "مکی", 9, 595),
        Surah(93, "ضحی", "الضحى", 11, "مکی", 11, 596),
        Surah(94, "شرح", "الشرح", 8, "مکی", 12, 596),
        Surah(95, "تین", "التين", 8, "مکی", 28, 597),
        Surah(96, "علق", "العلق", 19, "مکی", 1, 597),
        Surah(97, "قدر", "القدر", 5, "مکی", 25, 598),
        Surah(98, "بینه", "البينة", 8, "مدنی", 100, 598),
        Surah(99, "زلزله", "الزلزلة", 8, "مدنی", 93, 599),
        Surah(100, "عادیات", "العاديات", 11, "مکی", 14, 599),
        Surah(101, "قارعه", "القارعة", 11, "مکی", 30, 600),
        Surah(102, "تکاثر", "التكاثر", 8, "مکی", 16, 600),
        Surah(103, "عصر", "العصر", 3, "مکی", 13, 601),
        Surah(104, "همزه", "الهمزة", 9, "مکی", 32, 601),
        Surah(105, "فیل", "الفيل", 5, "مکی", 19, 601),
        Surah(106, "قریش", "قريش", 4, "مکی", 29, 602),
        Surah(107, "ماعون", "الماعون", 7, "مکی", 17, 602),
        Surah(108, "کوثر", "الكوثر", 3, "مکی", 15, 602),
        Surah(109, "کافرون", "الكافرون", 6, "مکی", 18, 603),
        Surah(110, "نصر", "النصر", 3, "مدنی", 114, 603),
        Surah(111, "مسد", "المسد", 5, "مکی", 6, 603),
        Surah(112, "اخلاص", "الإخلاص", 4, "مکی", 22, 604),
        Surah(113, "فلق", "الفلق", 5, "مکی", 20, 604),
        Surah(114, "ناس", "الناس", 6, "مکی", 21, 604)
    )

    val reciters: List<Reciter> = listOf(
        Reciter(7, "مشاری راشد العفاسی", "مشاري راشد العفاسي", "ترتیل دلنشین", 7),
        Reciter(2, "عبدالباسط عبدالصمد", "عبد الباسط عبد الصمد", "ترتیل", 2),
        Reciter(1, "عبدالباسط عبدالصمد (مجوّد)", "عبد الباسط عبد الصمد", "مجوّد شاهکار", 1),
        Reciter(9, "محمد صدیق منشاوی", "محمد صديق المنشاوي", "ترتیل آرام", 9),
        Reciter(8, "محمد صدیق منشاوی (مجوّد)", "محمد صديق المنشاوي", "مجوّد خاشع", 8),
        Reciter(6, "محمود خلیل الحصری", "محمود خليل الحصري", "تجوید دقیق آموزشی", 6),
        Reciter(3, "عبدالرحمن السدیس", "عبد الرحمن السديس", "تلاوت حرم مکی", 3),
        Reciter(4, "ابوبکر الشاطری", "أبو بكر الشاطري", "تلاوت روان", 4),
        Reciter(5, "هانی الرفاعی", "هاني الرفاعي", "تلاوت محزون", 5)
    )

    fun getSurahById(id: Int): Surah? = surahs.find { it.id == id }

    fun getGlobalVerseIndex(surahId: Int, verseNumber: Int): Int {
        var count = 0
        for (i in 1 until surahId) {
            val s = getSurahById(i) ?: continue
            count += s.versesCount
        }
        return count + verseNumber
    }

    fun getSurahAndVerseFromGlobalIndex(globalIndex: Int): Pair<Int, Int> {
        var remaining = globalIndex.coerceIn(1, 6236)
        for (s in surahs) {
            if (remaining <= s.versesCount) {
                return Pair(s.id, remaining)
            }
            remaining -= s.versesCount
        }
        return Pair(114, 6)
    }

    fun getSurahByPage(page: Int): Surah {
        val clamped = page.coerceIn(1, 604)
        var matched = surahs.first()
        for (s in surahs) {
            if (s.startPage <= clamped) {
                matched = s
            } else {
                break
            }
        }
        return matched
    }

    fun calculateScheduleForPlan(
        planId: Long,
        method: com.example.persianquran.data.model.PlanMethod,
        totalDays: Int,
        startDateMillis: Long,
        startSurahId: Int = 1,
        startVerse: Int = 1,
        endSurahId: Int = 114,
        endVerse: Int = 6,
        startPageInput: Int = 1,
        endPageInput: Int = 604,
        dailyTarget: Int = 1
    ): List<com.example.persianquran.data.model.PlanDaySchedule> {
        val schedule = mutableListOf<com.example.persianquran.data.model.PlanDaySchedule>()
        val oneDayMillis = 24L * 60L * 60L * 1000L

        when (method) {
            com.example.persianquran.data.model.PlanMethod.PAGES -> {
                val sPage = startPageInput.coerceIn(1, 604)
                val ePage = endPageInput.coerceIn(sPage, 604)
                val totalPages = ePage - sPage + 1
                val target = if (dailyTarget > 0) dailyTarget else maxOf(1, (totalPages + totalDays - 1) / totalDays)
                val calculatedDays = (totalPages + target - 1) / target

                for (d in 1..calculatedDays) {
                    val dayStartPage = sPage + (d - 1) * target
                    val dayEndPage = minOf(ePage, dayStartPage + target - 1)
                    if (dayStartPage > ePage) break

                    val sSurah = getSurahByPage(dayStartPage)
                    val eSurah = getSurahByPage(dayEndPage)
                    val dateMillis = startDateMillis + (d - 1) * oneDayMillis
                    val dateFormatted = PersianDateHelper.formatPersianDate(dateMillis)

                    val desc = if (dayStartPage == dayEndPage) {
                        "صفحه ${dayStartPage.toPersianDigits()} (${sSurah.namePersian})"
                    } else {
                        "صفحات ${dayStartPage.toPersianDigits()} تا ${dayEndPage.toPersianDigits()} (${sSurah.namePersian})"
                    }

                    schedule.add(
                        com.example.persianquran.data.model.PlanDaySchedule(
                            planId = planId,
                            dayNumber = d,
                            dateMillis = dateMillis,
                            dateFormatted = dateFormatted,
                            startSurahId = sSurah.id,
                            startSurahName = sSurah.namePersian,
                            startVerse = 1,
                            endSurahId = eSurah.id,
                            endSurahName = eSurah.namePersian,
                            endVerse = eSurah.versesCount,
                            startPage = dayStartPage,
                            endPage = dayEndPage,
                            rangeDescription = desc,
                            isCompleted = false
                        )
                    )
                }
            }

            com.example.persianquran.data.model.PlanMethod.SURAHS -> {
                val sId = startSurahId.coerceIn(1, 114)
                val eId = endSurahId.coerceIn(sId, 114)
                val totalSurahs = eId - sId + 1
                val target = if (dailyTarget > 0) dailyTarget else maxOf(1, (totalSurahs + totalDays - 1) / totalDays)
                val calculatedDays = (totalSurahs + target - 1) / target

                for (d in 1..calculatedDays) {
                    val dayStartSurah = sId + (d - 1) * target
                    val dayEndSurah = minOf(eId, dayStartSurah + target - 1)
                    if (dayStartSurah > eId) break

                    val sSurah = getSurahById(dayStartSurah)!!
                    val eSurah = getSurahById(dayEndSurah)!!
                    val dateMillis = startDateMillis + (d - 1) * oneDayMillis
                    val dateFormatted = PersianDateHelper.formatPersianDate(dateMillis)

                    val desc = if (dayStartSurah == dayEndSurah) {
                        "سوره ${sSurah.namePersian} (کامل - ${sSurah.versesCount.toPersianDigits()} آیه)"
                    } else {
                        "از سوره ${sSurah.namePersian} تا سوره ${eSurah.namePersian}"
                    }

                    schedule.add(
                        com.example.persianquran.data.model.PlanDaySchedule(
                            planId = planId,
                            dayNumber = d,
                            dateMillis = dateMillis,
                            dateFormatted = dateFormatted,
                            startSurahId = sSurah.id,
                            startSurahName = sSurah.namePersian,
                            startVerse = 1,
                            endSurahId = eSurah.id,
                            endSurahName = eSurah.namePersian,
                            endVerse = eSurah.versesCount,
                            startPage = sSurah.startPage,
                            endPage = eSurah.startPage,
                            rangeDescription = desc,
                            isCompleted = false
                        )
                    )
                }
            }

            com.example.persianquran.data.model.PlanMethod.VERSES,
            com.example.persianquran.data.model.PlanMethod.RANGE -> {
                val globalStart = getGlobalVerseIndex(startSurahId, startVerse)
                val globalEnd = getGlobalVerseIndex(endSurahId, endVerse)
                val totalVersesCount = maxOf(1, globalEnd - globalStart + 1)
                val target = if (dailyTarget > 0) dailyTarget else maxOf(1, (totalVersesCount + totalDays - 1) / totalDays)
                val calculatedDays = (totalVersesCount + target - 1) / target

                for (d in 1..calculatedDays) {
                    val dayStartGlobal = globalStart + (d - 1) * target
                    val dayEndGlobal = minOf(globalEnd, dayStartGlobal + target - 1)
                    if (dayStartGlobal > globalEnd) break

                    val (curSId, curSVerse) = getSurahAndVerseFromGlobalIndex(dayStartGlobal)
                    val (curEId, curEVerse) = getSurahAndVerseFromGlobalIndex(dayEndGlobal)
                    val sSurah = getSurahById(curSId)!!
                    val eSurah = getSurahById(curEId)!!
                    val dateMillis = startDateMillis + (d - 1) * oneDayMillis
                    val dateFormatted = PersianDateHelper.formatPersianDate(dateMillis)

                    val desc = if (curSId == curEId) {
                        if (curSVerse == curEVerse) "سوره ${sSurah.namePersian}، آیه ${curSVerse.toPersianDigits()}"
                        else "سوره ${sSurah.namePersian}، آیات ${curSVerse.toPersianDigits()} تا ${curEVerse.toPersianDigits()}"
                    } else {
                        "سوره ${sSurah.namePersian} آیه ${curSVerse.toPersianDigits()} تا ${eSurah.namePersian} آیه ${curEVerse.toPersianDigits()}"
                    }

                    schedule.add(
                        com.example.persianquran.data.model.PlanDaySchedule(
                            planId = planId,
                            dayNumber = d,
                            dateMillis = dateMillis,
                            dateFormatted = dateFormatted,
                            startSurahId = sSurah.id,
                            startSurahName = sSurah.namePersian,
                            startVerse = curSVerse,
                            endSurahId = eSurah.id,
                            endSurahName = eSurah.namePersian,
                            endVerse = curEVerse,
                            startPage = sSurah.startPage,
                            endPage = eSurah.startPage,
                            rangeDescription = desc,
                            isCompleted = false
                        )
                    )
                }
            }
        }

        return schedule
    }

    fun searchSurahs(query: String): List<Surah> {
        if (query.isBlank()) return surahs
        val trimmed = query.trim()
        val num = trimmed.toIntOrNull()
        if (num != null && num in 1..114) {
            return surahs.filter { it.id == num }
        }
        return surahs.filter {
            it.namePersian.contains(trimmed, ignoreCase = true) ||
            it.nameArabic.contains(trimmed, ignoreCase = true) ||
            it.id.toPersianDigits().contains(trimmed)
        }
    }
}
