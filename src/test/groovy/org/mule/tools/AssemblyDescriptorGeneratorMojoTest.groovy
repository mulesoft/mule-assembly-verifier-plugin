package org.mule.tools

import org.apache.maven.plugin.testing.MojoRule
import org.junit.Rule
import org.junit.Test

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class AssemblyDescriptorGeneratorMojoTest {

    @Rule
    public MojoRule rule = new MojoRule()

    @Test
    void verifyExecutionTest() throws Exception {
        File pom = new File("target/test-classes/descriptor-mojo-test-project/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        AssemblyDescriptorGeneratorMojo verifierMojo = (AssemblyDescriptorGeneratorMojo) rule.lookupConfiguredMojo(pom, "descriptor");
        assertNotNull(verifierMojo);
        verifierMojo.execute();
    }

}
