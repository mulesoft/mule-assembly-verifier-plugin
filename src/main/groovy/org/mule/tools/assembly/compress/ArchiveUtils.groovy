/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.compress

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.utils.IOUtils

import java.nio.file.Files
import java.nio.file.Paths

class ArchiveUtils {

    static void extractZip(File zipFile, File targetDir) {
        try (ArchiveInputStream archiveInputStream = new ZipArchiveInputStream(zipFile.newInputStream())) {
            ArchiveEntry entry
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                File f = Paths.get(targetDir.getPath(), entry.getName()).toFile()
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        // @todo: test this scenario
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        // @todo: test this scenario
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(archiveInputStream, o);
                    }
                }
            }
        }
    }
}
