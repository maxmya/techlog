<#
.SYNOPSIS
  Builds the bench image locally and deploys it over SSH to the droplet.

.DESCRIPTION
  Requires Docker Desktop (local build), OpenSSH and scp on PATH.
  The image is saved as a tar, copied to the server, loaded into Docker,
  and run as a container with a persistent data volume.

.EXAMPLE
  .\deploy.ps1
  .\deploy.ps1 -Server 164.92.190.246 -SshKey "$env:USERPROFILE\.ssh\id_ed25519"
  .\deploy.ps1 -SkipBuild -AdminToken "a-long-random-string"
#>
[CmdletBinding()]
param(
  [string] $Server,
  [string] $User = 'root',
  [string] $SshKey = '',
  [string] $Image = 'maxmya/bench:local',
  [string] $Container = 'bench',
  [int] $Port = 80,
  [string] $Volume = 'bench-data',
  [string] $AdminToken,
  [switch] $SkipBuild,
  [switch] $SkipFrontend
)

$ErrorActionPreference = 'Stop'

if (-not $Server) {
  if ($env:BENCH_SERVER) { $Server = $env:BENCH_SERVER } else { $Server = 'maxmya.com' }
}
if (-not $AdminToken) { $AdminToken = $env:ADMIN_TOKEN }

function Write-Step([string] $text) {
  Write-Host "`n[bench] $text" -ForegroundColor Cyan
}

Write-Host @"
 ______  __   __  _______  _______  _______
|   _  \|  | |  ||   _   ||       ||       |
|  |_|  |  | |  ||  |_|  ||    _  ||   _   |
|   _  <|  |_|  ||       ||   |_| ||  | |  |
|  |_|  |       ||   _   ||    _  ||  |_|  |
|______/|__|_|__||__| |__||___| |_||_______|

       // deploy from the workbench
"@ -ForegroundColor Green

# --- local prerequisites -------------------------------------------------
$docker = Get-Command docker -ErrorAction SilentlyContinue
if (-not $docker) {
  throw "Docker is required locally. Install Docker Desktop (https://www.docker.com/products/docker-desktop/), start it, then run this script again."
}
foreach ($tool in @('ssh', 'scp')) {
  if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
    throw "OpenSSH client is required but '$tool' was not found on PATH."
  }
}

if ($AdminToken -and $AdminToken -notmatch '^[A-Za-z0-9_\-]{8,}$') {
  throw 'ADMIN_TOKEN must be at least 8 chars and use only letters, numbers, _ or -.'
}

$sshArgs = @('-o', 'StrictHostKeyChecking=accept-new', '-o', 'ConnectTimeout=15')
if ($SshKey) { $sshArgs += @('-i', $SshKey) }
$target = "$User@$Server"

# --- build ----------------------------------------------------------------
if (-not $SkipBuild) {
  if (-not $SkipFrontend) {
    Write-Step 'Building Angular bundle'
    & npm --prefix frontend run build
    if ($LASTEXITCODE -ne 0) { throw 'Angular build failed.' }
  }

  Write-Step "Building Docker image $Image"
  & docker build -t $Image .
  if ($LASTEXITCODE -ne 0) { throw 'Docker build failed.' }
} else {
  Write-Step 'Skipping build (-SkipBuild)'
}

$archive = Join-Path (Get-Location) 'bench-image.tar'

try {
  Write-Step "Saving image to $archive"
  & docker save $Image -o $archive
  if ($LASTEXITCODE -ne 0) { throw 'docker save failed.' }

  Write-Step "Copying image to $target"
  & scp @sshArgs $archive "${target}:/tmp/bench-image.tar"
  if ($LASTEXITCODE -ne 0) { throw 'scp failed. Check SSH access to the server.' }

  $remote = @"
set -e
echo 'Loading image'
docker load -i /tmp/bench-image.tar
echo 'Preparing volume'
docker volume create $Volume >/dev/null
echo 'Replacing container'
docker rm -f $Container >/dev/null 2>&1 || true
docker run -d --name $Container --restart unless-stopped \
  -p $Port:8080 \
  -v ${Volume}:/app/data \
  -e SITE_URL='https://maxmya.com' \
  -e ADMIN_TOKEN='$AdminToken' \
  $Image
rm -f /tmp/bench-image.tar
docker ps --filter name=$Container --format '{{.Names}} {{.Status}}'
"@

  Write-Step "Starting container on $Server"
  & ssh @sshArgs $target $remote
  if ($LASTEXITCODE -ne 0) { throw 'Remote deployment failed.' }

  Write-Step 'Waiting for health check'
  $healthy = $false
  for ($i = 0; $i -lt 20; $i++) {
    & ssh @sshArgs $target "docker exec $Container wget -q -O - http://127.0.0.1:8080/api/health" 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $healthy = $true; break }
    Start-Sleep -Seconds 2
  }
  if (-not $healthy) { throw 'Container did not become healthy in time. Check: docker logs bench' }

  Write-Host "`n[bench] Live at http://$Server/ (or https://maxmya.com if DNS is pointed here)" -ForegroundColor Green
}
finally {
  if (Test-Path $archive) { Remove-Item $archive -Force -ErrorAction SilentlyContinue }
}
