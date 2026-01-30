package org.rhai

import com.intellij.psi.tree.IElementType
import org.rhai.RhaiLanguage

class RhaiTokenType(debugName: String) : IElementType(debugName, RhaiLanguage.INSTANCE) {
    override fun toString() = "RhaiTokenType.${super.toString()}"
}
