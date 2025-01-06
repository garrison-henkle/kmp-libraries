package dev.henkle.stytch.utils.ext

import android.content.SharedPreferences

fun SharedPreferences.editAndCommit(block: SharedPreferences.Editor.() -> Unit) {
    edit().apply {
        block()
        commit()
    }
}
