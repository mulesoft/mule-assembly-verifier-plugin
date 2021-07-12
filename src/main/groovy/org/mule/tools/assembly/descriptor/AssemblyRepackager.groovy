package org.mule.tools.assembly.descriptor

import com.google.common.io.Files
import org.apache.maven.plugin.logging.Log

import java.nio.file.Path

import static org.mule.tools.assembly.compress.ArchiveUtils.createTarGz
import static org.mule.tools.assembly.compress.ArchiveUtils.createZip
import static org.mule.tools.assembly.compress.ArchiveUtils.extractTarGz
import static org.mule.tools.assembly.compress.ArchiveUtils.extractZip
import static org.mule.tools.assembly.compress.ArchiveUtils.validateAssemblyFormat

class AssemblyRepackager {

    private static final String TAR_GZ_EXTENSION = "tar.gz"

    Log log
    File workingDir

    void repackageWithDescriptor(File assemblyFile, File descriptorArchive, Path targetDescriptorDirWithinAssembly) {
        log.debug("Adding ${descriptorArchive} to assembly ${assemblyFile} in loc ${targetDescriptorDirWithinAssembly}")

        validateAssemblyFormat(assemblyFile)
        File extractedAssemblyRootDir = extractAssembly(assemblyFile)
        backupOriginalAssembly(assemblyFile)
        copyDescriptorToExpectedLocation(extractedAssemblyRootDir, descriptorArchive, targetDescriptorDirWithinAssembly)
        repackageAssembly(assemblyFile, extractedAssemblyRootDir)

        // @todo[question]: is it ok to be an info message?
        log.info("The assembly file ${assemblyFile} has been repackaged to add its descriptor ${descriptorArchive}")
    }

    private File extractAssembly(File assemblyFile) {
        File extractLocation = setupExtractDir(workingDir)
        if (assemblyFile.name.endsWith(TAR_GZ_EXTENSION)) {
            extractTarGz(assemblyFile, extractLocation)
        } else {
            extractZip(assemblyFile, extractLocation)
        }
        return new File(extractLocation, extractLocation.list()[0])
    }

    private File setupExtractDir(File workingDir) {
        File extractDir = new File(workingDir, "extracted-assembly")
        if (!extractDir.mkdirs()) {
            throw new IOException("Couldn't create assembly extract dir ${extractDir}")
        }
        return extractDir
    }

    private void backupOriginalAssembly(File assemblyFile) {
        // @todo[question]: What about the idea of keeping a copy of the original assembly?
        File originalAssemblyNewName = new File(assemblyFile.getParentFile(), "${assemblyFile.name}-original")
        assemblyFile.renameTo(originalAssemblyNewName)
        log.info("Original assembly could be found at: ${originalAssemblyNewName}")
    }

    private void copyDescriptorToExpectedLocation(File extractedAssemblyRootDir,
                                                  File descriptorArchive,
                                                  Path targetDescriptorDirWithinAssembly) {
        File targetDir = new File(extractedAssemblyRootDir, targetDescriptorDirWithinAssembly.toString())

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Can't create dir ${targetDir} to place the descriptor file")
        }

        File descriptorArchiveNewHome = new File(targetDir, descriptorArchive.name)
        Files.copy(descriptorArchive, descriptorArchiveNewHome)
    }

    private repackageAssembly(File assemblyFile, File assemblyRootDir) {
        File packagingDir = assemblyRootDir.parentFile
        if (assemblyFile.name.endsWith(TAR_GZ_EXTENSION)) {
            createTarGz(assemblyFile, packagingDir)
        } else {
            createZip(assemblyFile, packagingDir)
        }
    }
}
