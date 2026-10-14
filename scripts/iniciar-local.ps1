# TODO EN LOCAL: DynamoDB Local + backend + frontend (el frontend lo sirve el propio backend).
# No necesita cuenta de AWS, Docker ni internet (salvo la primera vez, para que Maven descargue librerias).
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\comun.ps1"
Set-Location $script:raiz
Use-Java25

# 1. DynamoDB Local en el puerto 8000 (otra ventana)
if (Test-Puerto 8000) {
    Write-Host "DynamoDB Local ya estaba corriendo en el puerto 8000." -ForegroundColor DarkGray
} else {
    Write-Host "Iniciando DynamoDB Local (puerto 8000) en otra ventana..." -ForegroundColor Cyan
    $cmd = "`$env:JAVA_HOME='$env:JAVA_HOME'; `$env:PATH='$env:JAVA_HOME\bin;'+`$env:PATH; Set-Location '$script:raiz'; " +
           "& .\mvnw.cmd -q test-compile exec:java '-Dexec.classpathScope=test' '-Dexec.mainClass=com.rentacars.LocalDynamoDb'"
    Start-Process powershell -ArgumentList '-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', $cmd
    for ($i = 0; $i -lt 90 -and -not (Test-Puerto 8000); $i++) { Start-Sleep 2 }
    if (-not (Test-Puerto 8000)) { Write-Host "DynamoDB Local no arranco; revisa la otra ventana." -ForegroundColor Red; exit 1 }
}

# 2. Backend (perfil "local" = apunta a DynamoDB Local). Sirve tambien el frontend.
if (Test-Puerto 8080) {
    Write-Host "El puerto 8080 ya esta ocupado (el backend ya esta corriendo?)." -ForegroundColor Yellow
    exit 1
}
Write-Host "Backend + frontend en  http://localhost:8080   (Swagger: /swagger-ui.html)" -ForegroundColor Cyan
Write-Host "Cierra esta ventana o pulsa Ctrl+C para detener el backend." -ForegroundColor DarkGray
Start-Job { Start-Sleep 25; Start-Process 'http://localhost:8080' } | Out-Null
& "$script:raiz\mvnw.cmd" spring-boot:run '-Dspring-boot.run.profiles=local'
