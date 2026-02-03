package org.rhai.features

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.*

class RhaiFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Fold function definitions
        PsiTreeUtil.findChildrenOfType(root, RhaiFunctionDefinition::class.java).forEach { func ->
            func.block?.let { block ->
                addFoldingDescriptor(descriptors, block.node, "{...}")
            }
        }

        // Fold blocks (standalone and in control structures)
        PsiTreeUtil.findChildrenOfType(root, RhaiBlock::class.java).forEach { block ->
            val parent = block.parent
            when (parent) {
                is RhaiFunctionDefinition -> {} // Already handled
                is RhaiIfStatement -> addFoldingDescriptor(descriptors, block.node, "if {...}")
                is RhaiWhileStatement -> addFoldingDescriptor(descriptors, block.node, "while {...}")
                is RhaiForStatement -> addFoldingDescriptor(descriptors, block.node, "for {...}")
                is RhaiLoopStatement -> addFoldingDescriptor(descriptors, block.node, "loop {...}")
                is RhaiTryCatchStatement -> addFoldingDescriptor(descriptors, block.node, "{...}")
                else -> {
                    if (block.textLength > 3) {
                        addFoldingDescriptor(descriptors, block.node, "{...}")
                    }
                }
            }
        }

        // Fold switch expressions
        PsiTreeUtil.findChildrenOfType(root, RhaiSwitchExpression::class.java).forEach { switch ->
            if (switch.textLength > 20) {
                addFoldingDescriptor(descriptors, switch.node, "switch {...}")
            }
        }

        // Fold array literals
        PsiTreeUtil.findChildrenOfType(root, RhaiArrayLiteral::class.java).forEach { array ->
            if (array.textLength > 30) {
                addFoldingDescriptor(descriptors, array.node, "[...]")
            }
        }

        // Fold object literals
        PsiTreeUtil.findChildrenOfType(root, RhaiObjectLiteral::class.java).forEach { obj ->
            if (obj.textLength > 30) {
                addFoldingDescriptor(descriptors, obj.node, "#{...}")
            }
        }

        // Fold closures
        PsiTreeUtil.findChildrenOfType(root, RhaiClosureExpr::class.java).forEach { closure ->
            if (closure.textLength > 30) {
                addFoldingDescriptor(descriptors, closure.node, "|...| {...}")
            }
        }

        // Fold multiline comments
        root.node.getChildren(null).forEach { node ->
            when (node.elementType) {
                RhaiTypes.BLOCK_COMMENT -> {
                    if (node.textLength > 10) {
                        addFoldingDescriptor(descriptors, node, "/* ... */")
                    }
                }
                RhaiTypes.DOC_COMMENT -> {
                    if (node.textLength > 10) {
                        addFoldingDescriptor(descriptors, node, "/** ... */")
                    }
                }
            }
        }

        // Fold consecutive line comments
        foldConsecutiveLineComments(root, document, descriptors)

        // Fold imports
        foldImports(root, descriptors)

        return descriptors.toTypedArray()
    }

    private fun addFoldingDescriptor(
        descriptors: MutableList<FoldingDescriptor>,
        node: ASTNode,
        placeholder: String
    ) {
        if (node.textLength > 1) {
            descriptors.add(
                FoldingDescriptor(
                    node,
                    node.textRange,
                    FoldingGroup.newGroup("rhai"),
                    placeholder
                )
            )
        }
    }

    private fun foldConsecutiveLineComments(
        root: PsiElement,
        document: Document,
        descriptors: MutableList<FoldingDescriptor>
    ) {
        val comments = mutableListOf<PsiElement>()
        var currentGroup = mutableListOf<PsiElement>()

        root.node.getChildren(null).forEach { node ->
            if (node.elementType == RhaiTypes.LINE_COMMENT || node.elementType == RhaiTypes.DOC_LINE) {
                val psi = node.psi
                if (currentGroup.isEmpty()) {
                    currentGroup.add(psi)
                } else {
                    val lastComment = currentGroup.last()
                    val lastLine = document.getLineNumber(lastComment.textRange.endOffset)
                    val currentLine = document.getLineNumber(psi.textRange.startOffset)

                    if (currentLine == lastLine + 1) {
                        currentGroup.add(psi)
                    } else {
                        if (currentGroup.size >= 2) {
                            comments.addAll(currentGroup)
                            addCommentGroupDescriptor(currentGroup, descriptors)
                        }
                        currentGroup = mutableListOf(psi)
                    }
                }
            }
        }

        if (currentGroup.size >= 2) {
            addCommentGroupDescriptor(currentGroup, descriptors)
        }
    }

    private fun addCommentGroupDescriptor(
        group: List<PsiElement>,
        descriptors: MutableList<FoldingDescriptor>
    ) {
        val first = group.first()
        val last = group.last()
        val range = TextRange(first.textRange.startOffset, last.textRange.endOffset)

        descriptors.add(
            FoldingDescriptor(
                first.node,
                range,
                FoldingGroup.newGroup("rhai-comments"),
                "// ... (${group.size} lines)"
            )
        )
    }

    private fun foldImports(root: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        val imports = PsiTreeUtil.findChildrenOfType(root, RhaiImportStatement::class.java).toList()

        if (imports.size >= 2) {
            val first = imports.first()
            val last = imports.last()
            val range = TextRange(first.textRange.startOffset, last.textRange.endOffset)

            descriptors.add(
                FoldingDescriptor(
                    first.node,
                    range,
                    FoldingGroup.newGroup("rhai-imports"),
                    "import ... (${imports.size} imports)"
                )
            )
        }
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return when (node.elementType) {
            RhaiTypes.BLOCK -> "{...}"
            RhaiTypes.FUNCTION_DEFINITION -> "fn {...}"
            RhaiTypes.BLOCK_COMMENT -> "/* ... */"
            RhaiTypes.DOC_COMMENT -> "/** ... */"
            RhaiTypes.LINE_COMMENT -> "// ..."
            RhaiTypes.ARRAY_LITERAL -> "[...]"
            RhaiTypes.OBJECT_LITERAL -> "#{...}"
            RhaiTypes.CLOSURE_EXPR -> "|...| {...}"
            RhaiTypes.SWITCH_EXPRESSION -> "switch {...}"
            else -> "..."
        }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return when (node.elementType) {
            RhaiTypes.IMPORT_STATEMENT -> true
            RhaiTypes.BLOCK_COMMENT, RhaiTypes.DOC_COMMENT -> node.textLength > 200
            else -> false
        }
    }
}
