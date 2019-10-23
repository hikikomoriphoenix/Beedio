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

package marabillas.loremar.beedio.extractors

fun Any.plus(other: Any): Any {
    return if (this is Int && other is Int)
        this + other
    else if (this is String && other is String)
        this + other
    else if (this is Double && other is Double)
        this + other
    else if (this is Long && other is Long)
        this + other
    else if (this is Float && other is Float)
        this + other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.minus(other: Any): Any {
    return if (this is Int && other is Int)
        this - other
    else if (this is Double && other is Double)
        this - other
    else if (this is Long && other is Long)
        this - other
    else if (this is Float && other is Float)
        this - other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.times(other: Any): Any {
    return if (this is Int && other is Int)
        this * other
    else if (this is Double && other is Double)
        this * other
    else if (this is Long && other is Long)
        this * other
    else if (this is Float && other is Float)
        this * other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.div(other: Any): Any {
    return if (this is Int && other is Int)
        this / other
    else if (this is Double && other is Double)
        this / other
    else if (this is Long && other is Long)
        this / other
    else if (this is Float && other is Float)
        this / other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.rem(other: Any): Any {
    return if (this is Int && other is Int) {
        this % other
    }
    else if (this is Double && other is Double)
        this % other
    else if (this is Long && other is Long)
        this % other
    else if (this is Float && other is Float)
        this % other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.or(other: Any): Any {
    return if (this is Int && other is Int)
        this or other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.xor(other: Any): Any {
    return if (this is Int && other is Int)
        this xor other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.and(other: Any): Any {
    return if (this is Int && other is Int)
        this and other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.shr(other: Any): Any {
    return if (this is Int && other is Int)
        this shr other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}

fun Any.shl(other: Any): Any {
    return if (this is Int && other is Int)
        this shl other
    else
        throw IllegalArgumentException("Operands not suitable for operation")
}