# Local helper (Windows) — regenerate upload keystore if you lost it (new key = new Play upload key!).
# Prefer the keystore already created: racktrack-upload.p12 + keystore.properties (gitignored).

param(
    [string]$Alias = "racktrack",
    [string]$OutFile = "racktrack-upload.p12"
)

$ErrorActionPreference = "Stop"
$keytool = (Get-Command keytool -ErrorAction SilentlyContinue)?.Source
if (-not $keytool) {
    throw "keytool not found on PATH. Install a JDK."
}

$pass = -join ((48..57 + 65..90 + 97..122) | Get-Random -Count 24 | ForEach-Object { [char]$_ })

& $keytool -genkeypair -v `
    -keystore $OutFile `
    -storetype PKCS12 `
    -keyalg RSA -keysize 2048 -validity 10000 `
    -alias $Alias `
    -storepass $pass -keypass $pass `
    -dname "CN=RackTrack Upload, OU=MappM, O=MappM, C=FR"

@"
storeFile=$OutFile
storePassword=$pass
keyAlias=$Alias
keyPassword=$pass
"@ | Set-Content -Path "keystore.properties" -Encoding ascii

@"
# LOCAL ONLY — never commit
storeFile=$OutFile
keyAlias=$Alias
storePassword=$pass
keyPassword=$pass

# GitHub secret RACKTRACK_KEYSTORE_BASE64:
#   [Convert]::ToBase64String([IO.File]::ReadAllBytes('$OutFile')) | Set-Clipboard
"@ | Set-Content -Path "keystore.BACKUP.local.txt" -Encoding utf8

Write-Host "Created $OutFile and keystore.properties. Backup keystore.BACKUP.local.txt offline."
Write-Host "Then: ./gradlew :app:bundleRelease"
