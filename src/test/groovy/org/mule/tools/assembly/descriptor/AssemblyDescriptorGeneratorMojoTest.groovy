package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.testing.MojoRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThat
import static org.mule.tools.assembly.compress.ArchiveUtils.extractZip
import static org.mule.tools.assembly.compress.ArchiveUtils.listZipEntries

class AssemblyDescriptorGeneratorMojoTest {

    private static final String PROJECT_BASE_DIR = "/descriptor-mojo-test-project"
    private static final String ASSEMBLY_NAME = "descriptor-mojo-test-project-1.0.0.zip"
    private static final String ASSEMBLY_PATH = "${PROJECT_BASE_DIR}/target/${ASSEMBLY_NAME}"
    private static final String ASSEMBLY_PATH_BACKUP_COPY = "${ASSEMBLY_PATH}-original"
    private static final String EXPECTED_ZIP_DESCRIPTOR_PATH = "/zip-assembly-descriptor.yaml"

    private static final File projectBaseDir = new File(getClass().getResource(PROJECT_BASE_DIR).toURI())
    private static final File expectedZipDescriptor = new File(getClass().getResource(EXPECTED_ZIP_DESCRIPTOR_PATH).toURI())

    @Rule
    public MojoRule rule = new MojoRule()

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Test
    void verifyExecutionTest() throws Exception {
        File zipAssemblyToBeRepackaged = new File(getClass().getResource(ASSEMBLY_PATH).toURI())

        File copyOfOriginalAssembly = backupOriginalAssembly(zipAssemblyToBeRepackaged)
        List expectedEntries = getRepackagedAssemblyEntriesExpected(zipAssemblyToBeRepackaged)

        Mojo verifierMojo = rule.lookupConfiguredMojo(projectBaseDir, "attach-descriptor")

        assertThat(verifierMojo).isNotNull()
        verifierMojo.execute()

        assertThatBackupCopyWasCreated(copyOfOriginalAssembly)
        assertRepackagedAssemblyHasExpectedEntries(zipAssemblyToBeRepackaged, expectedEntries)
        assertDescriptorContent(zipAssemblyToBeRepackaged)
    }

    private File backupOriginalAssembly(File zipAssemblyToBeRepackaged) {
        File originalAssembly = tempFolder.newFile(ASSEMBLY_NAME)
        originalAssembly << zipAssemblyToBeRepackaged.bytes
        return originalAssembly
    }

    private List getRepackagedAssemblyEntriesExpected(File zipAssemblyToBeRepackaged) {
        List expectedEntries = listZipEntries(zipAssemblyToBeRepackaged).collect { it.name }
        expectedEntries << "the-assembly-1.0.0/lib/mule/mule-assembly-descriptor-1.0.0.jar"
        return expectedEntries
    }

    private void assertThatBackupCopyWasCreated(File copyOfOriginalAssembly) {
        File backupCopyOfOriginalAssemblyMadeByMojo = new File(getClass().getResource(ASSEMBLY_PATH_BACKUP_COPY).toURI())
        assertThat(backupCopyOfOriginalAssemblyMadeByMojo).exists().isFile().hasSameBinaryContentAs(copyOfOriginalAssembly)
    }

    private void assertRepackagedAssemblyHasExpectedEntries(File zipAssemblyToBeRepackaged, List<Object> expectedEntries) {
        List repackagedEntries = listZipEntries(zipAssemblyToBeRepackaged).collect { it.name }
        assertThat(repackagedEntries).containsOnly(expectedEntries.toArray());
    }

    private void assertDescriptorContent(File zipAssemblyToBeRepackaged) {
        File extractedRepackagedAssembly = tempFolder.newFolder("extracted-repackaged-assembly")
        extractZip(zipAssemblyToBeRepackaged, extractedRepackagedAssembly)

        // @todo: Improve this path handling
        File descriptorJar = new File(extractedRepackagedAssembly, "the-assembly-1.0.0/lib/mule/mule-assembly-descriptor-1.0.0" +
                ".jar")
        File extractedDescriptorJar = tempFolder.newFolder("extracted-descriptor-jar")
        extractZip(descriptorJar, extractedDescriptorJar)
        assertThat(new File(extractedDescriptorJar, "assembly-descriptor.yaml"))
                .exists()
                .isFile()
                .hasSameTextualContentAs(expectedZipDescriptor)
    }
}
