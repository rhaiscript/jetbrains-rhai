package org.rhai

import com.intellij.lexer.FlexAdapter
import org.rhai.lang.RhaiLanguage

class RhaiFlexAdapter : FlexAdapter(RhaiLexer(null)) {
    companion object {
        @JvmStatic
        val INSTANCE = RhaiFlexAdapter()
    }
}
