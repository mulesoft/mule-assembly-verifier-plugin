/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor


import org.apache.maven.plugin.testing.MojoRule
import org.apache.maven.project.MavenProject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.mule.tools.assembly.compress.ArchiveUtils.extractZip

class AssemblyDescriptorGeneratorMojoTest {

    private static final String DESCRIPTOR_TEST_RESOURCES_PATH = "/descriptor"
    private static final String PROJECT_BASE_DIR = "${DESCRIPTOR_TEST_RESOURCES_PATH}/descriptor-mojo-test-project"
    private static final String EXPECTED_ZIP_DESCRIPTOR_PATH =
            "${DESCRIPTOR_TEST_RESOURCES_PATH}/expected-assembly-descriptor.yaml"
    private static final String GENERATED_DESCRIPTOR_JAR_PATH =
            "${PROJECT_BASE_DIR}/target/mule-assembly-descriptor-temp/mule-assembly-descriptor-1.0.0.jar"

    private static final File projectBaseDir = new File(getClass().getResource(PROJECT_BASE_DIR).toURI())
    private static final File expectedZipDescriptor = new File(getClass().getResource(EXPECTED_ZIP_DESCRIPTOR_PATH).toURI())

    @Rule
    public MojoRule rule = new MojoRule()

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Test
    void verifyExecutionTest() throws Exception {
        AssemblyDescriptorGeneratorMojo generatorMojo = rule.lookupConfiguredMojo(projectBaseDir, "generate-descriptor")
        MavenProject project = rule.readMavenProject(projectBaseDir)

        assertThat(generatorMojo).isNotNull()
        generatorMojo.project = project
        generatorMojo.execute()

        File generatedDescriptorArchive = new File(getClass().getResource(GENERATED_DESCRIPTOR_JAR_PATH).toURI())
        assertThat(generatedDescriptorArchive).isFile().exists()

        File extractedDescriptorJar = tempFolder.newFolder()
        extractZip(generatedDescriptorArchive, extractedDescriptorJar)

        assertThat(extractedDescriptorJar.list()).hasSize(1)
        assertThat(new File(extractedDescriptorJar, "assembly-descriptor.yaml"))
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedZipDescriptor)

        assertThat(project.getAttachedArtifacts())
                .extractingResultOf("toString")
                .containsOnly("org.mule.tools:descriptor-mojo-test-project:jar:assembly-descriptor:1.0.0")
    }
}
