$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$errors = 0

function Invoke-Step([scriptblock]$Block) {
  try {
    & $Block
  } catch {
    $script:errors++
  }
}

Invoke-Step {
  $forbiddenRegex = @(
    'node_modules/',
    '(^|/)dist/',
    'examples/android/.*/build/|^build/',
    '(^|/)target/',
    '(^|/)coverage/',
    '\.gradle/',
    '__pycache__/',
    '\.apk$',
    '\.aab$',
    '^\.env$|/\.env$',
    'examples/web/public/app-update\.json$|examples/web/public/donations\.json$|examples/android/app/src/main/assets/app-update\.json$|examples/android/app/src/main/assets/donations\.json$',
    '\.DS_Store$|Thumbs\.db$'
  )
  $tracked = git ls-files
  $found = 0
  foreach ($pattern in $forbiddenRegex) {
    $matches = $tracked | Where-Object { $_ -match $pattern }
    if ($matches) {
      Write-Host "TRACKED FORBIDDEN: $($matches -join ', ')"
      $found++
    }
  }
  if ($found -gt 0) {
    $script:errors++
  } else {
    Write-Host "Tracked artifact check passed"
  }
}
Invoke-Step {
  $maxBytes = 500 * 1024
  $largeFiles = git ls-files | ForEach-Object {
    if (Test-Path $_) {
      $len = (Get-Item $_).Length
      if ($len -gt $maxBytes) { $_ }
    }
  }
  if ($largeFiles) {
    Write-Host "Large tracked files found: $($largeFiles -join ', ')"
    $script:errors++
  } else {
    Write-Host "Large tracked file check passed"
  }
}

$required = @("node_modules/", "dist/", ".env", "__pycache__/", "coverage/")
if (Test-Path ".gitignore") {
  $ignore = Get-Content ".gitignore" -Raw
  foreach ($entry in $required) {
    if ($ignore -notlike "*$entry*") {
      Write-Host "MISSING .gitignore entry: $entry"
      $errors++
    }
  }
} else {
  Write-Host "MISSING: .gitignore"
  $errors++
}

foreach ($f in @(".gitattributes", ".editorconfig")) {
  if (-not (Test-Path $f)) {
    Write-Host "MISSING: $f"
    $errors++
  }
}

if ($errors -gt 0) {
  Write-Host "$errors repo hygiene check(s) failed"
  exit 1
}

Write-Host "Repo hygiene checks passed"
