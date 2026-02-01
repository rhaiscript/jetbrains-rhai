package org.rhai

import com.intellij.lexer.FlexAdapter

class RhaiFlexAdapter : FlexAdapter(RhaiLexer(null))
