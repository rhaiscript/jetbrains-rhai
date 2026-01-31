package org.rhai.file

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import org.rhai.lang.RhaiIcons

class RhaiTemplatesProvider : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group =
            FileTemplateGroupDescriptor("Rhai", RhaiIcons.FILE)
        group.addTemplate("Rhai File")
        return group
    }
}