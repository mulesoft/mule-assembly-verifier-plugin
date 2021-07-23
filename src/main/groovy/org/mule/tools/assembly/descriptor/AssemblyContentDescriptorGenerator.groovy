/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.maven.plugin.logging.Log

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.KEBAB_CASE
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER

/**
 * Class for generating the yaml descriptor file of all the entries inside an assembly.
 */
class AssemblyContentDescriptorGenerator {

    private static final String TAR_GZ_EXTENSION = "tar.gz"
    private static final String YAML_DESCRIPTOR_FILE_NAME = "assembly-descriptor.yaml"

    Log log
    File workingDir

    File generateDescriptor(File assemblyFile) {
        log.debug("Generating content descriptor for ${assemblyFile}")

        List entries = getAssemblyEntries(assemblyFile)
        File yamDescriptor = createYamlDescriptor(entries, workingDir)

        log.debug("Descriptor of assembly content has been created at: ${yamDescriptor}")
        return yamDescriptor
    }

    private List getAssemblyEntries(File assemblyFile) {
        if (assemblyFile.name.endsWith(TAR_GZ_EXTENSION)) {
            return listTarGzEntries(assemblyFile)
        } else {
            return listZipEntries(assemblyFile)
        }
    }

    private List listZipEntries(File zipFile) {
        List entries
        try (ArchiveInputStream stream = new ZipArchiveInputStream(zipFile.newInputStream())) {
            entries = listArchiveEntries(stream, zipFile)
        }
        return entries
    }

    private List listTarGzEntries(File tarGzFile) {
        List entries
        try (ArchiveInputStream stream = new TarArchiveInputStream(new GzipCompressorInputStream(tarGzFile.newInputStream()))) {
            entries = listArchiveEntries(stream, tarGzFile)
        }
        return entries
    }

    private List listArchiveEntries(ArchiveInputStream inputStream, File archive) {
        List entries = []
        ArchiveEntry entry
        while ((entry = inputStream.getNextEntry()) != null) {
            if (!inputStream.canReadEntryData(entry)) {
                throw new IllegalStateException("Cannot ready entry ${entry.name} from ${archive}")
            }
            if (!entry.directory) {
                String sha256 = DigestUtils.sha256Hex(IOUtils.toByteArray(inputStream))
                entries << new AssemblyEntry(name: entry.name, sizeInBytes: entry.size, sha256: sha256)
            }
        }
        return entries
    }

    private File createYamlDescriptor(List assemblyEntries, File workingDir) {
        File descriptorFile = new File(workingDir, YAML_DESCRIPTOR_FILE_NAME)
        buildYamlObjectMapper().writeValue(descriptorFile, assemblyEntries)
        return descriptorFile
    }

    private ObjectMapper buildYamlObjectMapper() {
        return new ObjectMapper(new YAMLFactory().disable(WRITE_DOC_START_MARKER))
                .setPropertyNamingStrategy(KEBAB_CASE)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(new JavaTimeModule())
    }

}
