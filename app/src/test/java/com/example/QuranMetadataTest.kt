package com.example

import com.example.persianquran.data.surah.QuranMetadata
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
}
