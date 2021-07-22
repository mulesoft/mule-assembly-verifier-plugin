/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.testing.MojoRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.mule.tools.assembly.compress.ArchiveUtils.extractZip

class AssemblyDescriptorGeneratorMojoTest {

    private static final String PROJECT_BASE_DIR = "/descriptor-mojo-test-project"
    private static final String ASSEMBLY_NAME = "descriptor-mojo-test-project-1.0.0.zip"
    private static final String EXPECTED_ZIP_DESCRIPTOR_PATH = "/zip-assembly-descriptor.yaml"
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
        Mojo verifierMojo = rule.lookupConfiguredMojo(projectBaseDir, "attach-descriptor")

        assertThat(verifierMojo).isNotNull()
        verifierMojo.execute()

        File generatedDescriptorArchive = new File(getClass().getResource(GENERATED_DESCRIPTOR_JAR_PATH).toURI())
        assertThat(generatedDescriptorArchive).isFile().exists()

        File extractedDescriptorJar = tempFolder.newFolder("extracted-descriptor-jar")
        extractZip(generatedDescriptorArchive, extractedDescriptorJar)
        assertThat(new File(extractedDescriptorJar, "assembly-descriptor.yaml"))
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedZipDescriptor)
    }
}
