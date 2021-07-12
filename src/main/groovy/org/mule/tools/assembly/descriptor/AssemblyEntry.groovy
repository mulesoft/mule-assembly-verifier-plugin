package org.mule.tools.assembly.descriptor

import groovy.transform.ToString;

@ToString
class AssemblyEntry {

    String name

    // @todo: Get @theales opinion about these properties
    long size
    boolean isDirectory
    Date lastModifiedDate
}
