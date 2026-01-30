package org.rhai

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class RhaiFileType : LanguageFileType(RhaiLanguage.INSTANCE) {
    override fun getName(): String {
        return "Rhai File"
    }

    override fun getDescription(): String {
        return "Rhai language file"
    }

    override fun getDefaultExtension(): String {
        return "rhai"
    }

    override fun getIcon(): Icon = RhaiIcons.FILE

    companion object {
        @JvmStatic
        val INSTANCE = RhaiFileType()
    }
}
