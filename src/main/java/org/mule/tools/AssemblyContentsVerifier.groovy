package org.mule.tools

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.project.MavenProject
import org.codehaus.gmaven.mojo.GroovyMojo
import java.util.regex.Pattern
import java.util.zip.ZipFile

/**
 * @goal verify
 */
class AssemblyContentsVerifier extends GroovyMojo
{
    /**
     * The library list to check against.
     *
     * @parameter default-value="assembly-whitelist.txt"
     */
    File whitelist

    /**
     * File name which contents will be verified.
     * @parameter default-value="${project.build.finalName}.zip"
     */
    String projectOutputFile

    /**
     * Version of the product to be verified. Used in path expressions.
     * @parameter default-value="${project.version}"
     */
    String productVersion

    /**
     * Maven 3 dropped non-unique snapshots support and always timestamps artifacts on deploy. When 'true',
     * such artifacts will be treated specially in the distribution. Disable the flag for Maven 2 based builds.
     * @parameter default-value=true
     */
    Boolean maven3StyleSnapshots

    /**
     * Project instance.
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    MavenProject project

    void execute() {
        // sanity check
        if (!whitelist.exists()) {
            throw new MojoExecutionException("Whitelist file $whitelist does not exist.")
        }

        // splash
        log.info "*" * 80
        log.info("Verifying contents of the assembly".center(80))
        log.info "*" * 80

        // confirm output file is available
        def outputFile = new File("$project.build.directory/$projectOutputFile")
        if (!outputFile.exists()) {
            throw new MojoExecutionException("Output file $outputFile does not exist.")
        }

        // temp directory to unpack to
        def root = new File("${project.build.directory}/mule-assembly-verifier-temp")

        // unpack archive
        ant.unzip(src: outputFile,
                  dest: root)

        def canonicalRootPath = root.canonicalPath.replaceAll("\\\\", "/")

        // list all files
        def files = []
        root.eachFileRecurse() { file ->
            def canonicalPath = file.canonicalPath.replaceAll("\\\\", "/") - canonicalRootPath
            files << canonicalPath
        }

        log.debug("Files in the assembly: " + files.join("\n\t"))

        def missing = findMissing(files)
        def unexpected = findUnexpected(files)
        def duplicates = findDuplicates(outputFile)

        if (missing || unexpected || duplicates) {
            def msg = new StringBuilder("The following problems have been encountered:\n\n")
            if (missing) {
                msg << "\tMissing from the Distribution:\n"
                missing.eachWithIndex { name, i ->
                    msg << "\t\t\t${(i + 1).toString().padLeft(3)}. ${name}\n"
                }
            }
            if (unexpected) {
                msg << "\tUnexpected entries in the Distribution:\n"
                unexpected.eachWithIndex { name, i ->
                    msg << "\t\t\t${(i + 1).toString().padLeft(3)}. ${name}\n"
                }
            }
            if (duplicates) {
                msg << "\tDuplicate entries in the Distribution:\n"
                duplicates.eachWithIndex { name, i ->
                    msg << "\t\t\t${(i + 1).toString().padLeft(3)}. ${name}\n"
                }
            }
            throw new MojoFailureException(msg as String)
        }
    }

    def findMissing(actualNames) {
        // load the whitelist
        def expected = []
        whitelist.eachLine() {
            // ignore comments and empty lines
            if (!it.startsWith('#') && it.trim().size() != 0) {
                // canonicalize and interpolate the entry
                def parsed = it.replaceAll("\\\\", "/")
                expected << parsed.replace('${productVersion}', productVersion)
            }
        }
        // strip "-SNAPSHOT" from the version if present
        def version = productVersion - "-SNAPSHOT"
        // locate maven3-style snapshot string with a unique timestamp
        def pattern = Pattern.compile("$version-\\d{8}.\\d{6}-\\d+")

        // find all whitelist entries which are missing

        // for maven3-style timestamped snapshots
        def processedActualNames = []
        if (maven3StyleSnapshots) {
            actualNames.each {
                // pre-process the actual filename to look for a match by replacing m3 snapshot timestamp
                // with just a "-SNAPSHOT" for comparison
                def matcher = pattern.matcher(it)
                if (matcher.find()) {
                    def processed = matcher.replaceFirst("$version-SNAPSHOT")
                    processedActualNames << processed
                } else {
                    processedActualNames << it
                }
            }
        }

        // find all libs not in the whitelist
        expected.findAll {
            if (!maven3StyleSnapshots) {
                return !actualNames.contains(it)
            } else {
                return !processedActualNames.contains(it)
            }
        }.sort { it.toLowerCase() } // sort case-insensitive
    }

    def findUnexpected(actualNames) {
        // load the whitelist
        def expected = []
        whitelist.eachLine() {
            // ignore comments and empty lines
            if (!it.startsWith('#') && it.trim().size() != 0) {
                // canonicalize and interpolate the entry
                expected << it.replaceAll("\\\\", "/").replace('${productVersion}', productVersion)
            }
        }
        // strip "-SNAPSHOT" from the version if present
        def version = productVersion - "-SNAPSHOT"
        // locate maven3-style snapshot string with a unique timestamp
        def pattern = Pattern.compile("$version-\\d{8}.\\d{6}-\\d+")

        // find all libs not in the whitelist
        actualNames.findAll {
            if (!maven3StyleSnapshots) {
                return !expected.contains(it)
            } else {
                // pre-process the actual filename to look for a match by replacing m3 snapshot timestamp
                // with just a "-SNAPSHOT" for comparison
                def matcher = pattern.matcher(it)
                if (matcher.find()) {
                    def processed = matcher.replaceFirst("$version-SNAPSHOT")
                    return !expected.contains(processed)
                }
                return false
            }
        }.sort { it.toLowerCase() } // sort case-insensitive
    }

    def findDuplicates(File zipFile) {
        // convert Enumeration -> List and extract zip entry names
        def entries = Collections.list(new ZipFile(zipFile).entries()).collect { it.name }

        println entries.join("\n")

        entries.findAll {
            entries.count(it) > 1
        }.unique()
    }
}
