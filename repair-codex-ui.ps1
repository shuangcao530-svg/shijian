$ErrorActionPreference = "Continue"

Write-Host "== Codex UI repair helper ==" -ForegroundColor Cyan

Write-Host "`n[1/4] Stopping Codex / ChatGPT background processes..." -ForegroundColor Yellow
Get-Process ChatGPT,codex -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "Stopping $($_.ProcessName) PID=$($_.Id)"
    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
}

Start-Sleep -Seconds 2

Write-Host "`n[2/4] Backing up model cache if it exists..." -ForegroundColor Yellow
$cachePath = Join-Path $env:USERPROFILE ".codex\models_cache.json"
if (Test-Path -LiteralPath $cachePath) {
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $backupPath = "$cachePath.bak-$stamp"
    Rename-Item -LiteralPath $cachePath -NewName (Split-Path -Leaf $backupPath) -ErrorAction Stop
    Write-Host "Backed up: $backupPath" -ForegroundColor Green
} else {
    Write-Host "No models_cache.json found, skipping."
}

Write-Host "`n[3/4] Checking installed OpenAI packages..." -ForegroundColor Yellow
Get-AppxPackage | Where-Object {
    $_.Name -match "OpenAI|Codex|ChatGPT"
} | Select-Object Name, Version, PackageFullName | Format-Table -AutoSize

Write-Host "`n[4/4] Opening repair/update pages..." -ForegroundColor Yellow
Write-Host "Opening Windows app settings. Choose OpenAI Codex -> Advanced options -> Repair first, Reset only if needed."
Start-Process "ms-settings:appsfeatures"

Write-Host "Opening Microsoft Store downloads/updates page."
Start-Process "ms-windows-store://downloadsandupdates"

Write-Host "`nDone. After updating/repairing, reopen Codex or VS Code." -ForegroundColor Green
Write-Host "If VS Code extension is old, update or reinstall the OpenAI/ChatGPT/Codex extension from VS Code Extensions."
