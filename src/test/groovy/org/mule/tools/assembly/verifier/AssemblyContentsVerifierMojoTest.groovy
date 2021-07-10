package org.mule.tools.assembly.verifier

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.testing.MojoRule
import org.codehaus.plexus.configuration.PlexusConfiguration
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
        File baseDir = Paths.get("target", "test-classes", "verify-mojo-test-project").toFile()
        assertNotNull(baseDir)
        assertTrue(baseDir.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)
        verifierMojo.execute()
    }

    @Test
    void verifyExecutionMaven2StyleSnapshotsTest() throws Exception {
        File baseDir = Paths.get("target", "test-classes", "verify-mojo-maven2-style-snapshots-test-project").toFile()
        assertNotNull(baseDir)
        assertTrue(baseDir.exists())

        File pom = new File(baseDir, "pom.xml")
        assertNotNull(pom)
        assertTrue(pom.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)

        PlexusConfiguration configuration = rule.extractPluginConfiguration("mule-assembly-verifier", pom)
        rule.configureMojo(verifierMojo, configuration)

        verifierMojo.execute()
    }

    @Test
    void verifyFiledExecutionTest() throws Exception {
        File baseDir = Paths.get("target", "test-classes", "verify-mojo-test-failing-project").toFile()
        assertNotNull(baseDir)
        assertTrue(baseDir.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
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
        File baseDir = Paths.get("target", "test-classes", "verify-mojo-test-missing-allowlist-project").toFile()
        assertNotNull(baseDir)
        assertTrue(baseDir.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageMatching("Allowlist file .*assembly-allowlist.txt does not exist.")
    }

    @Test
    void missingAssemblyTest() throws Exception {
        File baseDir = Paths.get("target", "test-classes", "verify-mojo-missing-assembly-test-project").toFile()
        assertNotNull(baseDir)
        assertTrue(baseDir.exists())

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageMatching("Output file .*zip does not exist.")
    }
}
