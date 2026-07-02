@echo off
echo ========================================
echo 高校共享空间座位预约系统 - 前端启动
echo ========================================
echo.

cd frontend

echo 正在检查Node.js环境...
node -v
if %errorlevel% neq 0 (
    echo [错误] 未检测到Node.js环境，请先安装Node.js
    pause
    exit /b 1
)

echo.
echo 正在检查npm环境...
npm -v
if %errorlevel% neq 0 (
    echo [错误] 未检测到npm环境，请先安装npm
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo.
    echo 首次运行，正在安装依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
)

echo.
echo 正在启动前端开发服务器...
echo 前端应用将在 http://localhost:8081 启动
echo.
call npm run serve

pause