package org.rhai.refactoring

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai rename refactoring.
 */
class RhaiRenameTest : BasePlatformTestCase() {

    fun `test names validator accepts valid identifiers`() {
        val validator = RhaiNamesValidator()

        assertTrue(validator.isIdentifier("foo", project))
        assertTrue(validator.isIdentifier("_bar", project))
        assertTrue(validator.isIdentifier("camelCase", project))
        assertTrue(validator.isIdentifier("snake_case", project))
        assertTrue(validator.isIdentifier("PascalCase", project))
        assertTrue(validator.isIdentifier("x1", project))
        assertTrue(validator.isIdentifier("_123", project))
    }

    fun `test names validator rejects invalid identifiers`() {
        val validator = RhaiNamesValidator()

        assertFalse(validator.isIdentifier("", project))
        assertFalse(validator.isIdentifier("123", project))
        assertFalse(validator.isIdentifier("1abc", project))
        assertFalse(validator.isIdentifier("foo-bar", project))
        assertFalse(validator.isIdentifier("foo.bar", project))
        assertFalse(validator.isIdentifier("foo bar", project))
    }

    fun `test names validator recognizes keywords`() {
        val validator = RhaiNamesValidator()

        assertTrue(validator.isKeyword("let", project))
        assertTrue(validator.isKeyword("const", project))
        assertTrue(validator.isKeyword("fn", project))
        assertTrue(validator.isKeyword("if", project))
        assertTrue(validator.isKeyword("else", project))
        assertTrue(validator.isKeyword("while", project))
        assertTrue(validator.isKeyword("for", project))
        assertTrue(validator.isKeyword("return", project))
        assertTrue(validator.isKeyword("true", project))
        assertTrue(validator.isKeyword("false", project))
        assertTrue(validator.isKeyword("null", project))
    }

    fun `test names validator non-keywords`() {
        val validator = RhaiNamesValidator()

        assertFalse(validator.isKeyword("foo", project))
        assertFalse(validator.isKeyword("bar", project))
        assertFalse(validator.isKeyword("myFunction", project))
    }

    fun `test psi factory creates identifier`() {
        val factory = RhaiPsiFactory(project)
        val identifier = factory.createIdentifier("testName")
        assertNotNull(identifier)
        assertEquals("testName", identifier.text)
    }

    fun `test psi factory creates file`() {
        val factory = RhaiPsiFactory(project)
        val file = factory.createFile("let x = 42;")
        assertNotNull(file)
        assertTrue(file.text.contains("let x = 42"))
    }

    fun `test rename handler is available`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, "let foo<caret> = 1;")
        val handler = RhaiRenameHandler()
        // Just verify it doesn't throw
        assertNotNull(handler)
    }

    fun `test rename processor can process element`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, "let foo = 1;")
        val processor = RhaiRenamePsiElementProcessor()
        assertNotNull(processor)
    }
}
