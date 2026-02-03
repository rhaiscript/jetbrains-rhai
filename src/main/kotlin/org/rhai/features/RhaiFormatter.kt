package org.rhai.features

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiLanguage

class RhaiFormatter : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val block = RhaiFormattingBlock(
            formattingContext.node,
            Wrap.createWrap(WrapType.NONE, false),
            Alignment.createAlignment(),
            createSpacingBuilder(settings),
            settings
        )
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            block,
            settings
        )
    }

    private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
        return SpacingBuilder(settings, RhaiLanguage.INSTANCE)
            .around(RhaiTypes.EQ).spacing(1, 1, 0, false, 0)
    }
}

class RhaiFormattingBlock(
    private val node: ASTNode,
    private val wrap: Wrap?,
    private val alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder,
    private val settings: CodeStyleSettings
) : ASTBlock {
    override fun getNode(): ASTNode = node
    override fun getWrap(): Wrap? = wrap
    override fun getAlignment(): Alignment? = alignment
    override fun getIndent(): Indent? = Indent.getNoneIndent()
    override fun getSpacing(child1: Block?, child2: Block): Spacing? = spacingBuilder.getSpacing(this, child1, child2)
    override fun isLeaf(): Boolean = node.firstChildNode == null
    override fun getSubBlocks(): List<Block> = emptyList()
    override fun getTextRange(): TextRange = node.textRange
    override fun getChildAttributes(newChildIndex: Int): ChildAttributes = ChildAttributes.DELEGATE_TO_NEXT_CHILD
    override fun isIncomplete(): Boolean = FormatterUtil.isIncomplete(node)
}
