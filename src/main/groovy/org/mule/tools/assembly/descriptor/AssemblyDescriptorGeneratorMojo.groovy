/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
// @todo: Skip HelpMojo sources from coverage report

package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

import java.nio.file.Paths
import java.text.SimpleDateFormat

/**
 * Mojo for generating the descriptor of the assembly.
 *
 * The descriptor is a `jar` archive added as an additional entry to the assembly archive, containing  a `YAML` file that
 * describes Mule distribution assembly entries
 */
// @todo[question]: Any better name for the goal?
@Mojo(name = "attach-descriptor")
class AssemblyDescriptorGeneratorMojo extends AbstractMojo {

    /**
     * Assembly file whose descriptor will be generated.
     */
    @Parameter(defaultValue = '${project.build.directory}/${project.build.finalName}.zip')
    File assemblyFile

    /**
     * Temporary director for the work carried out by this mojo
     */
    @Parameter(defaultValue = '${project.build.directory}/mule-assembly-descriptor-temp')
    File descriptorTempDir

    /**
     * Version of the product used for the descriptor zip file name
     */
    @Parameter(defaultValue = 'mule-assembly-descriptor-${project.version}.jar')
    String descriptorJarName

    /**
     * Relative path of the location where the descriptor jar should be placed within the assembly's root directory
     */
    @Parameter(defaultValue = 'lib/mule')
    String targetDescriptorDirWithinAssembly

    /**
     * Skip execution of this mojo.
     */
    @Parameter(defaultValue = 'false')
    Boolean skip

    void execute() {

        if (skip) {
            log.info("Skipping descriptor generation.")
            return
        }

        printSplash()

        try {
            AssemblyDescriptorValidator.validateAssemblyFile(assemblyFile)
            AssemblyDescriptorValidator.validateDescriptorTempDir(descriptorTempDir)
            descriptorTempDir = setupWorkingDirForExecution()

            File contentDescriptor = new AssemblyContentDescriptorGenerator(log: log, workingDir: descriptorTempDir)
                    .generateDescriptor(assemblyFile)

            // @todo[question]: Would clients "scanners" be scared by such a new particular jar? Wouldn't be less scared if
            //  seeing a plain file?
            File descriptorArchive = new AssemblyDescriptorArchiveBuilder(log: log, workingDir: descriptorTempDir)
                    .buildDescriptorArchive(contentDescriptor, descriptorJarName)

            // @todo[question]: How inconvenient is the idea of repackaging the distro? Let's say from the Maven's convention
            //  perspective, How many conventions might we braking with this?
            // @todo: find out if the path thing works in Windows
            new AssemblyRepackager(log: log, workingDir: descriptorTempDir)
                    .repackageWithDescriptor(assemblyFile, descriptorArchive, Paths.get(targetDescriptorDirWithinAssembly))

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e)
        }
    }

    private void printSplash() {
        log.info "*" * 80
        log.info("Generating descriptor for the assembly".center(80))
        log.info "*" * 80
    }

    private File setupWorkingDirForExecution() {
        // @todo[question]: Should this explanation be left?
        // Given that more than one assembly could be processed in the same build lifecycle, this hack intends to avoid collisions
        String datetime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())
        File workingDir = new File(descriptorTempDir, datetime)
        if (!workingDir.mkdirs()) {
            throw new IOException("Could not create working dir for Mojo execution: ${workingDir}")
        }
        return workingDir
    }
}
