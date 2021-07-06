package org.mule.tools

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.testing.MojoRule
import org.junit.Rule
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class AssemblyContentsVerifierMojoTest {

    @Rule
    public MojoRule rule = new MojoRule()

    @Test
    void verifyExecutionTest() throws Exception {
        File pom = new File("target/test-classes/verify-mojo-test-project/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(pom, "verify");
        assertNotNull(verifierMojo);
        verifierMojo.execute();
    }

    @Test
    void verifyFiledExecutionTest() throws Exception {
        File pom = new File("target/test-classes/verify-mojo-test-failing-project/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(pom, "verify");
        assertNotNull(verifierMojo);

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoFailureException.class)
                .hasMessageMatching("(?s)The following problems have been encountered:\\s+" +
                        "Missing from the Distribution:\\s+" +
                        "1\\. /the-assembly-1.0.0/lib/opt/another-jar.jar\\s+" +
                        "Unexpected entries in the Distribution:\\s+" +
                        "1\\. /the-assembly-1.0.0/lib/opt/a-jar.jar\\s+")
    }
}
