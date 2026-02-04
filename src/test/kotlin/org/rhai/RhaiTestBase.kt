package org.rhai

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Base class for Rhai plugin tests.
 * Provides common setup and utility methods.
 */
abstract class RhaiTestBase : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/resources/testData"

    /**
     * Configure the fixture with Rhai code.
     */
    protected fun configureByText(code: String) {
        myFixture.configureByText(RhaiFileType.INSTANCE, code)
    }

    /**
     * Configure the fixture with Rhai code and place caret at <caret> marker.
     */
    protected fun configureByTextWithCaret(code: String) {
        myFixture.configureByText(RhaiFileType.INSTANCE, code)
    }

    /**
     * Get the text after the fixture has been modified.
     */
    protected fun getFileText(): String = myFixture.file.text

    /**
     * Type text at the current caret position.
     */
    protected fun type(text: String) {
        myFixture.type(text)
    }

    /**
     * Complete and get lookup strings.
     */
    protected fun completeBasic(): List<String> {
        return myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
    }
}
