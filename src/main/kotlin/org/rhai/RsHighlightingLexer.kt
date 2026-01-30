package org.rhai

import com.intellij.lexer.LayeredLexer
import org.rhai.RsLexer

class RsHighlightingLexer : LayeredLexer(RsLexer())
