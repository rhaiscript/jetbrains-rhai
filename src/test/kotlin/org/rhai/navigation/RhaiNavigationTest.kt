package org.rhai.navigation

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.RhaiFunctionDefinition
import org.rhai.RhaiLetDeclaration
import org.rhai.lang.RhaiFile
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai navigation (Go to Declaration).
 */
class RhaiNavigationTest : BasePlatformTestCase() {

    fun `test go to declaration handler exists`() {
        val handler = RhaiGotoDeclarationHandler()
        assertNotNull(handler)
    }

    fun `test file with functions parses correctly`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn myFunction() {
                print("hello");
            }

            myFunction();
        """.trimIndent())

        val file = myFixture.file as RhaiFile
        val functions = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
        assertEquals(1, functions.size)
        assertEquals("myFunction", functions.first().identifier.text)
    }

    fun `test file with variables parses correctly`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let myVariable = 42;
            print(myVariable);
        """.trimIndent())

        val file = myFixture.file as RhaiFile
        val letDecls = PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java)
        assertEquals(1, letDecls.size)
    }

    fun `test nested scopes parse correctly`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn outer() {
                let x = 1;
                if true {
                    let y = x;
                }
            }
        """.trimIndent())

        val file = myFixture.file as RhaiFile
        val functions = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
        assertEquals(1, functions.size)

        val letDecls = PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java)
        assertEquals(2, letDecls.size)
    }

    fun `test global and local variables`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let globalVar = "global";

            fn test() {
                let localVar = "local";
                print(globalVar);
                print(localVar);
            }
        """.trimIndent())

        val file = myFixture.file as RhaiFile
        val letDecls = PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java)
        assertEquals(2, letDecls.size)
    }

    fun `test multiple functions`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn foo() {}
            fn bar() {}
            fn baz() {}
        """.trimIndent())

        val file = myFixture.file as RhaiFile
        val functions = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
        assertEquals(3, functions.size)
    }
}
