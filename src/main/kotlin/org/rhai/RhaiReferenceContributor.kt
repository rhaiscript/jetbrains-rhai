package org.rhai

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.rhai.RhaiTypes

class RhaiReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Регистрируем провайдер для идентификаторов в вызовах функций
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiElement::class.java)
                .withElementType(RhaiTypes.IDENTIFIER),
            RhaiFunctionReferenceProvider()
        )
    }
}

class RhaiFunctionReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        // Проверяем, что это идентификатор в вызове функции
        val parent = element.parent
        if (parent is RhaiPrimaryExpr) {
            val grandParent = parent.parent
            if (grandParent is RhaiPostfixExpr) {
                val postfixOps = grandParent.postfixOpList
                if (postfixOps.isNotEmpty() && postfixOps[0].argumentList != null) {
                    // Это вызов функции, создаем ссылку
                    return arrayOf(RhaiFunctionReference(element))
                }
            }
        }
        return emptyArray()
    }
}

class RhaiFunctionReference(element: PsiElement) : PsiReferenceBase<PsiElement>(element, true) {
    override fun resolve(): PsiElement? {
        val functionName = element.text
        
        // Ищем определение функции в том же файле
        val file = element.containingFile
        if (file is RhaiFile) {
            return findFunctionDefinition(file, functionName)
        }
        
        return null
    }
    
    private fun findFunctionDefinition(file: RhaiFile, name: String): PsiElement? {
        // Используем PsiTreeUtil для поиска всех определений функций
        val functionDefinitions = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
        
        for (funcDef in functionDefinitions) {
            // Ищем идентификатор функции
            var foundFn = false
            for (funcChild in funcDef.children) {
                when (funcChild.node?.elementType) {
                    RhaiTypes.PRIVATE -> continue
                    RhaiTypes.FN -> {
                        foundFn = true
                        continue
                    }
                    RhaiTypes.IDENTIFIER -> {
                        if (foundFn && funcChild.text == name) {
                            // Возвращаем сам идентификатор, а не определение функции
                            return funcChild
                        }
                    }
                    RhaiTypes.LPAREN -> break
                }
            }
        }
        return null
    }
    
    override fun getVariants(): Array<Any> = emptyArray()
}
