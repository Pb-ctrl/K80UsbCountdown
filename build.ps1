param(
    [string]$SdkRoot = $env:ANDROID_HOME
)

$ErrorActionPreference = 'Stop'
if (!$SdkRoot) {
    $SdkRoot = $env:ANDROID_SDK_ROOT
}
if (!$SdkRoot) {
    throw 'Set ANDROID_HOME or pass -SdkRoot to the build script.'
}

$androidJar = Join-Path $SdkRoot 'platforms/android-35/android.jar'
$buildTools = Join-Path $SdkRoot 'build-tools/35.0.0'
$aapt2 = Join-Path $buildTools 'aapt2.exe'
$d8 = Join-Path $buildTools 'lib/d8.jar'
$zipalign = Join-Path $buildTools 'zipalign.exe'
foreach ($file in $androidJar, $aapt2, $d8, $zipalign) {
    if (!(Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Missing Android SDK file: $file"
    }
}

function Invoke-BuildTool([string]$Program, [string[]]$Arguments) {
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Program failed with exit code $LASTEXITCODE."
    }
}

$buildRoot = Join-Path $PSScriptRoot 'build'
$run = Join-Path $buildRoot ([Guid]::NewGuid().ToString('N'))
$classes = Join-Path $run 'classes'
$dex = Join-Path $run 'dex'
New-Item -ItemType Directory -Force $classes, $dex | Out-Null

$xposedApi = Join-Path $run 'xposed-api-82.jar'
$apiUrl = 'https://maven.aliyun.com/repository/public/de/robv/android/xposed/api/82/api-82.jar'
$expectedApiHash = 'F48C635F1C7469FDEC0E00AD2EA0B7A6B2F5B55065784A35B7CA3A84615E8E25'
Invoke-WebRequest -UseBasicParsing -Uri $apiUrl -OutFile $xposedApi
if ((Get-FileHash -Algorithm SHA256 -LiteralPath $xposedApi).Hash -ne $expectedApiHash) {
    throw 'Xposed API 82 JAR failed SHA-256 verification.'
}

$source = Join-Path $PSScriptRoot 'src/cn/local/k80usbcountdown/Hook.java'
$manifest = Join-Path $PSScriptRoot 'AndroidManifest.xml'
$assets = Join-Path $PSScriptRoot 'assets'
$compiled = Join-Path $run 'classes.jar'
$package = Join-Path $run 'unaligned.apk'
$output = Join-Path $buildRoot 'K80UsbCountdown-unsigned.apk'

Invoke-BuildTool javac @('--release', '8', '-encoding', 'UTF-8', '-cp', "$androidJar;$xposedApi", '-d', $classes, $source)
Invoke-BuildTool jar @('cf', $compiled, '-C', $classes, '.')
Invoke-BuildTool java @('-cp', $d8, 'com.android.tools.r8.D8', '--min-api', '28', '--lib', $androidJar, '--classpath', $xposedApi, '--output', $dex, $compiled)
Invoke-BuildTool $aapt2 @('link', '-o', $package, '--manifest', $manifest, '-I', $androidJar, '-A', $assets)
Invoke-BuildTool jar @('uf', $package, '-C', $dex, 'classes.dex')
Invoke-BuildTool $zipalign @('-f', '4', $package, $output)
Invoke-BuildTool $zipalign @('-c', '4', $output)
Write-Output "Built: $output"
