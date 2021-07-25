/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.maven.project.MavenProjectHelper

/**
 * Mojo for generating the descriptor of the assembly.
 *
 * The descriptor is a `jar` archive containing  a `YAML` file that describes Mule distribution assembly entries
 */
// @todo[question]: What about this name for the Mojo's goal?
// @todo: If anything change here, then, don't forget to update the `README.md`
@Mojo(name = "generate-descriptor")
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
     * Descriptor jar file name
     */
    @Parameter(defaultValue = 'mule-assembly-descriptor-${project.version}.jar')
    String descriptorJarName

    /**
     * Attach the descriptor jar to the Maven project
     */
    @Parameter(defaultValue = 'true')
    Boolean attachDescriptor

    /**
     * Classifier for attaching descriptor to the project
     */
    // @todo[question]: What about this classifier?
    @Parameter(defaultValue = 'assembly-descriptor')
    String descriptorClassifier

    /**
     * Skip execution of this mojo.
     */
    @Parameter(defaultValue = 'false')
    Boolean skip

    /**
     * Project instance.
     */
    @Parameter(defaultValue = '${project}', required = true, readonly = true)
    MavenProject project

    @Component
    MavenProjectHelper projectHelper;

    void execute() {

        if (skip) {
            log.info("Skipping descriptor generation.")
            return
        }

        printSplash()

        try {
            AssemblyDescriptorValidator.validateAssemblyFile(assemblyFile)
            AssemblyDescriptorValidator.validateDescriptorTempDir(descriptorTempDir)

            File contentDescriptor = new AssemblyContentDescriptorGenerator(log: log, workingDir: descriptorTempDir)
                    .generateDescriptor(assemblyFile)

            File descriptorArchive = new AssemblyDescriptorArchiveBuilder(log: log, workingDir: descriptorTempDir)
                    .buildDescriptorArchive(contentDescriptor, descriptorJarName)

            if (attachDescriptor) {
                projectHelper.attachArtifact(project, "jar", descriptorClassifier, descriptorArchive)
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e)
        }
    }

    private void printSplash() {
        log.info "*" * 80
        log.info("Generating descriptor for the assembly".center(80))
        log.info "*" * 80
    }
}
