package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.logging.SystemStreamLog
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy

class AssemblyContentDescriptorGeneratorTest {

    static final Log logger = new SystemStreamLog()

    static final String ZIP_ASSEMBLY_PATH = '/the-assembly-1.0.0.zip'
    static final String TAR_GZ_ASSEMBLY_PATH = '/the-assembly-1.0.0.tar.gz'
    static final String TAR_BZ2_ASSEMBLY_PATH = '/the-assembly-1.0.0.tar.bz2'
    static final String EXPECTED_ZIP_DESCRIPTOR_PATH = "/zip-assembly-descriptor.yaml"
    static final String EXPECTED_TAR_GZ_DESCRIPTOR_PATH = "/tar-gz-assembly-descriptor.yaml"
    static final String ASSEMBLY_TEMP_DIR = "mule-assembly-descriptor-temp"

    static final File zipAssembly = new File(getClass().getResource(ZIP_ASSEMBLY_PATH).toURI())
    static final File tarGzAssembly = new File(getClass().getResource(TAR_GZ_ASSEMBLY_PATH).toURI())
    static final File tarBz2Assembly = new File(getClass().getResource(TAR_BZ2_ASSEMBLY_PATH).toURI())
    static final File expectedZipDescriptor = new File(getClass().getResource(EXPECTED_ZIP_DESCRIPTOR_PATH).toURI())
    static final File expectedTarGzDescriptor = new File(getClass().getResource(EXPECTED_TAR_GZ_DESCRIPTOR_PATH).toURI())

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    File descriptorTempDir

    AssemblyContentDescriptorGenerator generator

    @Before
    void setUp() throws Exception {
        descriptorTempDir = tempFolder.newFolder(ASSEMBLY_TEMP_DIR)
        generator = new AssemblyContentDescriptorGenerator(log: logger, workingDir: descriptorTempDir)
    }

    @Test
    void generateDescriptorForZipAssemblyTest() {
        File descriptor = generator.generateDescriptor(zipAssembly)
        assertThat(descriptor)
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedZipDescriptor)
    }

    @Test
    void generateDescriptorForTarGzAssemblyTest() {
        File descriptor = generator.generateDescriptor(tarGzAssembly)
        assertThat(descriptor)
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedTarGzDescriptor)
    }

    @Test
    void generateDescriptorWhenAssemblyFormatNotSupportedTest() {
        assertThatThrownBy(() -> generator.generateDescriptor(tarBz2Assembly))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assembly archive format not supported")
    }
}
