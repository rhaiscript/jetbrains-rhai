package org.rhai

import com.intellij.lexer.FlexAdapter
import org.rhai.RhaiLexer

class RhaiFlexAdapter : FlexAdapter(RhaiLexer(null))
