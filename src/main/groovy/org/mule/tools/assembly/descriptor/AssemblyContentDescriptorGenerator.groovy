package org.mule.tools.assembly.descriptor

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.maven.plugin.logging.Log

import static org.apache.commons.compress.compressors.CompressorStreamFactory.GZIP

class AssemblyContentDescriptorGenerator {

    private static final String TAR_GZ_EXTENSION = "tar.gz"
    private static final String ZIP_EXTENSION = "zip"

    Log log
    File assemblyFile
    File descriptorTempDir

    File generateDescriptor() {
        log.debug("Generating content descriptor for ${assemblyFile}")
        validateFiles()
        getAssemblyEntries()
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

    private void getAssemblyEntries() {
        // Here are the things that I need to be aware of while dealing with streams
        // Close everything
        // Consider buffering
        // There seems to be some autodetect capabilities
        // Important web pages:
        // 1. https://commons.apache.org/proper/commons-compress/examples.html
        // 2. http://groovy-lang.org/groovy-dev-kit.html#_working_with_io
        // 3. How to create the input buffered stream http://docs.groovy-lang.org/latest/html/groovy-jdk/java/io/File
        // .html#newInputStream()
        // 4. Collections: https://docs.groovy-lang.org/next/html/documentation/working-with-collections.html#Collections-Lists

        try (ArchiveInputStream stream = buildArchiveInputStream(assemblyFile)) {
            ArchiveEntry entry = null;
            while ((entry = stream.getNextEntry()) != null) {
                if (!stream.canReadEntryData(entry)) {
                    throw new IllegalStateException("Cant ready entry ${entry.name} from ${assemblyFile}")
                }
                println entry.name
            }
        }
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
