package org.mule.tools.assembly.descriptor


import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.logging.SystemStreamLog
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThatThrownBy

class AssemblyContentDescriptorGeneratorTest {

    static final Log logger = new SystemStreamLog()

    static final String ZIP_ASSEMBLY_PATH = '/the-assembly-1.0.0.zip'
    static final String TAR_GZ_ASSEMBLY_PATH = '/the-assembly-1.0.0.tar.gz'
    static final String TAR_BZ2_ASSEMBLY_PATH = '/the-assembly-1.0.0.tar.bz2'
    static final String ASSEMBLY_TEMP_DIR = "mule-assembly-descriptor-temp"

    static final File zipAssembly = new File(getClass().getResource(ZIP_ASSEMBLY_PATH).toURI())
    static final File tarGzAssembly = new File(getClass().getResource(TAR_GZ_ASSEMBLY_PATH).toURI())
    static final File tarBz2Assembly = new File(getClass().getResource(TAR_BZ2_ASSEMBLY_PATH).toURI())

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    File descriptorTempDir

    AssemblyContentDescriptorGenerator assemblyContentDescriptorGenerator

    @Before
    void setUp() throws Exception {
        descriptorTempDir = new File(tempFolder.newFolder(), ASSEMBLY_TEMP_DIR)
    }

    @Test
    void generateDescriptorForZipAssemblyTest() {
        assemblyContentDescriptorGenerator = new AssemblyContentDescriptorGenerator(
                log: logger,
                assemblyFile: zipAssembly,
                descriptorTempDir: descriptorTempDir)
        assemblyContentDescriptorGenerator.generateDescriptor()

        // @todo: assertions
    }

    @Test
    void generateDescriptorForTarGzAssemblyTest() {
        assemblyContentDescriptorGenerator = new AssemblyContentDescriptorGenerator(
                log: logger,
                assemblyFile: tarGzAssembly,
                descriptorTempDir: descriptorTempDir)
        assemblyContentDescriptorGenerator.generateDescriptor()

        // @todo: assertions
    }

    @Test
    void generateDescriptorWhenAssemblyFormatNotSupportedTest() {
        assemblyContentDescriptorGenerator = new AssemblyContentDescriptorGenerator(
                log: logger,
                assemblyFile: tarBz2Assembly,
                descriptorTempDir: descriptorTempDir)

        assertThatThrownBy(() -> assemblyContentDescriptorGenerator.generateDescriptor())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assembly archive format not supported")
    }

    @Test
    void generateDescriptorWhenAssemblyFileDoesNotExistTest() {
        assemblyContentDescriptorGenerator = new AssemblyContentDescriptorGenerator(
                log: logger,
                assemblyFile: new File("an-assembly-that-does-not-exist.zip"),
                descriptorTempDir: new File("tmp-that-does-not-exist"))

        assertThatThrownBy(() -> assemblyContentDescriptorGenerator.generateDescriptor())
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("an-assembly-that-does-not-exist.zip (Assembly file does not exist)")
    }

    @Test
    void generateDescriptorWhenTempDirIsNotADirTest() {
        File aFile = tempFolder.newFile()
        assemblyContentDescriptorGenerator = new AssemblyContentDescriptorGenerator(
                log: logger,
                assemblyFile: zipAssembly,
                descriptorTempDir: aFile)

        assertThatThrownBy(() -> assemblyContentDescriptorGenerator.generateDescriptor())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Given descriptor generation temp dir path ${aFile} is not a directory")
    }

    @Test
    void generateDescriptorWhenTempDirCanNotBeCreatedTest() {
        File nonWritableDir = tempFolder.newFolder()
        nonWritableDir.setWritable(false)
        File fileThatWillNotBeCreated = new File(nonWritableDir, "tmp-dir")

        assemblyContentDescriptorGenerator = new AssemblyContentDescriptorGenerator(
                log: logger,
                assemblyFile: zipAssembly,
                descriptorTempDir: fileThatWillNotBeCreated)

        assertThatThrownBy(() -> assemblyContentDescriptorGenerator.generateDescriptor())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can not create descriptor generation temp dir at ${fileThatWillNotBeCreated}")

        nonWritableDir.setWritable(true)
    }
}
