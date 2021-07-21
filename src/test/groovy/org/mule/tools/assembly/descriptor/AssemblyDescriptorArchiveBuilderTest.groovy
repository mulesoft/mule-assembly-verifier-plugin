/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.logging.SystemStreamLog
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.mule.tools.assembly.compress.ArchiveUtils.extractZip

class AssemblyDescriptorArchiveBuilderTest {

    static final Log logger = new SystemStreamLog()

    static final String ASSEMBLY_DESCRIPTOR_JAR_NAME = "mule-assembly-descriptor-1.0.0-SNAPSHOT.jar"
    static final String ASSEMBLY_TEMP_DIR = "mule-assembly-descriptor-temp"
    static final String ZIP_DESCRIPTOR_FILE_NAME = "zip-assembly-descriptor.yaml"

    static final File zipAssemblyDescriptor = new File(getClass().getResource("/${ZIP_DESCRIPTOR_FILE_NAME}").toURI())

    File descriptorTempDir
    AssemblyDescriptorArchiveBuilder archiveBuilder;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Before
    void setUp() throws Exception {
        descriptorTempDir = tempFolder.newFolder(ASSEMBLY_TEMP_DIR)
        archiveBuilder = new AssemblyDescriptorArchiveBuilder(log: logger, workingDir: descriptorTempDir)
    }

    @Test
    void buildDescriptorArchiveTest() {
        File jarExtractLocation = tempFolder.newFolder("jar-extract-location")

        File descriptorJarFile = archiveBuilder.buildDescriptorArchive(zipAssemblyDescriptor, ASSEMBLY_DESCRIPTOR_JAR_NAME)
        assertThat(descriptorJarFile).isFile().hasExtension("jar")

        extractZip(descriptorJarFile, jarExtractLocation)
        File extractedDescriptorFile = new File(jarExtractLocation, ZIP_DESCRIPTOR_FILE_NAME)
        assertThat(extractedDescriptorFile)
                .exists()
                .isFile()
                .hasSameTextualContentAs(zipAssemblyDescriptor)
    }
}
