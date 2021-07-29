/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.verifier

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.testing.MojoRule
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.junit.Rule
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.junit.Assert.assertNotNull

class AssemblyContentsVerifierMojoTest {

    private static final String VERIFIER_TEST_RESOURCES_PATH = "/verifier"
    public static final String VERIFY_EXECUTION_PROJECT_PATH =
            "${VERIFIER_TEST_RESOURCES_PATH}/verify-mojo-test-project"
    private static final String VERIFY_MAVEN2_TEST_PROJECT_PATH =
            "${VERIFIER_TEST_RESOURCES_PATH}/verify-mojo-maven2-style-snapshots-test-project"
    public static final String VERIFY_FAILED_EXECUTION_PROJECT_PATH =
            "${VERIFIER_TEST_RESOURCES_PATH}/verify-mojo-test-failing-project"
    public static final String MISSING_ALLOWLIST_PROJECT_PATH =
            "${VERIFIER_TEST_RESOURCES_PATH}/verify-mojo-test-missing-allowlist-project"
    public static final String MISSING_ASSEMBLY_PROJECT_PATH =
            "${VERIFIER_TEST_RESOURCES_PATH}/verify-mojo-missing-assembly-test-project"

    @Rule
    public MojoRule rule = new MojoRule()

    @Test
    void verifyExecutionTest() throws Exception {
        File baseDir = new File(getClass().getResource(VERIFY_EXECUTION_PROJECT_PATH).toURI())
        assertThat(baseDir).exists().isDirectory()

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)
        verifierMojo.execute()
    }

    @Test
    void verifyExecutionMaven2StyleSnapshotsTest() throws Exception {
        File baseDir = new File(getClass().getResource(VERIFY_MAVEN2_TEST_PROJECT_PATH).toURI())
        assertThat(baseDir).exists().isDirectory()

        File pom = new File(baseDir, "pom.xml")
        assertThat(pom).exists().isFile()

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)

        PlexusConfiguration configuration = rule.extractPluginConfiguration("mule-assembly-verifier", pom)
        rule.configureMojo(verifierMojo, configuration)

        verifierMojo.execute()
    }

    @Test
    void verifyFiledExecutionTest() throws Exception {
        File baseDir = new File(getClass().getResource(VERIFY_FAILED_EXECUTION_PROJECT_PATH).toURI())
        assertThat(baseDir).exists().isDirectory()

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
        File baseDir = new File(getClass().getResource(MISSING_ALLOWLIST_PROJECT_PATH).toURI())
        assertThat(baseDir).exists().isDirectory()

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageMatching("Allowlist file .*assembly-allowlist.txt does not exist.")
    }

    @Test
    void missingAssemblyTest() throws Exception {
        File baseDir = new File(getClass().getResource(MISSING_ASSEMBLY_PROJECT_PATH).toURI())
        assertThat(baseDir).exists().isDirectory()

        AssemblyContentsVerifierMojo verifierMojo = (AssemblyContentsVerifierMojo) rule.lookupConfiguredMojo(baseDir, "verify")
        assertNotNull(verifierMojo)

        assertThatThrownBy(() -> verifierMojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageMatching("Output file .*zip does not exist.")
    }
}
