package org.mule.tools.assembly.descriptor

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.maven.plugin.logging.Log

class AssemblyDescriptorArchiveBuilder {

    Log log
    File workingDir

    File buildDescriptorArchive(File assemblyDescriptor, String descriptorJarName) {
        log.debug("Generating the assembly descriptor archive")

        File descriptorArchive = new File(workingDir, descriptorJarName)

        try (JarArchiveOutputStream jarOutStream = new JarArchiveOutputStream(descriptorArchive.newOutputStream())) {
            ArchiveEntry entry = jarOutStream.createArchiveEntry(assemblyDescriptor, assemblyDescriptor.name);
            jarOutStream.putArchiveEntry(entry);
            try (InputStream assemblyDescriptorInputStream = assemblyDescriptor.newInputStream()) {
                IOUtils.copy(assemblyDescriptorInputStream, jarOutStream);
            }
            jarOutStream.closeArchiveEntry();
            jarOutStream.finish()
        }

        log.debug("Descriptor archive was generated at ${descriptorArchive}")
        return descriptorArchive;
    }
}
