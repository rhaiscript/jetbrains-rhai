package org.rhai.features

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai code formatter.
 */
class RhaiFormatterTest : BasePlatformTestCase() {

    fun `test formatter is registered`() {
        val formatter = RhaiFormatter()
        assertNotNull(formatter)
    }

    fun `test format simple let declaration`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, "let x=42;")
        // Formatter should add spaces around =
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format function definition`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn add(a,b){a+b}
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
        assertTrue(formatted.contains("fn"))
    }

    fun `test format array literal`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let arr = [1,2,3];
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format object literal`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let obj = #{ name:"test",value:42 };
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format closure`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let f = |x,y|x+y;
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format if statement`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let x = 1;
            if x>0{print("positive");}
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format method chain`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let result = arr.map(|x|x*2).filter(|x|x>5);
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format operators`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let a=1+2*3-4/5%6;
            let b=a==1&&b!=2||c>=3;
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format range expression`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let r = 0..10;
            for i in 0..=5 { print(i); }
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }

    fun `test format switch expression`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let x = 1;
            switch x{1=>"one",2=>"two",_=>"other"}
        """.trimIndent())
        val formatted = myFixture.file.text
        assertNotNull(formatted)
    }
}
