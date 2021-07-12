package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.logging.Log

class AssemblyContentDescriptorGenerator {

    Log log
    File assemblyFile
    File descriptorTempDir

    File generateDescriptor() {
        // 2. Walk the file
        // 3. Generate the descriptor file
        // 0. Log
        log.debug("Generating content descriptor for ${assemblyFile}")
        validateFiles()

        return null
    }

    void validateFiles() {
        if (!assemblyFile.exists()){
            throw new FileNotFoundException(assemblyFile.getPath(), "Assembly file does not exist")
        }

        if(descriptorTempDir.exists() && !descriptorTempDir.isDirectory()){
            throw new IllegalArgumentException("Given descriptor generation temp dir path ${descriptorTempDir} is not a directory")
        }

        if(!descriptorTempDir.exists() && !descriptorTempDir.mkdirs()){
            throw new IllegalArgumentException("Can not create descriptor generation temp dir at ${descriptorTempDir}")
        }
    }
}
