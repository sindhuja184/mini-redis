param(
    [Parameter(Mandatory=$true, ValueFromRemainingArguments=$true)]
    [string[]]$Command
)

try {
    $client = New-Object System.Net.Sockets.TcpClient("127.0.0.1", 6379)
    $stream = $client.GetStream()
} catch {
    Write-Error "Could not connect to Redis server at 127.0.0.1:6379. Make sure the server is running."
    return
}

# Construct the RESP array payload
$payload = "*$($Command.Length)`r`n"
foreach ($arg in $Command) {
    $payload += "`$$($arg.Length)`r`n$arg`r`n"
}

$bytes = [System.Text.Encoding]::UTF8.GetBytes($payload)
$stream.Write($bytes, 0, $bytes.Length)
$stream.Flush()

# Read the RESP response
$reader = New-Object System.IO.StreamReader($stream)
$line = $reader.ReadLine()

if ($line -eq $null) {
    Write-Host "No response from server"
} elseif ($line.StartsWith("+")) {
    Write-Host "Response: $($line.Substring(1))" -ForegroundColor Green
} elseif ($line.StartsWith("-")) {
    Write-Host "Error: $($line.Substring(1))" -ForegroundColor Red
} elseif ($line.StartsWith(":")) {
    Write-Host "Response: $($line.Substring(1))" -ForegroundColor Cyan
} elseif ($line.StartsWith("$")) {
    $length = [int]$line.Substring(1)
    if ($length -eq -1) {
        Write-Host "Response: (nil)" -ForegroundColor Yellow
    } else {
        $value = $reader.ReadLine()
        Write-Host "Response: `"$value`"" -ForegroundColor Green
    }
} else {
    Write-Host "Raw Response: $line"
}

$client.Close()
