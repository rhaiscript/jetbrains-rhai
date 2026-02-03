package org.rhai.features

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.rhai.*
import org.rhai.lang.RhaiFile
import org.rhai.lang.RhaiIcons
import javax.swing.Icon

class RhaiLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Only process identifiers to avoid duplicate markers
        if (element.elementType != RhaiTypes.IDENTIFIER) {
            return null
        }

        val parent = element.parent

        // Function definition marker
        if (parent is RhaiFunctionDefinition && element == parent.identifier) {
            return createFunctionMarker(element, parent)
        }

        return null
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        for (element in elements) {
            if (element.elementType != RhaiTypes.IDENTIFIER) continue

            val parent = element.parent

            // Find function usages
            if (parent is RhaiFunctionDefinition && element == parent.identifier) {
                collectFunctionUsageMarkers(element, parent, result)
            }
        }
    }

    private fun createFunctionMarker(
        element: PsiElement,
        func: RhaiFunctionDefinition
    ): LineMarkerInfo<PsiElement> {
        val icon = getFunctionIcon(func)
        val name = func.identifier.text

        return LineMarkerInfo(
            element,
            element.textRange,
            icon,
            { "Function: $name" },
            null,
            GutterIconRenderer.Alignment.CENTER,
            { "Function $name" }
        )
    }

    private fun getFunctionIcon(func: RhaiFunctionDefinition): Icon {
        val visibility = func.visibility
        val isPrivate = visibility?.text == "private"
        val isPublic = visibility?.text == "pub"

        return when {
            isPrivate -> AllIcons.Nodes.Method
            isPublic -> AllIcons.Nodes.AbstractMethod
            func.identifier.text.startsWith("test_") -> AllIcons.RunConfigurations.TestState.Run
            func.identifier.text == "main" -> AllIcons.RunConfigurations.Application
            else -> AllIcons.Nodes.Function
        }
    }

    private fun collectFunctionUsageMarkers(
        element: PsiElement,
        func: RhaiFunctionDefinition,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        val file = element.containingFile as? RhaiFile ?: return
        val funcName = func.identifier.text

        // Find all calls to this function
        val calls = findFunctionCalls(file, funcName)

        if (calls.isNotEmpty()) {
            val builder = NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod)
                .setTargets(calls)
                .setTooltipText("${calls.size} usage(s)")
                .setPopupTitle("Usages of '$funcName'")

            result.add(builder.createLineMarkerInfo(element))
        }
    }

    private fun findFunctionCalls(file: RhaiFile, functionName: String): List<PsiElement> {
        val calls = mutableListOf<PsiElement>()

        PsiTreeUtil.processElements(file) { element ->
            if (element.elementType == RhaiTypes.IDENTIFIER &&
                element.text == functionName &&
                element.parent !is RhaiFunctionDefinition) {

                // Check if it's followed by parentheses (function call)
                val next = element.nextSibling
                if (next != null && next.text == "(") {
                    calls.add(element)
                }
            }
            true
        }

        return calls
    }
}
