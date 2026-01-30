package org.rhai

import com.intellij.lexer.FlexAdapter

class RsLexer : FlexAdapter(RhaiLexer(null))