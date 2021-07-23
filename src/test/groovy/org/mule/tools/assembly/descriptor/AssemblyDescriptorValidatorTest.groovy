/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.mule.tools.assembly.descriptor.AssemblyDescriptorValidator.validateAssemblyFile
import static org.mule.tools.assembly.descriptor.AssemblyDescriptorValidator.validateDescriptorTempDir

class AssemblyDescriptorValidatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Test
    void validateAssemblyFileTest() {
        validateAssemblyFile(tempFolder.newFile("assembly.zip"))
        validateAssemblyFile(tempFolder.newFile("assembly.tar.gz"))
    }

    @Test
    void validateAssemblyFileWhenAssemblyFileDoesNotExistTest() {
        String assemblyName = "does-not-exist-assembly.zip"
        assertThatThrownBy(() -> validateAssemblyFile(new File(assemblyName)))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("${assemblyName} (Assembly file does not exist)")
    }

    @Test
    void validateAssemblyFileWhenFormatIsNotSupported() {
        File assembly = tempFolder.newFile("does-not-exist-assembly.tar.bz2")
        assertThatThrownBy(() -> validateAssemblyFile(assembly))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assembly archive format not supported")
    }

    @Test
    void validateDescriptorTempDirCreateDirIfDoesNotExistTest() {
        File aDirectoryThatDoesNotExistPriorValidation = new File(tempFolder.newFolder(), "mule-assembly-descriptor-temp")
        validateDescriptorTempDir(aDirectoryThatDoesNotExistPriorValidation)

        assertThat(aDirectoryThatDoesNotExistPriorValidation).exists().isDirectory()
    }

    @Test
    void validateDescriptorTempDirWhenItIsNotADirTest() {
        File aFile = tempFolder.newFile()

        assertThatThrownBy(() -> validateDescriptorTempDir(aFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Given descriptor generation temp dir path ${aFile} is not a directory")
    }

    @Test
    void validateDescriptorTempDirWhenItCanNotBeCreatedTest() {
        File nonWritableDir = tempFolder.newFolder()
        nonWritableDir.setWritable(false)
        File fileThatWillNotBeCreated = new File(nonWritableDir, "tmp-dir")

        assertThatThrownBy(() -> validateDescriptorTempDir(fileThatWillNotBeCreated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create descriptor generation temp dir at ${fileThatWillNotBeCreated}")

        nonWritableDir.setWritable(true)
    }
}
