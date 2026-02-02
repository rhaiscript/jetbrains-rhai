package org.rhai.features

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import org.rhai.RhaiTypes
import java.lang.annotation.ElementType

class RhaiAnnotator : Annotator {

    companion object {
        val FUNCTION_DECLARATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION_DECLARATION",
            DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
        )

        val FUNCTION_CALL: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION_CALL",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
        )
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
//         Отладка: смотрим, что приходит в аннотатор
         println("Element: ${element.text} ${element.javaClass.simpleName}, type: ${element.elementType}")

//        when (element.elementType) {
//            RhaiTypes.FUNCTION_DEFINITION -> annotateFunctionDefinition(element, holder)
//            RhaiTypes.IDENTIFIER -> annotateIdentifier(element, holder)
//            GeneratedParserUtilBase.DUMMY_BLOCK -> annotateIdentifier(element, holder)
//        }
//
//        checkErrors(element, holder)
    }

    private fun annotateFunctionDefinition(func: PsiElement, holder: AnnotationHolder) {
        // Ищем первый IDENTIFIER внутри функции - это имя функции
        val nameIdentifier = func.node.findChildByType(RhaiTypes.IDENTIFIER)?.psi
            ?: func.children.find { it.elementType == RhaiTypes.IDENTIFIER }

        nameIdentifier?.let { id ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(id.textRange)
                .textAttributes(FUNCTION_DECLARATION)
                .create()
        }

        // Параметры - ищем внутри PARAMETERS
        func.children.forEach { child ->
            if (child.elementType?.toString()?.contains("PARAMETER") == true) {
                // Внутри параметра ищем IDENTIFIER
                child.node.findChildByType(RhaiTypes.IDENTIFIER)?.psi?.let { paramId ->
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(paramId.textRange)
                        .textAttributes(PARAMETER)
                        .create()
                }
            }
        }
    }

    private fun annotateIdentifier(identifier: PsiElement, holder: AnnotationHolder) {
        val parent = identifier.parent

        // Проверяем, что это вызов функции (после идентификатора идут скобки)
        // Или родитель - это postfix-выражение с вызовом
        println("nameIdentifier: identifier.nextSibling: ${identifier.text} ${identifier.nextSibling}")

        parent?.children?.forEach {
            println("annotateIdentifier: child ${it.text}; ${it.elementType}")
        }
        val isFunctionCall = parent.children.all { it.elementType == GeneratedParserUtilBase.DUMMY_BLOCK }

        if (isFunctionCall) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(identifier.textRange)
                .textAttributes(FUNCTION_CALL)
                .create()
        }
    }

    private fun skipWs(element: PsiElement?): PsiElement? {
        var current = element
        while (current != null && current.elementType.toString().contains("WHITESPACE")) {
            current = current.nextSibling
        }
        return current
    }

    private fun checkErrors(element: PsiElement, holder: AnnotationHolder) {
        if (element.elementType == TokenType.BAD_CHARACTER) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Invalid character")
                .range(element.textRange)
                .highlightType(ProblemHighlightType.ERROR)
                .create()
        }
    }

    // Остальные методы (annotatePath, annotateMacro, etc.) можно оставить через elementType тоже
    private val PARAMETER = TextAttributesKey.createTextAttributesKey(
        "RHAI_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER
    )
}