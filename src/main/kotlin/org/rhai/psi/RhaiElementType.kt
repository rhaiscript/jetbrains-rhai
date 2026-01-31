package org.rhai.psi

import com.intellij.psi.tree.IElementType
import org.rhai.lang.RhaiLanguage

class RhaiElementType(debugName: String) : IElementType(debugName, RhaiLanguage.INSTANCE)
