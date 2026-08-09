param(
    [string]$JarPath = "target/account-service-0.1.0-SNAPSHOT.jar",
    [string]$Timezone = "Asia/Kolkata",
    [int]$Port = 9082
)

# Resolve jar path relative to the script location so the script can be invoked from any cwd
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$absJar = Join-Path -Path $scriptDir -ChildPath "..\$JarPath" | Resolve-Path -ErrorAction SilentlyContinue
if ($absJar) { $absJar = $absJar.Path }

if (-not $absJar -or -not (Test-Path $absJar)) {
    Write-Error "Jar not found: $absJar"; exit 2
}

Write-Output "Starting account-service from $absJar (port $Port)"
Start-Process -FilePath 'java' -ArgumentList "-Duser.timezone=$Timezone","-jar","$absJar" -WorkingDirectory $scriptDir -WindowStyle Hidden
Write-Output "Started (background). Use Get-Process -Name java or netstat -ano to verify."
