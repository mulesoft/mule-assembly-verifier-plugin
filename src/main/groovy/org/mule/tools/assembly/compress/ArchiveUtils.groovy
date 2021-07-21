package org.mule.tools.assembly.compress

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
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

    static void extractTarGz(File zipFile, File targetDir) {
        try (ArchiveInputStream archiveInputStream =
                new TarArchiveInputStream(new GzipCompressorInputStream(zipFile.newInputStream()))) {
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

    static void createZip(File zipFile, File dirToCompress) {
        try (ArchiveOutputStream archiveOutputStream = new ZipArchiveOutputStream(zipFile.newOutputStream())) {
            createArchive(dirToCompress, archiveOutputStream, true)
        }
    }

    static void createTarGz(File tarGz, File dirToCompress) {
        try (ArchiveOutputStream archiveOutputStream =
                new TarArchiveOutputStream(new GzipCompressorOutputStream(tarGz.newOutputStream()))) {
            createArchive(dirToCompress, archiveOutputStream, false)
        }
    }

    private static void createArchive(File dirToCompress,
                                      ArchiveOutputStream archiveOutputStream,
                                      boolean formatSupportsDirEntries) {
        dirToCompress.eachFileRecurse { file ->
            if (file.isFile() || (file.isDirectory() && formatSupportsDirEntries)) {
                String entryName = dirToCompress.toPath().relativize(file.toPath()).toString()
                ArchiveEntry entry = archiveOutputStream.createArchiveEntry(file, entryName);
                archiveOutputStream.putArchiveEntry(entry);
                if (file.isFile()) {
                    try (InputStream i = file.newInputStream()) {
                        IOUtils.copy(i, archiveOutputStream);
                    }
                }
                archiveOutputStream.closeArchiveEntry();
            }
        }
        archiveOutputStream.finish();
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
