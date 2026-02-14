$baseUrl = "http://localhost:8080/api/auth"
$email = "test.android." + (Get-Date -Format "HHmmss") + "@example.com"
$password = "password123"

Write-Host "1. Registering User: $email"
$registerBody = @{
    firstName = "Test"
    lastName = "User"
    email = $email
    password = $password
    studentIdNumber = "SID" + (Get-Date -Format "HHmmss")
    phoneNumber = "1234567890"
    address = "123 Test St"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "Registration Success!"
    # Write-Host $regResponse
} catch {
    Write-Host "Registration Failed: $_"
    exit 1
}

Write-Host "`n2. Logging in..."
$loginBody = @{
    email = $email
    password = $password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/login" -Method Post -Body $loginBody -ContentType "application/json"
    Write-Host "Login Success!"
    
    if ($loginResponse.token) {
        Write-Host "JWT Token Received (First 20 chars): $($loginResponse.token.Substring(0, 20))..."
        Write-Host "Backend JWT Implementation works!"
    } else {
        Write-Host "WARNING: No token received in response. Backend might not be updated."
        Write-Host "Response Keys: $($loginResponse.PSObject.Properties.Name -join ', ')"
    }
} catch {
    Write-Host "Login Failed: $_"
    exit 1
}
