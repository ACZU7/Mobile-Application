param(
    [string]$GroupId = 'GROUPID_PENDING',
    [Parameter(Mandatory = $true)][string]$StudentNumber
)
$ErrorActionPreference = 'Stop'
if ($GroupId -notmatch '^[A-Za-z0-9_-]+$' -or $StudentNumber -notmatch '^\d+$') {
    throw 'Use a group identifier containing only letters, numbers, underscores or hyphens and a numeric student number.'
}
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$outputRoot = Join-Path $projectRoot 'submission-output'
New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null
$zipPath = Join-Path $outputRoot "MACoursework_${GroupId}_${StudentNumber}.zip"
if (Test-Path -LiteralPath $zipPath) { throw "Archive already exists: $zipPath. Preserve or rename it before making another." }
Add-Type -AssemblyName System.IO.Compression
$zipStream = [System.IO.File]::Open($zipPath, [System.IO.FileMode]::CreateNew)
$archive = New-Object System.IO.Compression.ZipArchive($zipStream, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    $files = git -C $projectRoot ls-files
    if ($LASTEXITCODE -ne 0) { throw 'Could not read tracked project files.' }
    foreach ($file in $files) {
        if ($file -match '^(\.idea/|\.git/|\.tooling/|submission-output/)' -or $file -eq 'local.properties') { continue }
        $absolute = Join-Path $projectRoot $file
        if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) { continue }
        $entry = $archive.CreateEntry("AndroidStudioProject/$file", [System.IO.Compression.CompressionLevel]::Optimal)
        $destination = $entry.Open()
        $source = [System.IO.File]::OpenRead($absolute)
        try { $source.CopyTo($destination) } finally { $source.Dispose(); $destination.Dispose() }
    }
    foreach ($name in 'Contribution_Mini_Report_Review.docx', 'Design_Document_Review.docx') {
        $absolute = Join-Path $projectRoot "docs/submission/$name"
        if (-not (Test-Path -LiteralPath $absolute)) { throw "Missing required review document: $name" }
        $entry = $archive.CreateEntry($name)
        $destination = $entry.Open()
        $source = [System.IO.File]::OpenRead($absolute)
        try { $source.CopyTo($destination) } finally { $source.Dispose(); $destination.Dispose() }
    }
    $entry = $archive.CreateEntry('READ_BEFORE_SUBMISSION.txt')
    $writer = New-Object System.IO.StreamWriter($entry.Open())
    try {
        $writer.WriteLine('Review package: confirm Group ID, both student details and signatures, peer review and final group video before submission.')
        $writer.WriteLine('See AndroidStudioProject/docs/submission/README.md. Both members must explain their own code in a group video under eight minutes.')
        $writer.WriteLine('The Android Studio project excludes local.properties. Configure your local Android SDK in Android Studio.')
    } finally { $writer.Dispose() }
} finally { $archive.Dispose(); $zipStream.Dispose() }
Write-Output $zipPath
