package org.rhai.features

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.*
import org.rhai.lang.RhaiFile
import org.rhai.lang.RhaiIcons
import javax.swing.Icon

class RhaiStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    TextEditorBasedStructureViewModel(editor, psiFile) {

    override fun getRoot(): StructureViewTreeElement {
        return RhaiStructureViewElement(psiFile)
    }

    override fun getSorters(): Array<Sorter> {
        return arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun getFilters(): Array<Filter> {
        return arrayOf(
            RhaiPrivateMembersFilter(),
            RhaiVariablesFilter()
        )
    }

    override fun getSuitableClasses(): Array<Class<*>> {
        return arrayOf(
            RhaiFunctionDefinition::class.java,
            RhaiLetDeclaration::class.java,
            RhaiConstDeclaration::class.java,
            RhaiModuleDeclaration::class.java
        )
    }
}

class RhaiStructureViewElement(private val element: PsiElement) : StructureViewTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        if (element is NavigatablePsiElement) {
            element.navigate(requestFocus)
        }
    }

    override fun canNavigate(): Boolean {
        return element is NavigatablePsiElement && element.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element is NavigatablePsiElement && element.canNavigateToSource()
    }

    override fun getChildren(): Array<StructureViewTreeElement> {
        if (element !is RhaiFile) {
            return emptyArray()
        }

        val children = mutableListOf<StructureViewTreeElement>()

        // Add modules
        PsiTreeUtil.findChildrenOfType(element, RhaiModuleDeclaration::class.java).forEach {
            children.add(RhaiStructureViewElement(it))
        }

        // Add functions
        PsiTreeUtil.findChildrenOfType(element, RhaiFunctionDefinition::class.java).forEach {
            children.add(RhaiStructureViewElement(it))
        }

        // Add constants
        PsiTreeUtil.findChildrenOfType(element, RhaiConstDeclaration::class.java).forEach {
            children.add(RhaiStructureViewElement(it))
        }

        // Add top-level variables
        element.children.filterIsInstance<RhaiStatement>().forEach { stmt ->
            val letDecl = PsiTreeUtil.findChildOfType(stmt, RhaiLetDeclaration::class.java)
            if (letDecl != null) {
                children.add(RhaiStructureViewElement(letDecl))
            }
        }

        return children.toTypedArray()
    }

    override fun getPresentation(): ItemPresentation {
        return when (element) {
            is RhaiFile -> FilePresentation(element)
            is RhaiFunctionDefinition -> FunctionPresentation(element)
            is RhaiLetDeclaration -> VariablePresentation(element)
            is RhaiConstDeclaration -> ConstantPresentation(element)
            is RhaiModuleDeclaration -> ModulePresentation(element)
            else -> DefaultPresentation(element)
        }
    }

    // Presentation classes
    private class FilePresentation(private val file: RhaiFile) : ItemPresentation {
        override fun getPresentableText(): String = file.name
        override fun getLocationString(): String? = null
        override fun getIcon(unused: Boolean): Icon = RhaiIcons.FILE
    }

    private class FunctionPresentation(private val func: RhaiFunctionDefinition) : ItemPresentation {
        override fun getPresentableText(): String {
            val name = func.identifier.text
            val params = func.parameters?.parameterList?.joinToString(", ") { it.pattern.text } ?: ""
            return "$name($params)"
        }

        override fun getLocationString(): String? {
            return func.returnType?.text?.removePrefix("->")?.trim()
        }

        override fun getIcon(unused: Boolean): Icon {
            val visibility = func.visibility?.text
            return when {
                visibility == "private" -> AllIcons.Nodes.Method
                visibility == "pub" -> AllIcons.Nodes.AbstractMethod
                func.identifier.text.startsWith("test_") -> AllIcons.RunConfigurations.TestState.Run
                else -> AllIcons.Nodes.Function
            }
        }
    }

    private class VariablePresentation(private val letDecl: RhaiLetDeclaration) : ItemPresentation {
        override fun getPresentableText(): String = letDecl.pattern.text

        override fun getLocationString(): String? {
            return letDecl.typeAnnotation?.text?.removePrefix(":")?.trim()
        }

        override fun getIcon(unused: Boolean): Icon = AllIcons.Nodes.Variable
    }

    private class ConstantPresentation(private val constDecl: RhaiConstDeclaration) : ItemPresentation {
        override fun getPresentableText(): String {
            return constDecl.node.findChildByType(RhaiTypes.IDENTIFIER)?.text ?: "const"
        }

        override fun getLocationString(): String? = "const"

        override fun getIcon(unused: Boolean): Icon = AllIcons.Nodes.Constant
    }

    private class ModulePresentation(private val module: RhaiModuleDeclaration) : ItemPresentation {
        override fun getPresentableText(): String {
            return module.node.findChildByType(RhaiTypes.IDENTIFIER)?.text ?: "module"
        }

        override fun getLocationString(): String? = null

        override fun getIcon(unused: Boolean): Icon = AllIcons.Nodes.Module
    }

    private class DefaultPresentation(private val element: PsiElement) : ItemPresentation {
        override fun getPresentableText(): String = element.text.take(30)
        override fun getLocationString(): String? = null
        override fun getIcon(unused: Boolean): Icon? = null
    }
}

// Filter for private members
class RhaiPrivateMembersFilter : Filter {
    override fun getName(): String = "SHOW_PRIVATE"

    override fun getPresentation(): ActionPresentation {
        return object : ActionPresentation {
            override fun getText(): String = "Show Private Members"
            override fun getDescription(): String = "Show private functions and variables"
            override fun getIcon(): Icon = AllIcons.Nodes.C_private
        }
    }

    override fun isVisible(treeNode: TreeElement): Boolean {
        if (treeNode !is RhaiStructureViewElement) return true

        val value = treeNode.value
        if (value is RhaiFunctionDefinition) {
            return value.visibility?.text != "private"
        }
        return true
    }

    override fun isReverted(): Boolean = true
}

// Filter for variables
class RhaiVariablesFilter : Filter {
    override fun getName(): String = "SHOW_VARIABLES"

    override fun getPresentation(): ActionPresentation {
        return object : ActionPresentation {
            override fun getText(): String = "Show Variables"
            override fun getDescription(): String = "Show let declarations"
            override fun getIcon(): Icon = AllIcons.Nodes.Variable
        }
    }

    override fun isVisible(treeNode: TreeElement): Boolean {
        if (treeNode !is RhaiStructureViewElement) return true
        return treeNode.value !is RhaiLetDeclaration
    }

    override fun isReverted(): Boolean = true
}
