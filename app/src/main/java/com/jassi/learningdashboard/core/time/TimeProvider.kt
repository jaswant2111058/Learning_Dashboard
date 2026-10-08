package com.jassi.learningdashboard.core.time

fun interface TimeProvider {
    fun nowMillis(): Long
}
