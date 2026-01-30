package org.rhai

import com.intellij.psi.tree.IElementType
import org.rhai.RhaiLanguage

class RhaiElementType(debugName: String) : IElementType(debugName, RhaiLanguage.INSTANCE)
