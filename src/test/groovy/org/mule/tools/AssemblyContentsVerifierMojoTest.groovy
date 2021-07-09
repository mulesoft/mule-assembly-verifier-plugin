package org.mule.tools

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.testing.MojoRule
import org.junit.Rule
import org.junit.Test

import java.nio.file.Paths

import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class AssemblyContentsVerifierMojoTest {

    @Rule
    public MojoRule rule = new MojoRule()

    @Test
    void verifyExecutionTest() throws Exception {
        File pom = Paths.get("target", "test-classes", "verify-mojo-test-project").toFile()
        assertNotNull(pom)
        assertTrue(pom.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(pom, "verify")
        assertNotNull(verifierMojo)
        verifierMojo.execute()
    }

    @Test
    void verifyExecutionMaven2StyleSnapshotsTest() throws Exception {
        File pom = Paths.get("target", "test-classes", "verify-mojo-maven2-style-snapshots-test-project").toFile()
        assertNotNull(pom)
        assertTrue(pom.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(pom, "verify")
        assertNotNull(verifierMojo)
        verifierMojo.execute()
    }

    @Test
    void verifyFiledExecutionTest() throws Exception {
        File pom = Paths.get("target", "test-classes", "verify-mojo-test-failing-project").toFile()
        assertNotNull(pom)
        assertTrue(pom.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(pom, "verify")
        assertNotNull(verifierMojo)

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoFailureException.class)
                .hasMessageMatching("(?s)The following problems have been encountered:\\s+" +
                        "Missing from the Distribution:\\s+" +
                        "1\\. /the-assembly-1.0.0/lib/opt/missing.jar\\s+" +
                        "Unexpected entries in the Distribution:\\s+" +
                        "1\\. /the-assembly-1.0.0/lib/opt/unexpected.jar\\s+" +
                        "Duplicate entries in the Distribution:\\s+" +
                        "1\\. the-assembly-1.0.0/duplicated/duplicated-1.0.0-SNAPSHOT.jar\\s+")
    }

    @Test
    void missingAllowlistTest() throws Exception {
        File pom = Paths.get("target", "test-classes", "verify-mojo-test-missing-allowlist-project").toFile()
        assertNotNull(pom)
        assertTrue(pom.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(pom, "verify")
        assertNotNull(verifierMojo)

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageMatching("Allowlist file .*assembly-allowlist.txt does not exist.")
    }

    @Test
    void missingAssemblyTest() throws Exception {
        File pom = Paths.get("target", "test-classes", "verify-mojo-missing-assembly-test-project").toFile()
        assertNotNull(pom)
        assertTrue(pom.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(pom, "verify")
        assertNotNull(verifierMojo)

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageMatching("Output file .*zip does not exist.")
    }
}
