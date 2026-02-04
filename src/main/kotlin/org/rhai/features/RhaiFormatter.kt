package org.rhai.features

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiLanguage
import org.rhai.settings.RhaiCodeStyleSettings

/**
 * Formatting model builder for Rhai language.
 * Handles code formatting (Ctrl+Alt+L).
 */
class RhaiFormatter : FormattingModelBuilder {

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val rhaiSettings = settings.getCustomSettings(RhaiCodeStyleSettings::class.java)
        val commonSettings = settings.getCommonSettings(RhaiLanguage.INSTANCE)

        val spacingBuilder = createSpacingBuilder(settings, rhaiSettings, commonSettings)

        val block = RhaiFormattingBlock(
            node = formattingContext.node,
            wrap = null,
            alignment = null,
            spacingBuilder = spacingBuilder,
            settings = settings,
            rhaiSettings = rhaiSettings,
            indent = Indent.getNoneIndent(),
            isRoot = true
        )

        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            block,
            settings
        )
    }

    private fun createSpacingBuilder(
        settings: CodeStyleSettings,
        rhaiSettings: RhaiCodeStyleSettings,
        commonSettings: CommonCodeStyleSettings
    ): SpacingBuilder {
        return SpacingBuilder(settings, RhaiLanguage.INSTANCE)
            // Assignment operators
            .aroundInside(RhaiTypes.ASSIGN, RhaiTypes.LET_DECLARATION).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .aroundInside(RhaiTypes.ASSIGN, RhaiTypes.CONST_DECLARATION).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.PLUS_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.MINUS_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.MUL_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.DIV_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.MOD_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.POW_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.AND_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.OR_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.XOR_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.SHL_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
            .around(RhaiTypes.SHR_ASSIGN).spaceIf(rhaiSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)

            // Logical operators
            .around(RhaiTypes.AND).spaceIf(rhaiSettings.SPACE_AROUND_LOGICAL_OPERATORS)
            .around(RhaiTypes.OR).spaceIf(rhaiSettings.SPACE_AROUND_LOGICAL_OPERATORS)

            // Equality operators
            .around(RhaiTypes.EQ).spaceIf(rhaiSettings.SPACE_AROUND_EQUALITY_OPERATORS)
            .around(RhaiTypes.NE).spaceIf(rhaiSettings.SPACE_AROUND_EQUALITY_OPERATORS)

            // Relational operators
            .around(RhaiTypes.LT).spaceIf(rhaiSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
            .around(RhaiTypes.GT).spaceIf(rhaiSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
            .around(RhaiTypes.LE).spaceIf(rhaiSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
            .around(RhaiTypes.GE).spaceIf(rhaiSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
            .around(RhaiTypes.SPACESHIP).spaceIf(rhaiSettings.SPACE_AROUND_RELATIONAL_OPERATORS)

            // Additive operators
            .around(RhaiTypes.PLUS).spaceIf(rhaiSettings.SPACE_AROUND_ADDITIVE_OPERATORS)
            .around(RhaiTypes.MINUS).spaceIf(rhaiSettings.SPACE_AROUND_ADDITIVE_OPERATORS)

            // Multiplicative operators
            .around(RhaiTypes.MUL).spaceIf(rhaiSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
            .around(RhaiTypes.DIV).spaceIf(rhaiSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
            .around(RhaiTypes.MOD).spaceIf(rhaiSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
            .around(RhaiTypes.POW).spaceIf(rhaiSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)

            // Range operator
            .around(RhaiTypes.RANGE).spaceIf(rhaiSettings.SPACE_AROUND_RANGE_OPERATOR)
            .around(RhaiTypes.DOT_DOT_EQ).spaceIf(rhaiSettings.SPACE_AROUND_RANGE_OPERATOR)

            // Arrow operators
            .around(RhaiTypes.ARROW).spaceIf(rhaiSettings.SPACE_AROUND_ARROW_OPERATOR)
            .around(RhaiTypes.THIN_ARROW).spaceIf(rhaiSettings.SPACE_AROUND_ARROW_OPERATOR)

            // Null coalescing
            .around(RhaiTypes.NULL_COALESCING).spaces(1)

            // Comma
            .after(RhaiTypes.COMMA).spaceIf(rhaiSettings.SPACE_AFTER_COMMA)
            .before(RhaiTypes.COMMA).spaceIf(rhaiSettings.SPACE_BEFORE_COMMA)

            // Colon
            .after(RhaiTypes.COLON).spaceIf(rhaiSettings.SPACE_AFTER_COLON)
            .before(RhaiTypes.COLON).spaceIf(rhaiSettings.SPACE_BEFORE_COLON)

            // Semicolon
            .before(RhaiTypes.SEMICOLON).spaceIf(rhaiSettings.SPACE_BEFORE_SEMICOLON)
            .after(RhaiTypes.SEMICOLON).spaceIf(rhaiSettings.SPACE_AFTER_SEMICOLON)

            // Braces for functions/blocks
            .before(RhaiTypes.LBRACE).spaceIf(rhaiSettings.SPACE_BEFORE_FUNCTION_LBRACE)
            .after(RhaiTypes.LBRACE).lineBreakOrForceSpace(true, rhaiSettings.SPACE_WITHIN_BRACES)
            .before(RhaiTypes.RBRACE).lineBreakOrForceSpace(true, rhaiSettings.SPACE_WITHIN_BRACES)

            // Parentheses
            .after(RhaiTypes.LPAREN).spaceIf(rhaiSettings.SPACE_WITHIN_PARENTHESES)
            .before(RhaiTypes.RPAREN).spaceIf(rhaiSettings.SPACE_WITHIN_PARENTHESES)

            // Brackets
            .after(RhaiTypes.LBRACKET).spaceIf(rhaiSettings.SPACE_WITHIN_BRACKETS)
            .before(RhaiTypes.RBRACKET).spaceIf(rhaiSettings.SPACE_WITHIN_BRACKETS)

            // Closure pipes
            .afterInside(RhaiTypes.BOR, RhaiTypes.CLOSURE_PARAMS).spaceIf(rhaiSettings.SPACE_WITHIN_CLOSURE_PIPES)
            .beforeInside(RhaiTypes.BOR, RhaiTypes.CLOSURE_PARAMS).spaceIf(rhaiSettings.SPACE_WITHIN_CLOSURE_PIPES)

            // No space before function call parentheses
            .between(RhaiTypes.IDENTIFIER, RhaiTypes.LPAREN).spaceIf(rhaiSettings.SPACE_BEFORE_FUNCTION_PARENTHESES)

            // No space around dot
            .around(RhaiTypes.DOT).spaces(0)

            // No space around double colon
            .around(RhaiTypes.DOUBLE_COLON).spaces(0)

            // Keywords
            .after(RhaiTypes.LET).spaces(1)
            .after(RhaiTypes.CONST).spaces(1)
            .after(RhaiTypes.FN).spaces(1)
            .after(RhaiTypes.IF).spaces(1)
            .after(RhaiTypes.ELSE).spaces(1)
            .after(RhaiTypes.WHILE).spaces(1)
            .after(RhaiTypes.FOR).spaces(1)
            .after(RhaiTypes.IN).spaces(1)
            .after(RhaiTypes.RETURN).spaces(1)
            .after(RhaiTypes.THROW).spaces(1)
            .after(RhaiTypes.SWITCH).spaces(1)
            .after(RhaiTypes.TRY).spaces(1)
            .after(RhaiTypes.CATCH).spaces(1)
            .after(RhaiTypes.IMPORT).spaces(1)
            .after(RhaiTypes.EXPORT).spaces(1)
            .after(RhaiTypes.MODULE).spaces(1)
            .after(RhaiTypes.PUB).spaces(1)
            .after(RhaiTypes.PRIVATE).spaces(1)
            .after(RhaiTypes.AS).spaces(1)
            .before(RhaiTypes.AS).spaces(1)
    }
}

/**
 * Formatting block for Rhai code.
 */
class RhaiFormattingBlock(
    private val node: ASTNode,
    private val wrap: Wrap?,
    private val alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder,
    private val settings: CodeStyleSettings,
    private val rhaiSettings: RhaiCodeStyleSettings,
    private val indent: Indent,
    private val isRoot: Boolean = false
) : ASTBlock {

    private var subBlocks: List<Block>? = null

    companion object {
        private val BLOCKS = TokenSet.create(
            RhaiTypes.BLOCK,
            RhaiTypes.FUNCTION_DEFINITION,
            RhaiTypes.IF_STATEMENT,
            RhaiTypes.IF_EXPRESSION,
            RhaiTypes.WHILE_STATEMENT,
            RhaiTypes.FOR_STATEMENT,
            RhaiTypes.LOOP_STATEMENT,
            RhaiTypes.DO_WHILE_STATEMENT,
            RhaiTypes.UNTIL_STATEMENT,
            RhaiTypes.SWITCH_EXPRESSION,
            RhaiTypes.TRY_CATCH_STATEMENT,
            RhaiTypes.MODULE_DECLARATION,
            RhaiTypes.CLOSURE_EXPR
        )

        private val BRACES = TokenSet.create(
            RhaiTypes.LBRACE,
            RhaiTypes.RBRACE,
            RhaiTypes.LBRACKET,
            RhaiTypes.RBRACKET,
            RhaiTypes.LPAREN,
            RhaiTypes.RPAREN
        )

        private val CONTAINERS = TokenSet.create(
            RhaiTypes.ARRAY_LITERAL,
            RhaiTypes.OBJECT_LITERAL,
            RhaiTypes.PARAMETERS,
            RhaiTypes.ARGUMENT_LIST,
            RhaiTypes.CLOSURE_PARAMS
        )
    }

    override fun getNode(): ASTNode = node

    override fun getTextRange(): TextRange = node.textRange

    override fun getSubBlocks(): List<Block> {
        if (subBlocks == null) {
            subBlocks = buildSubBlocks()
        }
        return subBlocks!!
    }

    private fun buildSubBlocks(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = node.firstChildNode

        while (child != null) {
            if (!isWhitespaceOrEmpty(child)) {
                val childIndent = computeChildIndent(child)
                val childAlignment = computeChildAlignment(child)
                val childWrap = computeChildWrap(child)

                blocks.add(
                    RhaiFormattingBlock(
                        node = child,
                        wrap = childWrap,
                        alignment = childAlignment,
                        spacingBuilder = spacingBuilder,
                        settings = settings,
                        rhaiSettings = rhaiSettings,
                        indent = childIndent,
                        isRoot = false
                    )
                )
            }
            child = child.treeNext
        }

        return blocks
    }

    private fun isWhitespaceOrEmpty(node: ASTNode): Boolean {
        return node.elementType == TokenType.WHITE_SPACE || node.textLength == 0
    }

    private fun computeChildIndent(child: ASTNode): Indent {
        val parentType = node.elementType
        val childType = child.elementType

        // No indent for root elements
        if (isRoot) {
            return Indent.getNoneIndent()
        }

        // Contents of blocks should be indented
        if (parentType == RhaiTypes.BLOCK) {
            // Braces themselves should not be indented
            if (childType == RhaiTypes.LBRACE || childType == RhaiTypes.RBRACE) {
                return Indent.getNoneIndent()
            }
            return Indent.getNormalIndent()
        }

        // Contents of array/object literals should be indented
        if (parentType == RhaiTypes.ARRAY_LITERAL || parentType == RhaiTypes.OBJECT_LITERAL) {
            if (childType == RhaiTypes.LBRACKET || childType == RhaiTypes.RBRACKET ||
                childType == RhaiTypes.LBRACE || childType == RhaiTypes.RBRACE ||
                childType == RhaiTypes.HASH) {
                return Indent.getNoneIndent()
            }
            return Indent.getNormalIndent()
        }

        // Parameters and arguments inside parentheses
        if (parentType == RhaiTypes.PARAMETERS || parentType == RhaiTypes.ARGUMENT_LIST) {
            if (BRACES.contains(childType)) {
                return Indent.getNoneIndent()
            }
            return Indent.getContinuationIndent()
        }

        // Switch arms should be indented
        if (parentType == RhaiTypes.SWITCH_EXPRESSION) {
            if (childType == RhaiTypes.SWITCH_ARM || childType == RhaiTypes.DEFAULT_ARM) {
                return Indent.getNormalIndent()
            }
        }

        return Indent.getNoneIndent()
    }

    private fun computeChildAlignment(child: ASTNode): Alignment? {
        val parentType = node.elementType

        // Align multiline parameters
        if (rhaiSettings.ALIGN_MULTILINE_PARAMETERS && parentType == RhaiTypes.PARAMETERS) {
            if (child.elementType == RhaiTypes.PARAMETER) {
                return alignment ?: Alignment.createAlignment()
            }
        }

        // Align multiline arguments
        if (rhaiSettings.ALIGN_MULTILINE_ARGUMENTS && parentType == RhaiTypes.ARGUMENT_LIST) {
            if (!BRACES.contains(child.elementType) && child.elementType != RhaiTypes.COMMA) {
                return alignment ?: Alignment.createAlignment()
            }
        }

        // Align multiline array elements
        if (rhaiSettings.ALIGN_MULTILINE_ARRAY_ELEMENTS && parentType == RhaiTypes.ARRAY_ELEMENTS) {
            if (!BRACES.contains(child.elementType) && child.elementType != RhaiTypes.COMMA) {
                return alignment ?: Alignment.createAlignment()
            }
        }

        // Align multiline object fields
        if (rhaiSettings.ALIGN_MULTILINE_OBJECT_FIELDS && parentType == RhaiTypes.OBJECT_FIELDS) {
            if (child.elementType == RhaiTypes.OBJECT_FIELD) {
                return alignment ?: Alignment.createAlignment()
            }
        }

        return null
    }

    private fun computeChildWrap(child: ASTNode): Wrap? {
        val parentType = node.elementType
        val childType = child.elementType

        // Wrap parameters
        if (parentType == RhaiTypes.PARAMETERS && childType == RhaiTypes.PARAMETER) {
            return Wrap.createWrap(WrapType.NORMAL, false)
        }

        // Wrap arguments
        if (parentType == RhaiTypes.ARGUMENT_LIST && !BRACES.contains(childType) && childType != RhaiTypes.COMMA) {
            return Wrap.createWrap(WrapType.NORMAL, false)
        }

        // Wrap array elements
        if (parentType == RhaiTypes.ARRAY_ELEMENTS && !BRACES.contains(childType) && childType != RhaiTypes.COMMA) {
            return Wrap.createWrap(WrapType.NORMAL, false)
        }

        // Wrap object fields
        if (parentType == RhaiTypes.OBJECT_FIELDS && childType == RhaiTypes.OBJECT_FIELD) {
            return Wrap.createWrap(WrapType.ALWAYS, true)
        }

        return null
    }

    override fun getWrap(): Wrap? = wrap

    override fun getIndent(): Indent = indent

    override fun getAlignment(): Alignment? = alignment

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val parentType = node.elementType

        // Inside blocks, new line should be indented
        if (parentType == RhaiTypes.BLOCK) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        // Inside arrays/objects, continue with same indent
        if (parentType == RhaiTypes.ARRAY_LITERAL || parentType == RhaiTypes.OBJECT_LITERAL) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        // Inside parameters/arguments, use continuation indent
        if (parentType == RhaiTypes.PARAMETERS || parentType == RhaiTypes.ARGUMENT_LIST) {
            return ChildAttributes(Indent.getContinuationIndent(), null)
        }

        return ChildAttributes(Indent.getNoneIndent(), null)
    }

    override fun isIncomplete(): Boolean {
        return FormatterUtil.isIncomplete(node)
    }

    override fun isLeaf(): Boolean = node.firstChildNode == null
}
