#!/usr/bin/env pwsh
# Startup script for all banking transaction engine services

Write-Output "Starting Banking Transaction Engine Services..."
Write-Output "=================================================="
Write-Output ""

$services = @(
    @{
        Name = "auth-service"
        Port = 9081
        Jar = "D:\tools\services\banking-transaction-engine\auth-service\target\auth-service-0.1.0-SNAPSHOT.jar"
        Log = "D:\tools\services\banking-transaction-engine\auth-service-startup.log"
        Args = "-Duser.timezone=Asia/Kolkata"
    },
    @{
        Name = "account-service"
        Port = 9082
        Jar = "D:\tools\services\banking-transaction-engine\account-service\target\account-service-0.1.0-SNAPSHOT.jar"
        Log = "D:\tools\services\banking-transaction-engine\account-service-startup.log"
        Args = "-Duser.timezone=Asia/Kolkata"
    },
    @{
        Name = "transaction-service"
        Port = 9083
        Jar = "D:\tools\services\banking-transaction-engine\transaction-service\target\transaction-service-0.1.0-SNAPSHOT.jar"
        Log = "D:\tools\services\banking-transaction-engine\transaction-service-startup.log"
        Args = "-Duser.timezone=Asia/Kolkata"
    },
    @{
        Name = "api-gateway"
        Port = 9080
        Jar = "D:\tools\services\banking-transaction-engine\api-gateway\target\api-gateway-0.1.0-SNAPSHOT.jar"
        Log = "D:\tools\services\banking-transaction-engine\api-gateway-startup.log"
        Args = ""
    }
)

foreach ($service in $services) {
    Write-Output "Starting $($service.Name) on port $($service.Port)..."
    
    if (-not (Test-Path $service.Jar)) {
        Write-Output "  ERROR: JAR not found at $($service.Jar)"
        continue
    }
    
    $args = @($service.Args, "-jar", $service.Jar) | Where-Object { $_ }
    
    Start-Process -FilePath 'java' `
        -ArgumentList $args `
        -RedirectStandardOutput $service.Log `
        -RedirectStandardError $service.Log `
        -WindowStyle Hidden
    
    Write-Output "  Started (PID monitoring). Logs: $($service.Log)"
    Start-Sleep -Seconds 2
}

Write-Output ""
Write-Output "=================================================="
Write-Output "All services started. Waiting for initialization..."
Write-Output "This may take 20-30 seconds..."
Start-Sleep -Seconds 15

Write-Output ""
Write-Output "Checking service status:"
$ports = @(9081, 9082, 9083, 9080)
foreach ($port in $ports) {
    $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue
    $status = if ($connection.TcpTestSucceeded) { "✓ READY" } else { "✗ Starting..." }
    Write-Output "  Port $port: $status"
}

Write-Output ""
Write-Output "Ready for testing. Try:"
Write-Output '  curl -X POST http://localhost:9080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"username\":\"raj\",\"password\":\"Password@123\"}"'
