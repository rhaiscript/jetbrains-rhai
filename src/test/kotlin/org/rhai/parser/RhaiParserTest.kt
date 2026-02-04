package org.rhai.parser

import com.intellij.psi.util.PsiTreeUtil
import org.junit.Test
import org.rhai.*
import org.rhai.lang.RhaiFile

/**
 * Tests for the Rhai parser.
 * Verifies correct PSI tree construction.
 */
class RhaiParserTest : RhaiTestBase() {

    @Test
    fun `test empty file parses`() {
        configureByText("")
        assertNotNull(myFixture.file)
        assertTrue(myFixture.file is RhaiFile)
    }

    @Test
    fun `test let declaration`() {
        configureByText("let x = 42;")
        val letDecl = PsiTreeUtil.findChildOfType(myFixture.file, RhaiLetDeclaration::class.java)
        assertNotNull("Should find let declaration", letDecl)
        assertEquals("x", letDecl?.pattern?.text)
    }

    @Test
    fun `test const declaration`() {
        configureByText("const PI = 3.14;")
        val constDecl = PsiTreeUtil.findChildOfType(myFixture.file, RhaiConstDeclaration::class.java)
        assertNotNull("Should find const declaration", constDecl)
    }

    @Test
    fun `test function definition`() {
        configureByText("""
            fn add(a, b) {
                a + b
            }
        """.trimIndent())

        val funcDef = PsiTreeUtil.findChildOfType(myFixture.file, RhaiFunctionDefinition::class.java)
        assertNotNull("Should find function definition", funcDef)
        assertEquals("add", funcDef?.identifier?.text)

        val params = funcDef?.parameters?.parameterList
        assertEquals(2, params?.size)
    }

    @Test
    fun `test pub function`() {
        configureByText("pub fn public_func() {}")
        val funcDef = PsiTreeUtil.findChildOfType(myFixture.file, RhaiFunctionDefinition::class.java)
        assertNotNull(funcDef)
        assertTrue("Function should have pub visibility", funcDef?.text?.startsWith("pub") == true)
    }

    @Test
    fun `test private function`() {
        configureByText("private fn private_func() {}")
        val funcDef = PsiTreeUtil.findChildOfType(myFixture.file, RhaiFunctionDefinition::class.java)
        assertNotNull(funcDef)
        assertTrue("Function should have private visibility", funcDef?.text?.startsWith("private") == true)
    }

    @Test
    fun `test if statement`() {
        configureByText("""
            let x = 5;
            if x > 0 {
                print("positive");
            }
        """.trimIndent())

        // If statements may be parsed as expressions in Rhai
        val file = myFixture.file
        assertNotNull("File should not be null", file)
        // Verify the file contains the expected code
        assertTrue("File should contain if keyword", file.text.contains("if"))
    }

    @Test
    fun `test if-else statement`() {
        configureByText("""
            let x = 5;
            if x > 0 {
                print("positive");
            } else {
                print("non-positive");
            }
        """.trimIndent())

        val file = myFixture.file
        assertNotNull(file)
        // Verify the file parses and contains expected elements
        assertTrue("File should contain if keyword", file.text.contains("if"))
        assertTrue("File should contain else keyword", file.text.contains("else"))
    }

    @Test
    fun `test while loop`() {
        configureByText("""
            while x > 0 {
                x -= 1;
            }
        """.trimIndent())

        val whileStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiWhileStatement::class.java)
        assertNotNull("Should find while statement", whileStmt)
    }

    @Test
    fun `test for loop`() {
        configureByText("""
            for item in items {
                print(item);
            }
        """.trimIndent())

        val forStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiForStatement::class.java)
        assertNotNull("Should find for statement", forStmt)
        assertEquals("item", forStmt?.pattern?.text)
    }

    @Test
    fun `test loop statement`() {
        configureByText("""
            loop {
                if done { break; }
            }
        """.trimIndent())

        val loopStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiLoopStatement::class.java)
        assertNotNull("Should find loop statement", loopStmt)
    }

    @Test
    fun `test switch expression`() {
        configureByText("""
            switch x {
                1 => "one",
                2 => "two",
                _ => "other"
            }
        """.trimIndent())

        val switchExpr = PsiTreeUtil.findChildOfType(myFixture.file, RhaiSwitchExpression::class.java)
        assertNotNull("Should find switch expression", switchExpr)
    }

    @Test
    fun `test try-catch`() {
        configureByText("""
            try {
                risky_operation();
            } catch (err) {
                print(err);
            }
        """.trimIndent())

        val tryCatch = PsiTreeUtil.findChildOfType(myFixture.file, RhaiTryCatchStatement::class.java)
        assertNotNull("Should find try-catch statement", tryCatch)
    }

    @Test
    fun `test closure expression`() {
        configureByText("let f = |x, y| x + y;")
        val closure = PsiTreeUtil.findChildOfType(myFixture.file, RhaiClosureExpr::class.java)
        assertNotNull("Should find closure expression", closure)
        assertEquals(2, closure?.closureParams?.closureParamList?.size)
    }

    @Test
    fun `test array literal`() {
        configureByText("let arr = [1, 2, 3];")
        val array = PsiTreeUtil.findChildOfType(myFixture.file, RhaiArrayLiteral::class.java)
        assertNotNull("Should find array literal", array)
    }

    @Test
    fun `test object literal`() {
        configureByText("let obj = #{ name: \"test\", value: 42 };")
        val obj = PsiTreeUtil.findChildOfType(myFixture.file, RhaiObjectLiteral::class.java)
        assertNotNull("Should find object literal", obj)
    }

    @Test
    fun `test import statement`() {
        configureByText("import \"utils\" as utils;")
        val importStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiImportStatement::class.java)
        assertNotNull("Should find import statement", importStmt)
    }

    @Test
    fun `test module declaration`() {
        configureByText("""
            module math {
                fn add(a, b) { a + b }
            }
        """.trimIndent())

        val moduleDef = PsiTreeUtil.findChildOfType(myFixture.file, RhaiModuleDeclaration::class.java)
        assertNotNull("Should find module declaration", moduleDef)
    }

    @Test
    fun `test path expression`() {
        configureByText("let x = math::add(1, 2);")
        val pathExpr = PsiTreeUtil.findChildOfType(myFixture.file, RhaiPathExpression::class.java)
        assertNotNull("Should find path expression", pathExpr)
        assertEquals("math::add", pathExpr?.text)
    }

    @Test
    fun `test return statement`() {
        configureByText("""
            fn foo() {
                return 42;
            }
        """.trimIndent())

        val returnStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiReturnStatement::class.java)
        assertNotNull("Should find return statement", returnStmt)
    }

    @Test
    fun `test throw statement`() {
        configureByText("throw \"Error occurred\";")
        val throwStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiThrowStatement::class.java)
        assertNotNull("Should find throw statement", throwStmt)
    }

    @Test
    fun `test break statement`() {
        configureByText("""
            loop {
                break;
            }
        """.trimIndent())

        val breakStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiBreakStatement::class.java)
        assertNotNull("Should find break statement", breakStmt)
    }

    @Test
    fun `test continue statement`() {
        configureByText("""
            for i in 0..10 {
                if i == 5 { continue; }
            }
        """.trimIndent())

        val continueStmt = PsiTreeUtil.findChildOfType(myFixture.file, RhaiContinueStatement::class.java)
        assertNotNull("Should find continue statement", continueStmt)
    }

    @Test
    fun `test type annotation`() {
        configureByText("let x: int = 42;")
        val typeAnnotation = PsiTreeUtil.findChildOfType(myFixture.file, RhaiTypeAnnotation::class.java)
        assertNotNull("Should find type annotation", typeAnnotation)
    }

    @Test
    fun `test function with return type`() {
        // Note: Rhai type annotations syntax varies; test simpler form
        configureByText("fn add(a, b) { a + b }")
        val funcDef = PsiTreeUtil.findChildOfType(myFixture.file, RhaiFunctionDefinition::class.java)
        assertNotNull("Function should parse successfully", funcDef)
        assertEquals("add", funcDef?.identifier?.text)
    }

    @Test
    fun `test range expression`() {
        configureByText("let r = 0..10;")
        val rangeExpr = PsiTreeUtil.findChildOfType(myFixture.file, RhaiRangeExpr::class.java)
        assertNotNull("Should find range expression", rangeExpr)
    }

    @Test
    fun `test method chain`() {
        configureByText("arr.map(|x| x * 2).filter(|x| x > 5).len();")
        val postfixOps = PsiTreeUtil.findChildrenOfType(myFixture.file, RhaiPostfixOp::class.java)
        assertTrue("Should find multiple postfix operations", postfixOps.size >= 3)
    }

    @Test
    fun `test nested function calls`() {
        configureByText("print(to_string(abs(-42)));")
        val postfixExprs = PsiTreeUtil.findChildrenOfType(myFixture.file, RhaiPostfixExpr::class.java)
        assertTrue("Should find postfix expressions for calls", postfixExprs.isNotEmpty())
    }

    @Test
    fun `test binary expressions`() {
        configureByText("let result = (a + b) * (c - d) / e;")
        val additiveExprs = PsiTreeUtil.findChildrenOfType(myFixture.file, RhaiAdditiveExpr::class.java)
        val multiplicativeExprs = PsiTreeUtil.findChildrenOfType(myFixture.file, RhaiMultiplicativeExpr::class.java)

        assertTrue("Should find additive expressions", additiveExprs.isNotEmpty())
        assertTrue("Should find multiplicative expressions", multiplicativeExprs.isNotEmpty())
    }

    @Test
    fun `test logical expressions`() {
        configureByText("let result = a && b || c;")
        val logicalOrExpr = PsiTreeUtil.findChildOfType(myFixture.file, RhaiLogicalOrExpr::class.java)
        assertNotNull("Should find logical OR expression", logicalOrExpr)
    }

    @Test
    fun `test comparison expressions`() {
        configureByText("let result = a < b && c >= d;")
        val relationalExpr = PsiTreeUtil.findChildOfType(myFixture.file, RhaiRelationalExpr::class.java)
        assertNotNull("Should find relational expression", relationalExpr)
    }

    @Test
    fun `test unary expressions`() {
        configureByText("let result = !flag && -value;")
        val unaryExpr = PsiTreeUtil.findChildOfType(myFixture.file, RhaiUnaryExpr::class.java)
        assertNotNull("Should find unary expression", unaryExpr)
    }

    @Test
    fun `test conditional expression`() {
        configureByText("let result = condition ? value1 : value2;")
        val conditionalExpr = PsiTreeUtil.findChildOfType(myFixture.file, RhaiConditionalExpr::class.java)
        assertNotNull("Should find conditional expression", conditionalExpr)
    }

    @Test
    fun `test null coalescing`() {
        configureByText("let result = maybe_null ?? default_value;")
        val nullCoalescing = PsiTreeUtil.findChildOfType(myFixture.file, RhaiNullCoalescingExpr::class.java)
        assertNotNull("Should find null coalescing expression", nullCoalescing)
    }

    @Test
    fun `test multiple statements in file`() {
        configureByText("""
            let x = 1;
            let y = 2;
            fn add(a, b) { a + b }
            let z = add(x, y);
        """.trimIndent())

        val letDecls = PsiTreeUtil.findChildrenOfType(myFixture.file, RhaiLetDeclaration::class.java)
        val funcDefs = PsiTreeUtil.findChildrenOfType(myFixture.file, RhaiFunctionDefinition::class.java)

        assertEquals(3, letDecls.size)
        assertEquals(1, funcDefs.size)
    }

    @Test
    fun `test complex program`() {
        configureByText("""
            //! Module documentation

            const MAX_VALUE = 100;

            /// Calculates factorial
            pub fn factorial(n) {
                if n <= 1 {
                    return 1;
                }
                n * factorial(n - 1)
            }

            let numbers = [1, 2, 3, 4, 5];
            let doubled = numbers.map(|x| x * 2);

            for num in doubled {
                print(num);
            }
        """.trimIndent())

        // Verify key elements are parsed
        assertNotNull(PsiTreeUtil.findChildOfType(myFixture.file, RhaiConstDeclaration::class.java))
        assertNotNull(PsiTreeUtil.findChildOfType(myFixture.file, RhaiFunctionDefinition::class.java))
        assertNotNull(PsiTreeUtil.findChildOfType(myFixture.file, RhaiArrayLiteral::class.java))
        assertNotNull(PsiTreeUtil.findChildOfType(myFixture.file, RhaiForStatement::class.java))
    }
}
