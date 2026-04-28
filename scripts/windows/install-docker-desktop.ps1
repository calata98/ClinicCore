$ErrorActionPreference = "Stop"

function Test-Administrator {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($identity)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

if (-not (Test-Administrator)) {
    if (-not $PSCommandPath) {
        throw "Run this script from an elevated PowerShell terminal."
    }

    Write-Host "Re-launching Docker Desktop setup as Administrator. Accept the Windows UAC prompt to continue."
    Start-Process -FilePath "powershell.exe" `
        -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" `
        -Verb RunAs `
        -Wait
    exit
}

$installerDir = Join-Path $env:TEMP "ClinicCoreDockerSetup"
$installer = Join-Path $installerDir "DockerDesktopInstaller.exe"
$dockerDesktop = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
$dockerCli = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"
$installLog = Join-Path $env:LOCALAPPDATA "Docker\install-log.txt"

New-Item -ItemType Directory -Force -Path $installerDir | Out-Null

if (-not (Test-Path -LiteralPath $installer)) {
    Write-Host "Downloading Docker Desktop installer..."
    curl.exe -L "https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe" -o $installer
}

& wsl.exe --status *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "WSL is not available yet. Docker Desktop setup may enable the required Windows features and request a restart."
}

Write-Host "Installing Docker Desktop. This script must run as Administrator."
Start-Process -FilePath $installer -ArgumentList "install --quiet --accept-license" -Wait

if (-not (Test-Path -LiteralPath $dockerDesktop)) {
    throw "Docker Desktop was not installed. Check UAC/admin permissions and Docker installer logs at $installLog."
}

Write-Host "Docker Desktop installed at: $dockerDesktop"
Write-Host "Docker CLI expected at: $dockerCli"
Write-Host "If Windows or Docker asks for a restart, restart Windows and then open Docker Desktop once."
