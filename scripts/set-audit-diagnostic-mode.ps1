param(
    [Parameter(Mandatory)]
    [ValidateSet('NORMAL', 'SLOW', 'FAIL')]
    [string]$Mode,

    [ValidateRange(1, 120)]
    [int]$Seconds = 120
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$controlDir = Join-Path $projectRoot 'runtime-controls'

New-Item -ItemType Directory -Force `
    -Path $controlDir | Out-Null

$target = Join-Path $controlDir 'audit-fault.properties'
$temp = Join-Path $controlDir (
    'audit-fault-' + [guid]::NewGuid().ToString('N') + '.tmp'
)

$expiresAt = [DateTimeOffset]::UtcNow.AddSeconds($Seconds)

if ($Mode -eq 'NORMAL') {
    $expiresAt = [DateTimeOffset]::UtcNow
}

@(
    "mode=$Mode"
    "expiresAt=$($expiresAt.ToString('O'))"
) | Set-Content -Path $temp -Encoding utf8

[System.IO.File]::Move($temp, $target, $true)

[pscustomobject]@{
    Mode = $Mode
    ExpiresAt = $expiresAt.ToString('O')
}
