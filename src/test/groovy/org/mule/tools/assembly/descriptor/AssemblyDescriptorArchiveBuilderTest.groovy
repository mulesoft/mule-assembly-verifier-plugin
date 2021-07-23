/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor


import org.apache.maven.plugin.logging.SystemStreamLog
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.mule.tools.assembly.compress.ArchiveUtils.extractZip

class AssemblyDescriptorArchiveBuilderTest {

    private static final String DESCRIPTOR_TEST_RESOURCES_PATH = "/descriptor"
    private static final String ASSEMBLY_DESCRIPTOR_JAR_NAME = "mule-assembly-descriptor-1.0.0-SNAPSHOT.jar"
    private static final String ZIP_DESCRIPTOR_FILE_NAME = "expected-assembly-descriptor.yaml"
    private static final String ZIP_DESCRIPTOR_RESOURCE_PATH = "${DESCRIPTOR_TEST_RESOURCES_PATH}/${ZIP_DESCRIPTOR_FILE_NAME}"

    private static final File zipAssemblyDescriptor = new File(getClass().getResource(ZIP_DESCRIPTOR_RESOURCE_PATH).toURI())

    AssemblyDescriptorArchiveBuilder archiveBuilder;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Before
    void setUp() throws Exception {
        archiveBuilder = new AssemblyDescriptorArchiveBuilder(log: new SystemStreamLog(), workingDir: tempFolder.newFolder())
    }

    @Test
    void buildDescriptorArchiveTest() {
        File descriptorJarFile = archiveBuilder.buildDescriptorArchive(zipAssemblyDescriptor, ASSEMBLY_DESCRIPTOR_JAR_NAME)
        assertThat(descriptorJarFile).isFile().hasExtension("jar")

        File jarExtractLocation = tempFolder.newFolder()
        extractZip(descriptorJarFile, jarExtractLocation)
        assertThat(jarExtractLocation.list()).hasSize(1)

        File generatedDescriptorYaml = new File(jarExtractLocation, ZIP_DESCRIPTOR_FILE_NAME)
        assertThat(generatedDescriptorYaml)
                .exists()
                .isFile()
                .hasSameTextualContentAs(zipAssemblyDescriptor)
    }
}
