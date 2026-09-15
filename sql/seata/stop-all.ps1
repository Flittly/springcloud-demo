# ============================================================
#  Seata 示例 —— 一键停止 4 个服务
#
#  用法（在仓库根目录执行）：
#      powershell -ExecutionPolicy Bypass -File sql\seata\stop-all.ps1
#
#  只按端口找进程，不会误伤其他 java 进程。
# ============================================================
$ErrorActionPreference = 'Continue'

$ports = @(9101, 9102, 9103, 9104)

foreach ($p in $ports) {
    $conns = Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue
    if (-not $conns) {
        Write-Host "[空闲] :$p"
        continue
    }
    foreach ($c in ($conns | Select-Object -ExpandProperty OwningProcess -Unique)) {
        try {
            Stop-Process -Id $c -Force -ErrorAction Stop
            Write-Host "[停止] :$p  pid=$c" -ForegroundColor Yellow
        } catch {
            Write-Host "[失败] :$p  pid=$c  $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "`n剩余监听：" -ForegroundColor Cyan
Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object { $_.LocalPort -in $ports } |
    Select-Object LocalPort, OwningProcess | Sort-Object LocalPort -Unique |
    Format-Table -AutoSize | Out-String | Write-Host
