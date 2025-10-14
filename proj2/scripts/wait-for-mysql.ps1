param(
  [string]$Host = "127.0.0.1",
  [int]$Port = 3307,
  [int]$TimeoutSeconds = 60
)

Write-Output "Waiting for MySQL at $Host:${Port} (timeout: ${TimeoutSeconds} s)..."
$sw = [System.Diagnostics.Stopwatch]::StartNew()

function Test-TcpPortReachable {
  param([string]$Host, [int]$Port)
  try {
    if (Get-Command Test-NetConnection -ErrorAction SilentlyContinue) {
      return (Test-NetConnection -ComputerName $Host -Port ${Port} -WarningAction SilentlyContinue).TcpTestSucceeded
    } else {
      $client = New-Object System.Net.Sockets.TcpClient
      $iar = $client.BeginConnect($Host, $Port, $null, $null)
      $success = $iar.AsyncWaitHandle.WaitOne(1000, $false)
      if ($success -and $client.Connected) { $client.Close(); return $true }
      $client.Close()
      return $false
    }
  } catch { return $false }
}

while ($sw.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
  if (Test-TcpPortReachable -Host ${Hostname} -Port ${Port}) {
    Write-Output "MySQL is reachable at $Host:$Port."
    exit 0
  }
  Start-Sleep -Seconds 2
}

Write-Error "Timed out after $TimeoutSeconds s waiting for MySQL at $Host:$Port."
exit 1
