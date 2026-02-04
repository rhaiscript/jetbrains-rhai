package org.rhai.lexer

import com.intellij.lexer.LayeredLexer
import org.rhai.RhaiFlexAdapter

class RhaiHighlightingLexer : LayeredLexer(RhaiFlexAdapter.createLexer())
