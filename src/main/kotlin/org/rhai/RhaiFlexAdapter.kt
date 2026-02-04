package org.rhai

import com.intellij.lexer.FlexAdapter

class RhaiFlexAdapter : FlexAdapter(RhaiLexer(null)) {
    companion object {
        @JvmStatic
        fun createLexer() = RhaiFlexAdapter()

        // For backwards compatibility, but should not be used for parsing
        @JvmStatic
        val INSTANCE: RhaiFlexAdapter
            get() = RhaiFlexAdapter()
    }
}
