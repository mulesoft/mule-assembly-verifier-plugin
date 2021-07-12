package org.mule.tools.assembly.descriptor

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.maven.plugin.logging.Log

class AssemblyContentDescriptorGenerator {

    private static final String TAR_GZ_EXTENSION = "tar.gz"
    private static final String ZIP_EXTENSION = "zip"

    Log log
    File assemblyFile
    File descriptorTempDir

    File generateDescriptor() {
        log.debug("Generating content descriptor for ${assemblyFile}")
        validateFiles()
        List entries = getAssemblyEntries()
        entries.each { println it }
        // place entries in yaml file
        // log where the file was generated
        // return the file path of the descriptor
        return null
    }

    private void validateFiles() {
        if (!assemblyFile.exists()) {
            throw new FileNotFoundException(assemblyFile.getPath(), "Assembly file does not exist")
        }

        if (descriptorTempDir.exists() && !descriptorTempDir.isDirectory()) {
            throw new IllegalArgumentException("Given descriptor generation temp dir path ${descriptorTempDir} is not a " +
                    "directory")
        }

        if (!descriptorTempDir.exists() && !descriptorTempDir.mkdirs()) {
            throw new IllegalArgumentException("Can not create descriptor generation temp dir at ${descriptorTempDir}")
        }
    }

    private List getAssemblyEntries() {
        List entries = []
        try (ArchiveInputStream stream = buildArchiveInputStream(assemblyFile)) {
            ArchiveEntry entry = null;
            while ((entry = stream.getNextEntry()) != null) {
                if (!stream.canReadEntryData(entry)) {
                    throw new IllegalStateException("Cant ready entry ${entry.name} from ${assemblyFile}")
                }
                entries << new AssemblyEntry(name: entry.name,
                        size: entry.size,
                        isDirectory: entry.directory,
                        lastModifiedDate: entry.lastModifiedDate)
            }
        }
        return entries
    }

    private ArchiveInputStream buildArchiveInputStream(File assemblyFile) {
        switch (assemblyFile.name) {

            case { it.endsWith(TAR_GZ_EXTENSION) }:
                log.debug("Creating Archive Input Stream for a ${TAR_GZ_EXTENSION} assembly")
                return new TarArchiveInputStream(new GzipCompressorInputStream(assemblyFile.newInputStream()))

            case { it.endsWith(ZIP_EXTENSION) }:
                log.debug("Creating Archive Input Stream for a ${ZIP_EXTENSION} assembly")
                return new ZipArchiveInputStream(assemblyFile.newInputStream())

            default:
                throw new IllegalArgumentException("Assembly archive format not supported")
        }
    }

}
