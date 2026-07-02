@echo off
echo ========================================
echo 高校共享空间座位预约系统 - 后端启动
echo ========================================
echo.

echo 正在检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK 1.8
    pause
    exit /b 1
)

echo.
echo 正在检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo [错误] 未检测到Maven环境，请先安装Maven
    pause
    exit /b 1
)

echo.
echo 正在编译项目...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [错误] 项目编译失败
    pause
    exit /b 1
)

echo.
echo 正在启动后端服务...
echo 后端服务将在 http://localhost:8080 启动
echo.
call mvn spring-boot:run

pause