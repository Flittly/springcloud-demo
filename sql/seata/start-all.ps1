# ============================================================
#  Seata 示例 —— 一键启动 4 个服务
#
#  用法（在仓库根目录执行）：
#      powershell -ExecutionPolicy Bypass -File sql\seata\start-all.ps1
#
#  日志：sql\seata\logs\<服务名>.log
#  停止：sql\seata\stop-all.ps1
# ============================================================
$ErrorActionPreference = 'Continue'

# 本脚本位于 sql\seata\ 下，往上退两级就是仓库根目录
$root   = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$logDir = Join-Path $PSScriptRoot 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$java = Join-Path $env:JAVA_HOME 'bin\java.exe'
if (-not (Test-Path $java)) { $java = 'java' }

# ------------------------------------------------------------
# 【关键】清掉会污染 server.port 的环境变量
#
# Spring Boot 的 relaxed binding 会把环境变量映射成配置项，而
# ConfigurationPropertyName.adapt(name, '_') 解析时**会丢掉空片段**，
# 所以 SERVER__PORT=62802 实际等价于 server.port=62802。
# 环境变量的优先级高于 application.yml，yml 里写的端口会被顶掉。
#
# 本机 WorkBuddy 宿主进程注入了 SERVER__PORT / SERVER__HOST，
# 表现为四个服务全部绑到 62802。这两行删掉即可。
# ------------------------------------------------------------
Remove-Item Env:\SERVER__PORT -ErrorAction SilentlyContinue
Remove-Item Env:\SERVER__HOST -ErrorAction SilentlyContinue

$svcs = @(
    @{ name = 'seata-order';    port = 9102 },
    @{ name = 'seata-storage';  port = 9103 },
    @{ name = 'seata-account';  port = 9104 },
    @{ name = 'seata-business'; port = 9101 }
)

foreach ($s in $svcs) {
    $jar = Join-Path $root "services\$($s.name)\target\$($s.name)-0.0.1-SNAPSHOT.jar"
    if (-not (Test-Path $jar)) {
        Write-Host "[跳过] $($s.name) 未构建，请先执行 mvn -pl services/$($s.name) -am install" -ForegroundColor Yellow
        continue
    }

    $log = Join-Path $logDir "$($s.name).log"
    if (Test-Path $log) { Remove-Item $log -Force }

    # 用 ProcessStartInfo 而不是 Start-Process：
    # 本机环境块里存在 Path/PATH、HTTP_PROXY/http_proxy 这类大小写重复的键，
    # PowerShell 5.1 的 Start-Process 在 -RedirectStandardOutput 下会抛
    # "已添加项。字典中的关键字 ..." 而静默起不来。这里绕开它。
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName        = $java
    # 显式带 --server.port 只是双保险；application.yml 里端口已经写好了
    $psi.Arguments       = "-jar `"$jar`" --server.port=$($s.port) --logging.file.name=`"$log`""
    $psi.WorkingDirectory = $root
    $psi.UseShellExecute  = $true
    $psi.WindowStyle      = 'Hidden'
    try {
        [System.Diagnostics.Process]::Start($psi) | Out-Null
        Write-Host "[启动] $($s.name)  ->  127.0.0.1:$($s.port)" -ForegroundColor Green
    } catch {
        Write-Host "[失败] $($s.name): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n等待端口就绪 ..." -ForegroundColor Cyan
$deadline = (Get-Date).AddSeconds(120)
$ok = @{}
while ((Get-Date) -lt $deadline) {
    foreach ($s in $svcs) {
        if (-not $ok.ContainsKey($s.port)) {
            if (Get-NetTCPConnection -State Listen -LocalPort $s.port -ErrorAction SilentlyContinue) {
                $ok[$s.port] = $true
            }
        }
    }
    if ($ok.Count -eq 4) { break }
    Start-Sleep -Seconds 3
}

Write-Host "`n已就绪 $($ok.Count)/4：" -ForegroundColor Cyan
foreach ($s in $svcs) {
    $mark = if ($ok.ContainsKey($s.port)) { 'OK  ' } else { 'DOWN' }
    Write-Host "  $mark $($s.name)  :$($s.port)"
}
if ($ok.Count -eq 4) {
    Write-Host "`n可以开始验证了：" -ForegroundColor Cyan
    Write-Host '  curl "http://127.0.0.1:9101/business/purchase?userId=1&productId=1&count=1&money=100"'
    Write-Host '  curl "http://127.0.0.1:9101/business/purchase?userId=1&productId=1&count=1&money=10000"'
}
