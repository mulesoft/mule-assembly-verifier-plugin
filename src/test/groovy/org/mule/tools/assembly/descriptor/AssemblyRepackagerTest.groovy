package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.logging.SystemStreamLog
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.nio.file.Paths

import static org.assertj.core.api.Assertions.assertThat
import static org.mule.tools.assembly.compress.ArchiveUtils.listTarGzEntries
import static org.mule.tools.assembly.compress.ArchiveUtils.listZipEntries

class AssemblyRepackagerTest {

    static final Log logger = new SystemStreamLog()

    static final String ASSEMBLY_TEMP_DIR = "mule-assembly-descriptor-temp"

    static final File zipAssembly = new File(getClass().getResource("/the-assembly-to-be-repackaged-1.0.0.zip").toURI())
    static final File tarGzAssembly = new File(getClass().getResource("/the-assembly-to-be-repackaged-1.0.0.tar.gz").toURI())

    static final File assemblyDescriptorJar =
            new File(getClass().getResource("/mule-assembly-descriptor-1.0.0-SNAPSHOT.jar").toURI())

    File descriptorTempDir
    AssemblyRepackager repackager;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Before
    void setUp() throws Exception {
        descriptorTempDir = tempFolder.newFolder(ASSEMBLY_TEMP_DIR)
        repackager = new AssemblyRepackager(log: logger, workingDir: descriptorTempDir)
    }

    @Test
    void repackageZipAssemblyWithDescriptor() {
        List expectedEntries = listZipEntries(zipAssembly).collect { it.name }
        expectedEntries << "the-assembly-1.0.0/lib/opt/mule-assembly-descriptor-1.0.0-SNAPSHOT.jar"

        repackager.repackageWithDescriptor(zipAssembly, assemblyDescriptorJar, Paths.get("lib", "opt"));

        List repackagedEntries = listZipEntries(zipAssembly).collect { it.name }
        assertThat(repackagedEntries).containsOnly(expectedEntries.toArray());
    }

    @Test
    void repackageTarGzAssemblyWithDescriptor() {
        List expectedEntries = listTarGzEntries(tarGzAssembly).collect { it.name }
        expectedEntries << "the-assembly-1.0.0/lib/opt/mule-assembly-descriptor-1.0.0-SNAPSHOT.jar"

        repackager.repackageWithDescriptor(tarGzAssembly, assemblyDescriptorJar, Paths.get("lib", "opt"));

        List repackagedEntries = listTarGzEntries(tarGzAssembly).collect { it.name }
        assertThat(repackagedEntries).containsOnly(expectedEntries.toArray());
    }
}
