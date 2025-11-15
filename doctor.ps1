Param(
    [switch]$Full
)
Write-Host "=== Swieczkomat Doctor ==="
$errors = @()

function Check-Command($name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) { $errors += "Brak polecenia: $name"; Write-Warning "Nie znaleziono: $name" } else { Write-Host "OK: $name" }
}

# JDK
$javaVersion = (& java -version 2>&1 | Select-Object -First 1)
if ($javaVersion) { Write-Host "Java: $javaVersion" } else { $errors += "Brak Java w PATH" }

# Gradle wrapper wersja
$wrapperProps = Get-Content -Path "$PSScriptRoot\gradle\wrapper\gradle-wrapper.properties" -Raw
if ($wrapperProps -match 'distributionUrl=.*gradle-(?<ver>[0-9.]+)-bin.zip') {
  $gradleVer = $Matches['ver']; Write-Host "Gradle wrapper: $gradleVer" }
else { $errors += "Nie wykryto wersji Gradle" }

# Android SDK
$localProps = Get-Content -Path "$PSScriptRoot\local.properties" -Raw
if ($localProps -match 'sdk.dir=(?<sdk>.+)') {
  $sdkPath = ($Matches['sdk'] -replace '\\', '/'); Write-Host "SDK: $sdkPath"
  if (-not (Test-Path $sdkPath)) { $errors += "Ścieżka SDK nie istnieje" }
} else { $errors += "Brak sdk.dir w local.properties" }

# CompileSdk z build.gradle.kts modułu app
$appGradle = Get-Content "$PSScriptRoot\app\build.gradle.kts" -Raw
if ($appGradle -match 'compileSdk\s*=\s*(?<sdk>\d+)') { Write-Host "compileSdk: $($Matches['sdk'])" }
if ($appGradle -match 'minSdk\s*=\s*(?<minsdk>\d+)') { Write-Host "minSdk: $($Matches['minsdk'])" }
if ($appGradle -match 'targetSdk\s*=\s*(?<targetsdk>\d+)') { Write-Host "targetSdk: $($Matches['targetsdk'])" }

# Wersje katalogu versions
$versionsBlock = ($wrapperProps + "`n" + (Get-Content "$PSScriptRoot\gradle\libs.versions.toml" -Raw))
($versionsBlock | Select-String -Pattern 'agp =|kotlin =|composeBom =|ksp =') | ForEach-Object { Write-Host $_.Line }

# Opcjonalna pełna lista tasków
if ($Full) {
  Write-Host "Pobieram listę zadań Gradle..."; & .\gradlew.bat tasks --all | Select-String -Pattern 'assemble|build|test' | ForEach-Object { $_.Line }
}

if ($errors.Count -eq 0) { Write-Host "=== Doctor: OK (brak krytycznych problemów) ===" -ForegroundColor Green } else {
  Write-Host "=== Doctor: Wykryto problemy ===" -ForegroundColor Yellow
  $errors | ForEach-Object { Write-Host " - $_" }
  exit 1
}

