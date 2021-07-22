/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.compress

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils

import java.nio.file.Files
import java.nio.file.Paths

class ArchiveUtils {

    private static final String TAR_GZ_EXTENSION = "tar.gz"
    private static final String ZIP_EXTENSION = "zip"

    static void validateAssemblyFormat(File assembly) {
        String assemblyName = assembly.name
        if (!(assemblyName.endsWith(TAR_GZ_EXTENSION) || assemblyName.endsWith(ZIP_EXTENSION))) {
            throw new IllegalArgumentException("Assembly archive format not supported")
        }
    }

    static void extractZip(File zipFile, File targetDir) {
        try (ArchiveInputStream archiveInputStream = new ZipArchiveInputStream(zipFile.newInputStream())) {
            extractArchive(archiveInputStream, targetDir)
        }
    }

    private static void extractArchive(ArchiveInputStream zipArchiveInputStream, File targetDir) {
        ArchiveEntry entry
        while ((entry = zipArchiveInputStream.getNextEntry()) != null) {
            File f = Paths.get(targetDir.getPath(), entry.getName()).toFile()
            if (entry.isDirectory()) {
                if (!f.isDirectory() && !f.mkdirs()) {
                    throw new IOException("failed to create directory " + f);
                }
            } else {
                File parent = f.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("failed to create directory " + parent);
                }
                try (OutputStream o = Files.newOutputStream(f.toPath())) {
                    IOUtils.copy(zipArchiveInputStream, o);
                }
            }
        }
    }

    static List listZipEntries(File zipFile) {
        List entries
        try (ArchiveInputStream stream = new ZipArchiveInputStream(zipFile.newInputStream())) {
            entries = listArchiveEntries(stream, zipFile)
        }
        return entries
    }

    static List listTarGzEntries(File tarGzFile) {
        List entries
        try (ArchiveInputStream stream = new TarArchiveInputStream(new GzipCompressorInputStream(tarGzFile.newInputStream()))) {
            entries = listArchiveEntries(stream, tarGzFile)
        }
        return entries
    }

    private static List listArchiveEntries(ArchiveInputStream stream, File archive) {
        List entries = []
        ArchiveEntry entry
        while ((entry = stream.getNextEntry()) != null) {
            if (!stream.canReadEntryData(entry)) {
                throw new IllegalStateException("Cannot ready entry ${entry.name} from ${archive}")
            }
            if (!entry.directory) {
                entries << entry
            }
        }
        return entries
    }
}
