//@todo: Should we add any copyright headers

package org.mule.tools.assembly.descriptor

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.nio.file.Paths

/**
 * // @todo: Add a brief explanation of:
 * // - What do we mean by a descriptor
 * // - What is a descriptor used for
 * // - What is its content? Structure and format
 *
 * Mojo for generating the descriptor of the assembly
 */
@Mojo(name = "descriptor")
class AssemblyDescriptorGeneratorMojo extends AbstractMojo {

    /**
     * Name of the assembly file whose descriptor will be generated.
     */
    @Parameter(defaultValue = '${project.build.finalName}.zip')
    String assemblyFileName

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

    void execute() {

        if (skip) {
            log.info("Skipping descriptor generation.")
            return
        }

        printSplash()

        // 1. Build descriptor
        // INPUT: Archive
        // OUTPUT: Path of the descriptor file in the target folder
        // EXCEPTIONS: File not found

        // 2. Generate jar with descriptor and copies of files
        // INPUT: Descriptor, paths of files to copy
        // OUTPUT: Path of the descriptor archive
        // EXCEPTIONS: Files not found

        // 3. Attach descriptor
        // INPUT: The descriptor archive
        // OUTPUT: (Side effect) --> Archive with the descriptor archive added
        // EXCEPTIONS: Files not found
    }

    private void printSplash() {
        log.info "*" * 80
        log.info("Generating descriptor for the assembly".center(80))
        log.info "*" * 80
    }

    private File buildAssemblyFile() {
        File assemblyFile = Paths.get(project.build.directory, assemblyFileName).toFile()
        if (!assemblyFile.exists()) {
            throw new MojoExecutionException("Output file $assemblyFile does not exist.")
        }
        return assemblyFile
    }
}
