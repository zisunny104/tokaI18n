param([string]$Maven='mvn',[string]$Repository=(Join-Path (Split-Path $PSScriptRoot -Parent) 'plugin-modernization/build/m2'))
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
  & $Maven "-Dmaven.repo.local=$Repository" clean install
  if($LASTEXITCODE -ne 0){throw '編譯或測試失敗'}
} finally {Pop-Location}
