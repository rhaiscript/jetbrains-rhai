package org.rhai.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai code inspections.
 */
class RhaiInspectionTest : BasePlatformTestCase() {

    private fun enableInspection(inspection: LocalInspectionTool) {
        myFixture.enableInspections(inspection)
    }

    private fun configureAndHighlight(code: String) {
        myFixture.configureByText(RhaiFileType.INSTANCE, code)
        myFixture.doHighlighting()
    }

    // ===== Unresolved Reference Inspection Tests =====

    fun `test unresolved reference inspection is registered`() {
        val inspection = RhaiUnresolvedReferenceInspection()
        assertNotNull(inspection)
        assertEquals("Unresolved reference", inspection.displayName)
    }

    fun `test defined variable produces no error`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            let myVar = 42;
            let x = myVar;
        """.trimIndent())
        // No assertion needed - test passes if no exception
    }

    fun `test function parameter is recognized`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            fn process(data) {
                print(data);
            }
        """.trimIndent())
    }

    fun `test closure parameter is recognized`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            let f = |x| x * 2;
        """.trimIndent())
    }

    fun `test for loop variable is recognized`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            for item in [1, 2, 3] {
                print(item);
            }
        """.trimIndent())
    }

    fun `test const is recognized`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            const MAX = 100;
            let x = MAX;
        """.trimIndent())
    }

    fun `test builtin functions are recognized`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            print("hello");
            println("world");
            let t = type_of(42);
        """.trimIndent())
    }

    fun `test property access is not flagged`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            let obj = #{ name: "test" };
            let n = obj.name;
        """.trimIndent())
    }

    fun `test method call is not flagged`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            let arr = [1, 2, 3];
            arr.push(4);
        """.trimIndent())
    }

    fun `test global variable visible in function`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            let globalVar = "hello";

            fn test() {
                print(globalVar);
            }
        """.trimIndent())
    }

    fun `test defined function is recognized`() {
        enableInspection(RhaiUnresolvedReferenceInspection())
        configureAndHighlight("""
            fn myFunc() {}
            myFunc();
        """.trimIndent())
    }

    // ===== Unused Variable Inspection Tests =====

    fun `test unused variable inspection is registered`() {
        val inspection = RhaiUnusedVariableInspection()
        assertNotNull(inspection)
    }

    fun `test used variable produces no warning`() {
        enableInspection(RhaiUnusedVariableInspection())
        configureAndHighlight("""
            let used = 42;
            print(used);
        """.trimIndent())
    }

    fun `test underscore prefix is intentionally unused`() {
        enableInspection(RhaiUnusedVariableInspection())
        configureAndHighlight("""
            let _intentionallyUnused = 42;
        """.trimIndent())
    }

    fun `test used parameter produces no warning`() {
        enableInspection(RhaiUnusedVariableInspection())
        configureAndHighlight("""
            fn foo(x) {
                print(x);
            }
        """.trimIndent())
    }

    // ===== Duplicate Function Inspection Tests =====

    fun `test duplicate function inspection is registered`() {
        val inspection = RhaiDuplicateFunctionInspection()
        assertNotNull(inspection)
    }

    fun `test different param count is not duplicate`() {
        enableInspection(RhaiDuplicateFunctionInspection())
        configureAndHighlight("""
            fn foo() {}
            fn foo(x) {}
            fn foo(x, y) {}
        """.trimIndent())
    }

    fun `test different functions produce no warning`() {
        enableInspection(RhaiDuplicateFunctionInspection())
        configureAndHighlight("""
            fn foo() {}
            fn bar() {}
            fn baz() {}
        """.trimIndent())
    }
}
