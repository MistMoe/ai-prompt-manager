param(
    [string]$MavenCommand = 'mvn.cmd'
)
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$gitDir = Join-Path $projectDir '.git'
$keyPath = Join-Path $gitDir 'prompt-jwt-secret.local'
$previousJwt = $env:JWT_SECRET
try {
    if (-not $env:JWT_SECRET) {
        if (-not (Test-Path -LiteralPath $keyPath)) {
            $random = New-Object byte[] 48
            $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
            try { $generator.GetBytes($random) } finally { $generator.Dispose() }
            [IO.File]::WriteAllText($keyPath, [Convert]::ToBase64String($random))
        }
        $env:JWT_SECRET = [IO.File]::ReadAllText($keyPath).Trim()
    }
    Write-Host 'JWT signing key loaded locally; its value will not be printed.'
    & $MavenCommand -f (Join-Path $projectDir 'prompt-manager-backend/pom.xml') spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'Backend exited with an error.' }
} finally { $env:JWT_SECRET = $previousJwt }
