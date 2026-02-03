package org.rhai.features

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.intellij.psi.util.elementType
import org.rhai.RhaiTypes

class RhaiSpellcheckingStrategy : SpellcheckingStrategy() {

    override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
        if (element == null) return EMPTY_TOKENIZER

        return when (element.elementType) {
            // Check spelling in comments
            RhaiTypes.LINE_COMMENT,
            RhaiTypes.BLOCK_COMMENT,
            RhaiTypes.DOC_COMMENT,
            RhaiTypes.DOC_LINE -> TEXT_TOKENIZER

            // Check spelling in strings
            RhaiTypes.STRING_LITERAL,
            RhaiTypes.MULTILINE_STRING_LITERAL,
            RhaiTypes.INTERPOLATED_TEXT -> TEXT_TOKENIZER

            // Don't check identifiers, keywords, etc.
            else -> EMPTY_TOKENIZER
        }
    }
}
