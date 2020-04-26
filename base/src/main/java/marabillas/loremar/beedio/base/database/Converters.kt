/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.base.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.ByteArrayOutputStream

object HistoryItemConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun toZonedDateTime(value: String): ZonedDateTime = formatter.parse(value, ZonedDateTime::from)
            .withZoneSameInstant(ZoneId.systemDefault())

    @TypeConverter
    @JvmStatic
    fun fromZonedDateTime(date: ZonedDateTime): String = formatter.format(date)

    @TypeConverter
    @JvmStatic
    fun byteArrayFrom(bitmap: Bitmap?): ByteArray? {
        val bytesOutStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bytesOutStream)
        return bytesOutStream.toByteArray()
    }

    @TypeConverter
    @JvmStatic
    fun bitmapFrom(bytes: ByteArray?): Bitmap? {
        return if (bytes != null)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        else
            null
    }
}