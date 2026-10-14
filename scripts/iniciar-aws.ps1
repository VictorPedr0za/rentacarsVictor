# BACKEND + FRONTEND en tu PC con la base de datos DynamoDB REAL en AWS.
# Lee las credenciales del archivo .env (copia .env.example como .env y completalo).
# Al arrancar crea las tablas rentacars_* en tu cuenta si no existen, y carga datos de ejemplo.
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\comun.ps1"
Set-Location $script:raiz

$vars = Read-DotEnv
if (-not $vars.ContainsKey('AWS_ACCESS_KEY_ID') -or -not $vars['AWS_ACCESS_KEY_ID'] -or -not $vars['AWS_SECRET_ACCESS_KEY']) {
    Write-Host "Faltan las credenciales de AWS. Abre el archivo .env y completa AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY." -ForegroundColor Red
    Write-Host "(Como crearlas: GUIA_DYNAMODB.md, paso 4)" -ForegroundColor Yellow
    exit 1
}
Use-Java25

$env:AWS_ACCESS_KEY_ID = $vars['AWS_ACCESS_KEY_ID']
$env:AWS_SECRET_ACCESS_KEY = $vars['AWS_SECRET_ACCESS_KEY']
$env:APP_DYNAMODB_REGION = $(if ($vars['AWS_REGION']) { $vars['AWS_REGION'] } else { 'us-east-1' })
if ($vars['DYNAMODB_TABLE_PREFIX']) { $env:APP_DYNAMODB_TABLE_PREFIX = $vars['DYNAMODB_TABLE_PREFIX'] }
# sin endpoint = DynamoDB real
Remove-Item Env:APP_DYNAMODB_ENDPOINT -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue

if (Test-Puerto 8080) {
    Write-Host "El puerto 8080 ya esta ocupado (el backend ya esta corriendo?)." -ForegroundColor Yellow
    exit 1
}
Write-Host "DynamoDB: AWS, region $env:APP_DYNAMODB_REGION" -ForegroundColor DarkGray
Write-Host "Backend + frontend en  http://localhost:8080   (Swagger: /swagger-ui.html)" -ForegroundColor Cyan
Write-Host "Cierra esta ventana o pulsa Ctrl+C para detener." -ForegroundColor DarkGray
Start-Job { Start-Sleep 25; Start-Process 'http://localhost:8080' } | Out-Null
& "$script:raiz\mvnw.cmd" spring-boot:run
