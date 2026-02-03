package org.rhai.features

import com.intellij.codeInsight.editorActions.MultiCharQuoteHandler
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.psi.tree.TokenSet
import org.rhai.RhaiTypes

class RhaiQuoteHandler : SimpleTokenSetQuoteHandler(
    TokenSet.create(
        RhaiTypes.STRING_LITERAL,
        RhaiTypes.CHAR_LITERAL,
        RhaiTypes.RAW_STRING_LITERAL,
        RhaiTypes.MULTILINE_STRING_LITERAL,
        RhaiTypes.INTERPOLATED_START,
        RhaiTypes.INTERPOLATED_TEXT,
        RhaiTypes.INTERPOLATED_END
    )
), MultiCharQuoteHandler {

    override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType

        if (tokenType == RhaiTypes.STRING_LITERAL ||
            tokenType == RhaiTypes.CHAR_LITERAL ||
            tokenType == RhaiTypes.INTERPOLATED_START) {
            val start = iterator.start
            return offset == start
        }

        return false
    }

    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType

        if (tokenType == RhaiTypes.STRING_LITERAL ||
            tokenType == RhaiTypes.CHAR_LITERAL ||
            tokenType == RhaiTypes.INTERPOLATED_END) {
            val end = iterator.end
            return offset == end - 1
        }

        return false
    }

    override fun getClosingQuote(iterator: HighlighterIterator, offset: Int): CharSequence? {
        val document = iterator.document ?: return null

        if (offset >= document.textLength) return null

        val char = document.charsSequence[offset]

        return when (char) {
            '"' -> "\""
            '\'' -> "'"
            '`' -> "`"
            else -> null
        }
    }

    override fun hasNonClosedLiteral(editor: Editor, iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType ?: return false

        // Check for unclosed strings
        if (tokenType == RhaiTypes.STRING_LITERAL ||
            tokenType == RhaiTypes.CHAR_LITERAL ||
            tokenType == RhaiTypes.INTERPOLATED_START ||
            tokenType == RhaiTypes.INTERPOLATED_TEXT) {

            val document = editor.document
            val text = document.charsSequence
            val start = iterator.start
            val end = iterator.end

            if (end > start && end <= text.length) {
                val tokenText = text.subSequence(start, end)

                // String literal check
                if (tokenType == RhaiTypes.STRING_LITERAL) {
                    return !tokenText.endsWith("\"") || tokenText.length < 2
                }

                // Char literal check
                if (tokenType == RhaiTypes.CHAR_LITERAL) {
                    return !tokenText.endsWith("'") || tokenText.length < 2
                }
            }
        }

        return false
    }
}
