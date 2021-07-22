/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor

class AssemblyDescriptorValidator {

    static void validateAssemblyFile(File assemblyFile) {
        if (!assemblyFile.exists()) {
            throw new FileNotFoundException(assemblyFile.getPath(), "Assembly file does not exist")
        }
    }

    static void validateDescriptorTempDir(File descriptorTempDir) {
        if (descriptorTempDir.exists() && !descriptorTempDir.isDirectory()) {
            throw new IllegalArgumentException("Given descriptor generation temp dir path ${descriptorTempDir} is not a " +
                    "directory")
        }

        if (!descriptorTempDir.exists() && !descriptorTempDir.mkdirs()) {
            throw new IllegalArgumentException("Cannot create descriptor generation temp dir at ${descriptorTempDir}")
        }
    }
}
