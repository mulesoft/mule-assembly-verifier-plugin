//@todo: Should we add any copyright headers
package org.mule.tools

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

/**
 * //@todo: Improve all javadocs
 * Mojo for generating and attaching a descriptor to the assembly
 */
@Mojo(name = "descriptor")
class AssemblyDescriptorGeneratorMojo extends AbstractMojo {

    /**
     * File name whose descriptor will be generated.
     */
    @Parameter(defaultValue = '${project.build.finalName}.zip')
    String projectOutputFile

    /**
     * Version of the product.
     */
    @Parameter(defaultValue = '${project.version}')
    String productVersion

    /**
     * Skip execution of this plugin, allowing to control the behaviour using profiles.
     */
    @Parameter(defaultValue = 'false')
    Boolean skip

    /**
     * Project instance.
     */
    @Parameter(defaultValue = '${project}', required = true, readonly = true)
    MavenProject project

    def outputFile
    void execute() {

        // Potentially skip execution
        if (skip) {
            log.info("Skipping descriptor generation.")
            return
        }

        // splash
        log.info "*" * 80
        log.info("Generating descriptor for the assembly".center(80))
        log.info "*" * 80

        // confirm output file is available
        outputFile = new File("$project.build.directory/$projectOutputFile")
        if (!outputFile.exists()) {
            throw new MojoExecutionException("Output file $outputFile does not exist.")
        }


    }

}
