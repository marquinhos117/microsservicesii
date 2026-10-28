Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  INICIALIZADOR DOS 3 MICROSSERVICOS (DDD / E-COMMERCE)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Iniciando Cliente-Service na porta 8081..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\cliente-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 4

Write-Host "[2/3] Iniciando Estoque-Service na porta 8082..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\estoque-service'; .\mvnw.cmd spring-boot:run"

Start-Sleep -Seconds 4

Write-Host "[3/3] Iniciando Pedido-Service na porta 8083..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\pedido-service'; .\mvnw.cmd spring-boot:run"

Write-Host ""
Write-Host "Todos os 3 microsservicos foram iniciados em terminais dedicados!" -ForegroundColor Green
Write-Host "- Cliente Service (8081): http://localhost:8081/swagger-ui/index.html" -ForegroundColor White
Write-Host "- Estoque Service (8082): http://localhost:8082/swagger-ui/index.html" -ForegroundColor White
Write-Host "- Pedido Service  (8083): http://localhost:8083/swagger-ui/index.html" -ForegroundColor White
