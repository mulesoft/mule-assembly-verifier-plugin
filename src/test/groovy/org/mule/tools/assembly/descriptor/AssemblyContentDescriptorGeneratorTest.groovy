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

class AssemblyContentDescriptorGeneratorTest {

    static final Log logger = new SystemStreamLog()

    private static final String DESCRIPTOR_TEST_RESOURCES_PATH = "/descriptor"
    private static final String ZIP_ASSEMBLY_PATH = "${DESCRIPTOR_TEST_RESOURCES_PATH}/the-assembly-1.0.0.zip"
    private static final String TAR_GZ_ASSEMBLY_PATH = "${DESCRIPTOR_TEST_RESOURCES_PATH}/the-assembly-1.0.0.tar.gz"
    private static final String EXPECTED_DESCRIPTOR_PATH = "${DESCRIPTOR_TEST_RESOURCES_PATH}/expected-assembly-descriptor.yaml"

    private static final File zipAssembly = new File(getClass().getResource(ZIP_ASSEMBLY_PATH).toURI())
    private static final File tarGzAssembly = new File(getClass().getResource(TAR_GZ_ASSEMBLY_PATH).toURI())
    private static final File expectedDescriptor = new File(getClass().getResource(EXPECTED_DESCRIPTOR_PATH).toURI())

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    AssemblyContentDescriptorGenerator generator

    @Before
    void setUp() throws Exception {
        generator = new AssemblyContentDescriptorGenerator(log: logger, workingDir: tempFolder.newFolder())
    }

    @Test
    void generateDescriptorForZipAssemblyTest() {
        File descriptor = generator.generateDescriptor(zipAssembly)
        assertThat(descriptor)
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedDescriptor)
    }

    @Test
    void generateDescriptorForTarGzAssemblyTest() {
        File descriptor = generator.generateDescriptor(tarGzAssembly)
        assertThat(descriptor)
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedDescriptor)
    }
}
