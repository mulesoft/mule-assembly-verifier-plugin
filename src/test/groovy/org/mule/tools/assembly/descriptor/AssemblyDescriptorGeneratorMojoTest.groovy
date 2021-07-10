package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.testing.MojoRule
import org.junit.Rule
import org.junit.Test

class AssemblyDescriptorGeneratorMojoTest {

    @Rule
    public MojoRule rule = new MojoRule()

    @Test
    void verifyExecutionTest() throws Exception {
        File pom = new File("target/test-classes/descriptor-mojo-test-project/");
        org.junit.Assert.assertNotNull(pom);
        org.junit.Assert.assertTrue(pom.exists());

        AssemblyDescriptorGeneratorMojo verifierMojo = (AssemblyDescriptorGeneratorMojo) rule.lookupConfiguredMojo(pom, "descriptor");
        org.junit.Assert.assertNotNull(verifierMojo);
        verifierMojo.execute();
    }

}
