package org.rhai

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class RhaiFile(viewProvider: FileViewProvider) : 
    PsiFileBase(viewProvider, RhaiLanguage.INSTANCE) {
    
    override fun getFileType(): FileType = RhaiFileType.INSTANCE
    override fun toString(): String = "Rhai File"
}
