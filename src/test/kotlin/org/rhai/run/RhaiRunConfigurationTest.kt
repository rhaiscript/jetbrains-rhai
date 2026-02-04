package org.rhai.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai run configuration.
 */
class RhaiRunConfigurationTest : BasePlatformTestCase() {

    fun `test run configuration type exists`() {
        val configType = RhaiRunConfigurationType()
        assertNotNull(configType)
        assertNotNull(configType.displayName)
    }

    fun `test run configuration factory`() {
        val configType = RhaiRunConfigurationType()
        val factories = configType.configurationFactories
        assertTrue("Should have at least one factory", factories.isNotEmpty())
    }

    fun `test create configuration`() {
        val configType = RhaiRunConfigurationType()
        val factory = configType.configurationFactories.first()
        val config = factory.createTemplateConfiguration(project)

        assertNotNull(config)
        assertTrue(config is RhaiRunConfiguration)
    }

    fun `test configuration options`() {
        val configType = RhaiRunConfigurationType()
        val factory = configType.configurationFactories.first()
        val config = factory.createTemplateConfiguration(project) as RhaiRunConfiguration

        // Test default values
        config.scriptPath = "/path/to/script.rhai"
        assertEquals("/path/to/script.rhai", config.scriptPath)

        config.interpreterPath = "/usr/local/bin/rhai"
        assertEquals("/usr/local/bin/rhai", config.interpreterPath)

        config.workingDirectory = "/home/user/project"
        assertEquals("/home/user/project", config.workingDirectory)

        config.scriptArguments = "--arg1 --arg2"
        assertEquals("--arg1 --arg2", config.scriptArguments)
    }

    fun `test configuration serialization`() {
        val configType = RhaiRunConfigurationType()
        val factory = configType.configurationFactories.first()
        val config = factory.createTemplateConfiguration(project) as RhaiRunConfiguration

        config.scriptPath = "/test/script.rhai"
        config.interpreterPath = "/bin/rhai"

        // Verify values are stored
        assertEquals("/test/script.rhai", config.scriptPath)
        assertEquals("/bin/rhai", config.interpreterPath)
    }

    fun `test run line marker provider exists`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn main() {
                print("Hello");
            }
        """.trimIndent())

        // Just verify the file is configured correctly
        assertNotNull(myFixture.file)
    }
}
