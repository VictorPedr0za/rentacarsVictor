# Funciones compartidas por los scripts de arranque (se cargan con ". .\comun.ps1").

$script:raiz = Split-Path -Parent $PSScriptRoot

# Version mayor de un JDK, leyendo el archivo "release" (evita ejecutar java -version).
function Get-JavaMajor($carpeta) {
    if (-not (Test-Path (Join-Path $carpeta 'bin\java.exe'))) { return 0 }
    $release = Join-Path $carpeta 'release'
    if (Test-Path $release) {
        $linea = Get-Content $release | Where-Object { $_ -match '^JAVA_VERSION=' } | Select-Object -First 1
        if ($linea -match '"(\d+)') { return [int]$Matches[1] }
    }
    return 0
}

# Deja JAVA_HOME y PATH apuntando a un Java 25 o superior (el proyecto lo necesita).
function Use-Java25 {
    $javaHome = $null
    if ($env:JAVA_HOME -and (Get-JavaMajor $env:JAVA_HOME) -ge 25) { $javaHome = $env:JAVA_HOME }
    if (-not $javaHome) {
        $candidatos = @()
        foreach ($base in "$env:USERPROFILE\.jdks", "$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Amazon Corretto") {
            if (Test-Path $base) { $candidatos += Get-ChildItem $base -Directory -ErrorAction SilentlyContinue }
        }
        $javaHome = $candidatos | Where-Object { (Get-JavaMajor $_.FullName) -ge 25 } |
            Sort-Object Name -Descending | Select-Object -First 1 -ExpandProperty FullName
    }
    if (-not $javaHome) {
        Write-Host "No encontre Java 25 o superior. Instalalo (o abre el proyecto en IntelliJ, que lo descarga)." -ForegroundColor Red
        exit 1
    }
    $env:JAVA_HOME = $javaHome
    $env:PATH = "$javaHome\bin;$env:PATH"
    Write-Host "Java: $javaHome" -ForegroundColor DarkGray
}

# Lee el archivo .env (CLAVE=valor) y devuelve un hashtable. Las lineas con # se ignoran.
function Read-DotEnv {
    $archivo = Join-Path $script:raiz '.env'
    $vars = @{}
    if (-not (Test-Path $archivo)) { return $vars }
    foreach ($linea in Get-Content $archivo) {
        if ($linea -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$' -and $linea -notmatch '^\s*#') {
            $vars[$Matches[1]] = $Matches[2].Trim()
        }
    }
    return $vars
}

# true si algo ya escucha en ese puerto
function Test-Puerto($puerto) {
    return [bool](Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue)
}
