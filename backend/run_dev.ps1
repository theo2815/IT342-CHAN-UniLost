
# HulamPay Backend Run Script
# Usage: ./run_dev.ps1

# Load Environment Variables from .env
$envFile = ".env"
if (Test-Path $envFile) {
    Write-Host "Loading secrets from $envFile..."
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^\s*([^#=]+)\s*=\s*(.*)$") {
            $key = $matches[1]
            $value = $matches[2]
            [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }
}
else {
    Write-Error ".env file not found! Please create one from .env.example"
    exit 1
}

Write-Host "Starting HulamPay Backend..."
Write-Host "Database Configuration Loaded."

# Run Spring Boot
.\mvnw.cmd spring-boot:run
