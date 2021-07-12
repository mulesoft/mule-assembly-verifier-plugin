package org.mule.tools.assembly.descriptor


import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.logging.SystemStreamLog
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThatThrownBy

class AssemblyContentDescriptorGeneratorTest {

    static final Log logger = new SystemStreamLog()

    static final String ASSEMBLY_PATH = '/the-assembly-1.0.0.zip'
    static final String ASSEMBLY_TEMP_DIR = "mule-assembly-descriptor-temp"
    static final File assembly = new File(getClass().getResource(ASSEMBLY_PATH).toURI())

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    AssemblyContentDescriptorGenerator assemblyContentDescriptorGenerator

//    @Before
//    void setUp() throws Exception {
//        File descriptorTempDir = new File(folder.newFolder(), ASSEMBLY_TEMP_DIR)
//        File assembly = new File(getClass().getResource(ASSEMBLY_PATH).toURI())
//    }

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
                assemblyFile: assembly,
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
                assemblyFile: assembly,
                descriptorTempDir: fileThatWillNotBeCreated)

        assertThatThrownBy(() -> assemblyContentDescriptorGenerator.generateDescriptor())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can not create descriptor generation temp dir at ${fileThatWillNotBeCreated}")

        nonWritableDir.setWritable(true)
    }
}
