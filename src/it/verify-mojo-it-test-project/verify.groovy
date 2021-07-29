/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
File buildLog = new File( basedir, "build.log" );
assert buildLog.isFile()

def buildLogText = buildLog.text
assert buildLogText instanceof String

assert buildLogText ==~ /(?s).*--- mule-assembly-verifier:${pluginVersion}:verify.*/
assert buildLogText ==~ /(?s).*Verifying contents of the assembly.*/
