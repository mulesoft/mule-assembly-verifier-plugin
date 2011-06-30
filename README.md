# Summary

Mule Assembly Verifier is a Maven 3 plugin providing fine-grained validation of the assembly/distribution contents.

## Isn't There a Standard 'Maven' Way of Doing the Same?

Kind of. Recent versions of the assembly plugin added flags for strict filter matching, but it's a drop in the bucket of
other problems like:

* Duplicate entries added to the distribution, resulting in overwrite prompts upon unpacking
* No way to validate where exactly the file landed in the distribution after a complex network of includes/excludes and
  scopes has been applied
* No way to know for sure (and at a glance, for maintainer) which dependency version will be resolved and packaged in the distribution
* No easy overview of the distribution layout
* etc.

This plugin solves above problems, often in a much friendlier way, and on top of that adds the following:

* In case a validation failure is detected, the plugin reports:
  * Missing entries from the distribution
  * Unexpected entries which we didn't intend to package
  * Duplicate entries - whenever a file has been included multiple times in the archive (typically due to the
    _assembly.xml_ configuration errors
* Usability features
  * Current project's version is inferred and can be used in a validation template (see below)
  * Maven 3-style snapshots are handled transparently (those pesky ones with the datestamp in the name instead of SNAPSHOT)

## Ok, I Saw the Light, "Show Me the Codes"!

Add a snippet like the one below to your pom's *build/plugins* section:

```xml
        <plugin>
            <groupId>org.mule.tools</groupId>
            <artifactId>mule-assembly-verifier</artifactId>
            <version>1.3</version>
            <dependencies>
                <!--
                    Declare a compatible groovy dependency for this plugin to avoid
                    conflicts with the main build.
                -->
                <dependency>
                    <groupId>org.codehaus.groovy</groupId>
                    <artifactId>groovy-all</artifactId>
                    <version>1.6.0</version>
                </dependency>
            </dependencies>

            <executions>
                <execution>
                    <phase>verify</phase>
                    <goals>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
```

The plugin is bound to the *verify* phase of the build, right after the *package*, and before *install*. If the distribution
layout and contents fail to validate, the build will halt and validation report be printed.

