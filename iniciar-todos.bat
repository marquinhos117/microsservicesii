@echo off
echo ========================================================
echo   INICIALIZADOR DOS 3 MICROSSERVICOS (DDD / E-COMMERCE)
echo ========================================================
echo.

echo [1/3] Iniciando Cliente-Service na porta 8081...
start "Cliente Service (8081)" cmd /k "cd cliente-service && mvnw.cmd spring-boot:run"

timeout /t 4 /nobreak > nul

echo [2/3] Iniciando Estoque-Service na porta 8082...
start "Estoque Service (8082)" cmd /k "cd estoque-service && mvnw.cmd spring-boot:run"

timeout /t 4 /nobreak > nul

echo [3/3] Iniciando Pedido-Service na porta 8083...
start "Pedido Service (8083)" cmd /k "cd pedido-service && mvnw.cmd spring-boot:run"

echo.
echo Todos os servicos foram iniciados em novas janelas!
echo.
echo - Cliente Service: http://localhost:8081/swagger-ui/index.html
echo - Estoque Service: http://localhost:8082/swagger-ui/index.html
echo - Pedido Service:  http://localhost:8083/swagger-ui/index.html
echo.
pause
