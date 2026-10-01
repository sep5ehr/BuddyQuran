package com.example

import com.example.persianquran.data.model.PlanMethod
import com.example.persianquran.data.surah.QuranMetadata
import com.example.persianquran.data.surah.QuranPageMetadata
import com.example.persianquran.data.surah.toPersianDigits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranMetadataTest {

    @Test
    fun testSurahCountIs114() {
        val surahs = QuranMetadata.surahs
        assertEquals(114, surahs.size)
    }

    @Test
    fun testSurahIdsAreSequential() {
        val surahs = QuranMetadata.surahs
        surahs.forEachIndexed { index, surah ->
            assertEquals(index + 1, surah.id)
        }
    }

    @Test
    fun testTotalVersesCountIs6236() {
        val totalVerses = QuranMetadata.surahs.sumOf { it.versesCount }
        assertEquals(6236, totalVerses)
    }

    @Test
    fun testSurahAlFatihaDetails() {
        val fatiha = QuranMetadata.getSurahById(1)
        assertNotNull(fatiha)
        assertEquals("حمد", fatiha!!.namePersian)
        assertEquals("الفاتحة", fatiha.nameArabic)
        assertEquals(7, fatiha.versesCount)
        assertEquals("مکی", fatiha.revelationType)
    }

    @Test
    fun testSurahAnNasDetails() {
        val nas = QuranMetadata.getSurahById(114)
        assertNotNull(nas)
        assertEquals("ناس", nas!!.namePersian)
        assertEquals("الناس", nas.nameArabic)
        assertEquals(6, nas.versesCount)
    }

    @Test
    fun testPersianNumeralConversion() {
        assertEquals("۰۱۲۳۴۵۶۷۸۹", "0123456789".toPersianDigits())
        assertEquals("۱۱۴", 114.toPersianDigits())
        assertEquals("۶۲۳۶", 6236.toPersianDigits())
    }

    @Test
    fun testSearchSurahs() {
        val resultsFatiha = QuranMetadata.searchSurahs("حمد")
        assertTrue(resultsFatiha.any { it.id == 1 })

        val resultsBaqarah = QuranMetadata.searchSurahs("البقرة")
        assertTrue(resultsBaqarah.any { it.id == 2 })

        val resultsByNumber = QuranMetadata.searchSurahs("112")
        assertTrue(resultsByNumber.any { it.id == 112 })
    }

    @Test
    fun testRecitersList() {
        val reciters = QuranMetadata.reciters
        assertTrue(reciters.isNotEmpty())
        assertTrue(reciters.any { it.id == 7 && it.namePersian.contains("العفاسی") })
    }

    @Test
    fun testPageBoundariesCountIs604() {
        assertEquals(604, QuranPageMetadata.TOTAL_PAGES)
        for (i in 1..604) {
            val p = QuranPageMetadata.getPageBoundary(i)
            assertEquals(i, p.pageNumber)
            assertTrue(p.startSurahId in 1..114)
            assertTrue(p.endSurahId in 1..114)
            assertTrue(p.startVerse >= 1)
            assertTrue(p.endVerse >= 1)
        }
    }

    @Test
    fun testExactVersesOnFirstFivePages() {
        // Page 1: Surah 1:1 - 1:7
        val p1 = QuranPageMetadata.getPageBoundary(1)
        assertEquals(1, p1.startSurahId)
        assertEquals(1, p1.startVerse)
        assertEquals(1, p1.endSurahId)
        assertEquals(7, p1.endVerse)

        // Page 2: Surah 2:1 - 2:5
        val p2 = QuranPageMetadata.getPageBoundary(2)
        assertEquals(2, p2.startSurahId)
        assertEquals(1, p2.startVerse)
        assertEquals(2, p2.endSurahId)
        assertEquals(5, p2.endVerse)

        // Page 3: Surah 2:6 - 2:16 (MUST NOT open at 2:1!)
        val p3 = QuranPageMetadata.getPageBoundary(3)
        assertEquals(2, p3.startSurahId)
        assertEquals(6, p3.startVerse)
        assertEquals(2, p3.endSurahId)
        assertEquals(16, p3.endVerse)

        // Page 4: Surah 2:17 - 2:24
        val p4 = QuranPageMetadata.getPageBoundary(4)
        assertEquals(2, p4.startSurahId)
        assertEquals(17, p4.startVerse)
        assertEquals(2, p4.endSurahId)
        assertEquals(24, p4.endVerse)

        // Page 5: Surah 2:25 - 2:29
        val p5 = QuranPageMetadata.getPageBoundary(5)
        assertEquals(2, p5.startSurahId)
        assertEquals(25, p5.startVerse)
        assertEquals(2, p5.endSurahId)
        assertEquals(29, p5.endVerse)
    }

    @Test
    fun testWrapAroundCalculationAndSequence() {
        // 1 to 604: 604 pages
        assertEquals(604, QuranPageMetadata.calculateTotalPages(1, 604))

        // 302 to 301: (604 - 302 + 1) + 301 = 303 + 301 = 604 pages
        assertEquals(604, QuranPageMetadata.calculateTotalPages(302, 301))

        val seq = QuranPageMetadata.generatePageSequence(302, 301)
        assertEquals(604, seq.size)
        assertEquals(302, seq.first())
        assertEquals(604, seq[302]) // 303rd item is 604
        assertEquals(1, seq[303])   // 304th item wraps to 1
        assertEquals(301, seq.last())

        // No duplicates
        assertEquals(604, seq.toSet().size)
    }

    @Test
    fun testPageScheduleGenerationWithWrapAroundAndPartialFinalDay() {
        val schedule = QuranMetadata.calculateScheduleForPlan(
            planId = 999L,
            method = PlanMethod.PAGES,
            totalDays = 0,
            startDateMillis = 1000000L,
            startPageInput = 302,
            endPageInput = 301,
            dailyTarget = 20
        )

        // 604 / 20 = 31 days (30 days of 20, 1 day of 4)
        assertEquals(31, schedule.size)

        // Day 1
        val day1 = schedule[0]
        assertEquals(1, day1.dayNumber)
        assertEquals(302, day1.startPage)
        assertEquals(321, day1.endPage)
        val p302 = QuranPageMetadata.getPageBoundary(302)
        assertEquals(p302.startSurahId, day1.startSurahId)
        assertEquals(p302.startVerse, day1.startVerse)

        // Day 31 (final day should have remainder: 4 pages)
        val day31 = schedule.last()
        assertEquals(31, day31.dayNumber)
        assertEquals(298, day31.startPage)
        assertEquals(301, day31.endPage)
        assertEquals(4, (day31.endPage ?: 0) - (day31.startPage ?: 0) + 1)
        val p298 = QuranPageMetadata.getPageBoundary(298)
        assertEquals(p298.startSurahId, day31.startSurahId)
        assertEquals(p298.startVerse, day31.startVerse)
    }

    @Test
    fun testFull604PagesContinuity() {
        // First page
        val firstPage = QuranPageMetadata.getPageBoundary(1)
        assertEquals(1, firstPage.startSurahId)
        assertEquals(1, firstPage.startVerse)

        // Last page
        val lastPage = QuranPageMetadata.getPageBoundary(604)
        assertEquals(114, lastPage.endSurahId)
        assertEquals(6, lastPage.endVerse)

        // Validate continuity across all 604 pages
        for (page in 1..603) {
            val curr = QuranPageMetadata.getPageBoundary(page)
            val next = QuranPageMetadata.getPageBoundary(page + 1)
            val currEndSurah = QuranMetadata.getSurahById(curr.endSurahId)
            assertNotNull("Surah ${curr.endSurahId} must exist", currEndSurah)

            if (curr.endVerse == currEndSurah!!.versesCount) {
                // Surah ended on this page -> next page must start at next surah verse 1
                assertEquals("Page ${page+1} should start at surah ${curr.endSurahId + 1}", curr.endSurahId + 1, next.startSurahId)
                assertEquals("Page ${page+1} should start at verse 1", 1, next.startVerse)
            } else {
                // Surah continues onto next page
                assertEquals("Page ${page+1} should continue surah ${curr.endSurahId}", curr.endSurahId, next.startSurahId)
                assertEquals("Page ${page+1} should start at verse ${curr.endVerse + 1}", curr.endVerse + 1, next.startVerse)
            }
        }
    }
}
