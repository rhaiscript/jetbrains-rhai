package org.rhai.features

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.rhai.*
import org.rhai.highlighting.RhaiSyntaxHighlighter

class RhaiAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Проверяем идентификаторы и BUILTIN напрямую
        when (element.node?.elementType) {
            RhaiTypes.IDENTIFIER, RhaiTypes.BUILTIN -> {
                val parent = element.parent

                if (parent is RhaiFunctionDefinition) {
                    var foundFn = false
                    for (sibling in parent.children) {
                        when (sibling.node?.elementType) {
                            RhaiTypes.PRIVATE -> continue
                            RhaiTypes.FN -> {
                                foundFn = true
                                continue
                            }

                            RhaiTypes.IDENTIFIER -> {
                                if (foundFn && sibling == element) {
                                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(element)
                                        .textAttributes(RhaiSyntaxHighlighter.FUNCTION_DECLARATION)
                                        .create()
                                    return
                                }
                            }

                            RhaiTypes.LPAREN -> break
                        }
                    }
                }

                // Вызов функции: identifier(...) или BUILTIN(...)
                if (parent is RhaiPrimaryExpr) {
                    val grandParent = parent.parent
                    if (grandParent is RhaiPostfixExpr) {
                        val postfixOps = grandParent.postfixOpList
                        if (postfixOps.isNotEmpty() && postfixOps[0].argumentList != null) {
                            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                .range(element)
                                .textAttributes(RhaiSyntaxHighlighter.FUNCTION_CALL)
                                .create()
                        }
                    }
                }

                // Вызов функции через path_expression: std::foo::bar(...)
                if (parent is RhaiPathExpression) {
                    val pathParent = parent.parent
                    if (pathParent is RhaiPrimaryExpr) {
                        val grandParent = pathParent.parent
                        if (grandParent is RhaiPostfixExpr) {
                            val postfixOps = grandParent.postfixOpList
                            if (postfixOps.isNotEmpty() && postfixOps[0].argumentList != null) {
                                // Подсвечиваем только последний идентификатор в path
                                val identifiers = parent.children.filter {
                                    it.node?.elementType == RhaiTypes.IDENTIFIER
                                }
                                if (identifiers.isNotEmpty() && identifiers.last() == element) {
                                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(element)
                                        .textAttributes(RhaiSyntaxHighlighter.FUNCTION_CALL)
                                        .create()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Также проверяем определения функций на верхнем уровне
        if (element is RhaiFunctionDefinition) {
            var foundFn = false
            for (child in element.children) {
                when (child.node?.elementType) {
                    RhaiTypes.PRIVATE -> continue
                    RhaiTypes.FN -> {
                        foundFn = true
                        continue
                    }

                    RhaiTypes.IDENTIFIER -> {
                        if (foundFn) {
                            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                .range(child)
                                .textAttributes(RhaiSyntaxHighlighter.FUNCTION_DECLARATION)
                                .create()
                            return
                        }
                    }

                    RhaiTypes.LPAREN -> break
                }
            }
        }
    }
}
