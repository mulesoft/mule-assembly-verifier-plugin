File buildLog = new File(basedir, "build.log");
assert buildLog.isFile()

def buildLogText = buildLog.text
assert buildLogText instanceof String

assert buildLogText ==~ /(?s).*--- mule-assembly-verifier:${pluginVersion}:verify.*/
assert buildLogText ==~ /(?s).*Verifying contents of the assembly.*/

assert buildLogText ==~ "(?s).*" +
        "\\[ERROR\\]\\s+Missing from the Distribution:.*" +
        "\\[ERROR\\]\\s+1. /the-assembly-1.0.0/lib/opt/another-jar.jar\\s+" +
        "\\[ERROR\\]\\s+Unexpected entries in the Distribution:\\s+" +
        "\\[ERROR\\]\\s+1. /the-assembly-1.0.0/lib/opt/a-jar.jar\\s+" +
        ".*"
