package org.rhai.features

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.rhai.RhaiTypes

class RhaiBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(RhaiTypes.LPAREN, RhaiTypes.RPAREN, false),
        BracePair(RhaiTypes.LBRACE, RhaiTypes.RBRACE, true)
    )

    override fun getPairs(): Array<BracePair> = pairs
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?) = true
    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
