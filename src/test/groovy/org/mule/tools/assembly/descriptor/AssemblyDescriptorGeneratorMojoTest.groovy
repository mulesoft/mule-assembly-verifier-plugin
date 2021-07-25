/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.testing.MojoRule
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.mule.tools.assembly.compress.ArchiveUtils.extractZip

class AssemblyDescriptorGeneratorMojoTest {

    private static final String DESCRIPTOR_TEST_RESOURCES_PATH = "/descriptor"
    private static final String PROJECT_BASE_DIR = "${DESCRIPTOR_TEST_RESOURCES_PATH}/descriptor-mojo-test-project"
    private static final String FAILING_PROJECT_BASE_DIR =
            "${DESCRIPTOR_TEST_RESOURCES_PATH}/descriptor-mojo-failing-test-project"
    private static final String NO_ATTACHMENT_PROJECT_BASE_DIR =
            "${DESCRIPTOR_TEST_RESOURCES_PATH}/descriptor-mojo-no-attachment-test-project"
    private static final String EXPECTED_ZIP_DESCRIPTOR_PATH =
            "${DESCRIPTOR_TEST_RESOURCES_PATH}/expected-assembly-descriptor.yaml"
    private static final String GENERATED_DESCRIPTOR_JAR_PATH =
            "${PROJECT_BASE_DIR}/target/mule-assembly-descriptor-temp/mule-assembly-descriptor-1.0.0.jar"
    private static final String GENERATED_DESCRIPTOR_NO_ATTACHMENT_PROJECT_JAR_PATH =
            "${NO_ATTACHMENT_PROJECT_BASE_DIR}/target/mule-assembly-descriptor-temp/mule-assembly-descriptor-1.0.0.jar"

    private static final File projectBaseDir = getResourceFile(PROJECT_BASE_DIR)
    private static final File failingProjectBaseDir = getResourceFile(FAILING_PROJECT_BASE_DIR)
    private static final File noAttachmentProjectBaseDir = getResourceFile(NO_ATTACHMENT_PROJECT_BASE_DIR)
    private static final File expectedZipDescriptor = getResourceFile(EXPECTED_ZIP_DESCRIPTOR_PATH)

    @Rule
    public MojoRule rule = new MojoRule()

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Test
    void descriptorExecutionTest() throws Exception {
        AssemblyDescriptorGeneratorMojo generatorMojo = rule.lookupConfiguredMojo(projectBaseDir, "generate-descriptor")
        assertThat(generatorMojo).isNotNull()

        MavenProject project = rule.readMavenProject(projectBaseDir)
        generatorMojo.project = project

        generatorMojo.execute()

        File generatedDescriptorArchive = getResourceFile(GENERATED_DESCRIPTOR_JAR_PATH)
        assertThatDescriptorWasGeneratedWithExpectedContent(generatedDescriptorArchive)

        assertThat(project.getAttachedArtifacts())
                .extractingResultOf("toString")
                .containsOnly("org.mule.tools:descriptor-mojo-test-project:jar:assembly-descriptor:1.0.0")
    }

    @Test
    void descriptorExecutionWithNoArtifactAttachmentToProjectTest() throws Exception {
        AssemblyDescriptorGeneratorMojo generatorMojo =
                rule.lookupConfiguredMojo(noAttachmentProjectBaseDir, "generate-descriptor")
        assertThat(generatorMojo).isNotNull()

        MavenProject project = rule.readMavenProject(noAttachmentProjectBaseDir)
        generatorMojo.project = project

        configureMojoWithProjectConfig(generatorMojo, noAttachmentProjectBaseDir)

        generatorMojo.execute()

        File generatedDescriptorArchive = getResourceFile(GENERATED_DESCRIPTOR_NO_ATTACHMENT_PROJECT_JAR_PATH)
        assertThatDescriptorWasGeneratedWithExpectedContent(generatedDescriptorArchive)

        assertThat(project.getAttachedArtifacts()).isEmpty()
    }

    @Test
    void descriptorFailingExecutionTest() throws Exception {
        AssemblyDescriptorGeneratorMojo generatorMojo = rule.lookupConfiguredMojo(failingProjectBaseDir, "generate-descriptor")

        assertThat(generatorMojo).isNotNull()
        assertThatThrownBy(() -> generatorMojo.execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("descriptor-mojo-failing-test-project-1.0.0.zip (Assembly file does not exist)")
    }

    private static File getResourceFile(String resourcePath) {
        return new File(getClass().getResource(resourcePath).toURI())
    }

    private void configureMojoWithProjectConfig(AssemblyDescriptorGeneratorMojo generatorMojo, File baseProject) {
        File pom = new File(baseProject, "pom.xml")
        assertThat(pom).exists().isFile()

        PlexusConfiguration configuration = rule.extractPluginConfiguration("mule-assembly-verifier", pom)
        rule.configureMojo(generatorMojo, configuration)
    }

    private void assertThatDescriptorWasGeneratedWithExpectedContent(File generatedDescriptorArchive) {
        assertThat(generatedDescriptorArchive).isFile().exists()

        File extractedDescriptorJar = tempFolder.newFolder()
        extractZip(generatedDescriptorArchive, extractedDescriptorJar)

        assertThat(extractedDescriptorJar.list()).hasSize(1)
        assertThat(new File(extractedDescriptorJar, "assembly-descriptor.yaml"))
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedZipDescriptor)
    }

}
