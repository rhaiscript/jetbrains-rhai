package org.rhai.psi

import com.intellij.psi.tree.IElementType
import org.rhai.lang.RhaiLanguage

class RhaiTokenType(debugName: String) : IElementType(debugName, RhaiLanguage.INSTANCE) {
    override fun toString() = "RhaiTokenType.${super.toString()}"
}
