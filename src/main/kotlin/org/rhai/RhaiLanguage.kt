package org.rhai

import com.intellij.lang.Language

class RhaiLanguage : Language("Rhai") {
    companion object {
        @JvmStatic
        val INSTANCE = RhaiLanguage()
    }

    override fun getDisplayName(): String = "Rhai script"
    override fun isCaseSensitive(): Boolean = true

}
