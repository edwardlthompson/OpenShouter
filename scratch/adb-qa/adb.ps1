$script:Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

function Invoke-OsAdb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
    & $script:Adb @Args
}

function Wait-OsDevices {
    for ($i = 0; $i -lt 12; $i++) {
        $out = & $script:Adb devices
        if ($out -match "8bf09993" -and $out -match "b5214fc6") {
            return
        }
        Start-Sleep -Seconds 1
        & $script:Adb start-server | Out-Null
    }
    throw "devices missing"
}

function Dump-OsUi {
    param([string]$Serial, [string]$OutFile)
    Wait-OsDevices
    Invoke-OsAdb -s $Serial shell uiautomator dump /sdcard/uidump.xml | Out-Null
    Invoke-OsAdb -s $Serial pull /sdcard/uidump.xml $OutFile | Out-Null
}

function Tap-OsText {
    param([string]$Serial, [string]$Xml, [string]$Needle)
    $xy = python3 C:\Users\edwar\OpenShouter\scratch\adb-qa\ui_tap.py $Xml $Needle
    if ($LASTEXITCODE -ne 0) { throw "missing tap target: $Needle" }
    $parts = $xy.Trim() -split "\s+"
    Invoke-OsAdb -s $Serial shell input tap $parts[0] $parts[1]
}

function Start-OsApp {
    param([string]$Serial)
    Invoke-OsAdb -s $Serial shell am start -S -n org.openshouter/dev.foss.goldenpath.MainActivity | Out-Null
    Start-Sleep -Seconds 3
}
