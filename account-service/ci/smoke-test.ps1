param(
    [string]$Url = 'http://localhost:9082/actuator/health',
    [int]$Attempts = 5,
    [int]$DelaySeconds = 2
)

for ($i = 1; $i -le $Attempts; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri $Url -UseBasicParsing -ErrorAction Stop
        if ($resp.StatusCode -eq 200) {
            Write-Output "Health check succeeded (HTTP 200)."
            exit 0
        }
        else {
            Write-Output "Health check returned status $($resp.StatusCode)."
        }
    }
    catch {
        Write-Output "Attempt $($i): health check failed: $($_.Exception.Message)"
    }
    Start-Sleep -Seconds $DelaySeconds
}

Write-Error "Health check failed after $Attempts attempts."; exit 1
