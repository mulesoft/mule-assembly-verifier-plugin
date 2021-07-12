package org.mule.tools.assembly.descriptor

import groovy.transform.ToString;

@ToString
class AssemblyEntry {

    String name
    long size
    Date lastModifiedDate
}
