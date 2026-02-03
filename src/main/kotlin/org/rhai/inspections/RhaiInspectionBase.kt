package org.rhai.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.rhai.lang.RhaiFile

abstract class RhaiInspectionBase : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is RhaiFile) {
            return PsiElementVisitor.EMPTY_VISITOR
        }
        return buildRhaiVisitor(holder, isOnTheFly)
    }

    abstract fun buildRhaiVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor

    override fun getGroupDisplayName(): String = "Rhai"
}
