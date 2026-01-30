package org.rhai

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.rhai.RhaiVisitor

class RhaiStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    TextEditorBasedStructureViewModel(editor, psiFile) {

    override fun getRoot(): StructureViewTreeElement {
        return RhaiStructureViewElement(psiFile)
    }
}

class RhaiStructureViewElement(private val element: PsiElement) : StructureViewTreeElement {
    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        // Navigation logic
    }

    override fun canNavigate(): Boolean = false
    override fun canNavigateToSource(): Boolean = false

    override fun getChildren(): Array<TreeElement> {
        val children = mutableListOf<TreeElement>()
        element.acceptChildren(object : RhaiVisitor() {
//            override fun visitLetStmt(o: RhaiLetDeclaration) {
//                children.add(RhaiStructureViewElement(o))
//            }
        })
        return children.toTypedArray()
    }

    override fun getPresentation(): com.intellij.navigation.ItemPresentation {
        return object : com.intellij.navigation.ItemPresentation {
            override fun getPresentableText(): String? = element.text
            override fun getLocationString(): String? = null
            override fun getIcon(unused: Boolean): javax.swing.Icon? = null
        }
    }
}
