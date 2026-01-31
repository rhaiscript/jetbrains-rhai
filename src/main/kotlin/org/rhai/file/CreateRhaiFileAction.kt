package org.rhai.file

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.rhai.lang.RhaiIcons

class CreateRhaiFileAction : CreateFileFromTemplateAction("Rhai Script", "Create a new Rhai script", RhaiIcons.FILE) {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Rhai Script")
            .addKind("Rhai Script", RhaiIcons.FILE, "Rhai File")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?) =
        "Create Rhai script ${newName}"

}