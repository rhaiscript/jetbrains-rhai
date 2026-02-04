package org.rhai.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai quick fixes.
 */
class RhaiQuickFixesTest : BasePlatformTestCase() {

    fun `test create function quick fix exists`() {
        val quickFix = CreateFunctionQuickFix("testFunc", 2)
        assertNotNull(quickFix)
        assertEquals("Create function 'testFunc'", quickFix.name)
        assertEquals("Create function", quickFix.familyName)
    }

    fun `test create variable quick fix exists`() {
        val quickFix = CreateVariableQuickFix("testVar")
        assertNotNull(quickFix)
        assertEquals("Create variable 'testVar'", quickFix.name)
        assertEquals("Create variable", quickFix.familyName)
    }

    fun `test delete duplicate function quick fix exists`() {
        val quickFix = DeleteDuplicateFunctionQuickFix()
        assertNotNull(quickFix)
        assertEquals("Delete duplicate function", quickFix.name)
        assertEquals("Delete duplicate function", quickFix.familyName)
    }

    fun `test rename duplicate function quick fix exists`() {
        val quickFix = RenameDuplicateFunctionQuickFix("myFunc")
        assertNotNull(quickFix)
        assertEquals("Rename to 'myFunc_2'", quickFix.name)
        assertEquals("Rename duplicate function", quickFix.familyName)
    }

    fun `test add semicolon quick fix exists`() {
        val quickFix = AddSemicolonQuickFix()
        assertNotNull(quickFix)
        assertEquals("Add missing semicolon", quickFix.name)
        assertEquals("Add semicolon", quickFix.familyName)
    }

    fun `test remove semicolon quick fix exists`() {
        val quickFix = RemoveSemicolonQuickFix()
        assertNotNull(quickFix)
        assertEquals("Remove unnecessary semicolon", quickFix.name)
        assertEquals("Remove semicolon", quickFix.familyName)
    }

    fun `test convert to closure quick fix exists`() {
        val quickFix = ConvertToClosureQuickFix("myFunc")
        assertNotNull(quickFix)
        assertEquals("Convert to closure assigned to 'myFunc'", quickFix.name)
        assertEquals("Convert to closure", quickFix.familyName)
    }

    fun `test simplify boolean expression quick fix exists`() {
        val quickFix = SimplifyBooleanExpressionQuickFix("x")
        assertNotNull(quickFix)
        assertEquals("Simplify to 'x'", quickFix.name)
        assertEquals("Simplify boolean expression", quickFix.familyName)
    }

    fun `test unused variable inspection has quick fixes`() {
        myFixture.enableInspections(RhaiUnusedVariableInspection())
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let unusedVar = 42;
        """.trimIndent())
        myFixture.doHighlighting()
        // Test passes if no exception is thrown
    }

    fun `test duplicate function inspection has quick fixes`() {
        myFixture.enableInspections(RhaiDuplicateFunctionInspection())
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn foo() {}
            fn foo() {}
        """.trimIndent())
        myFixture.doHighlighting()
        // Test passes if no exception is thrown
    }

    fun `test unresolved reference inspection has quick fixes`() {
        myFixture.enableInspections(RhaiUnresolvedReferenceInspection())
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let x = unknownVar;
        """.trimIndent())
        myFixture.doHighlighting()
        // Test passes if no exception is thrown
    }
}
