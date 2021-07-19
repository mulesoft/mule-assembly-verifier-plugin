package org.mule.tools.assembly.descriptor

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy

class AssemblyDescriptorValidatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Test
    void validateAssemblyFileTest() {
        File anAssembly = tempFolder.newFile()
        AssemblyDescriptorValidator.validateAssemblyFile(anAssembly)
    }

    @Test
    void validateAssemblyFileWhenAssemblyFileDoesNotExistTest() {
        assertThatThrownBy(() -> AssemblyDescriptorValidator.validateAssemblyFile(new File("an-assembly-that-does-not-exist" +
                ".zip")))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("an-assembly-that-does-not-exist.zip (Assembly file does not exist)")
    }

    @Test
    void validateDescriptorTempDirCreateDirIfDoesNotExistTest() {
        File tempDir = new File(tempFolder.newFolder(), "mule-assembly-descriptor-temp")
        AssemblyDescriptorValidator.validateDescriptorTempDir(tempDir)

        assertThat(tempDir).exists().isDirectory()
    }

    @Test
    void validateDescriptorTempDirWhenItIsNotADirTest() {
        File aFile = tempFolder.newFile()

        assertThatThrownBy(() -> AssemblyDescriptorValidator.validateDescriptorTempDir(aFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Given descriptor generation temp dir path ${aFile} is not a directory")
    }

    @Test
    void validateDescriptorTempDirWhenItCanNotBeCreatedTest() {
        File nonWritableDir = tempFolder.newFolder()
        nonWritableDir.setWritable(false)
        File fileThatWillNotBeCreated = new File(nonWritableDir, "tmp-dir")

        assertThatThrownBy(() -> AssemblyDescriptorValidator.validateDescriptorTempDir(fileThatWillNotBeCreated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can not create descriptor generation temp dir at ${fileThatWillNotBeCreated}")

        nonWritableDir.setWritable(true)
    }
}
