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
import org.apache.maven.plugin.logging.Log
import org.mule.tools.assembly.compress.ArchiveUtils

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.KEBAB_CASE
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import static org.mule.tools.assembly.compress.ArchiveUtils.validateAssemblyFormat

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

        validateAssemblyFormat(assemblyFile)
        List entries = getAssemblyEntries(assemblyFile)
        File yamDescriptor = createYamlDescriptor(entries, workingDir)

        log.debug("Descriptor of assembly content has been created at: ${yamDescriptor}")
        return yamDescriptor
    }

    private List getAssemblyEntries(File assemblyFile) {
        List entries
        if (assemblyFile.name.endsWith(TAR_GZ_EXTENSION)) {
            entries = ArchiveUtils.listTarGzEntries(assemblyFile)
        } else {
            entries = ArchiveUtils.listZipEntries(assemblyFile)
        }
        return entries.collect {
            new AssemblyEntry(name: it.name, size: it.size, lastModifiedDate: it.lastModifiedDate)
        }
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
