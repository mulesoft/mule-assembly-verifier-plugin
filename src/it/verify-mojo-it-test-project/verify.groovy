File buildLog = new File( basedir, "build.log" );
assert buildLog.isFile()

def buildLogText = buildLog.text
assert buildLogText instanceof String

assert buildLogText ==~ /(?s).*--- mule-assembly-verifier:${pluginVersion}:verify.*/
assert buildLogText ==~ /(?s).*Verifying contents of the assembly.*/
