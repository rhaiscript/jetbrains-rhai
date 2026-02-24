package org.rhai.run

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import org.rhai.lang.RhaiFile

class RhaiRunLineMarkerProvider : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        val containingFile = element.containingFile
        if (containingFile !is RhaiFile) {
            return null
        }

        // Only show on leaf elements (no children)
        if (element.firstChild != null) {
            return null
        }

        // Check if this element is at the very beginning of the file (offset 0)
        if (element.textRange.startOffset != 0) {
            return null
        }

        val actions = ExecutorAction.getActions(0)
        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            actions,
            { "Run ${containingFile.name}" }
        )
    }
}
