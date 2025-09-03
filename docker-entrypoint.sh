#!/bin/bash

set -e

echo "🚀 Starting ShortLink Application..."

# 检查外部配置文件是否存在，设置配置优先级
CONFIG_OPTIONS=""

# 检查 application.yaml
if [ -f "/app/application.yaml" ] && [ ! -d "/app/application.yaml" ]; then
    echo "✅ Using external application.yaml"
    CONFIG_OPTIONS="${CONFIG_OPTIONS}file:/app/application.yaml"
else
    echo "📋 Using default application.yaml"
    # 如果挂载点是目录，删除并复制默认配置
    if [ -d "/app/application.yaml" ]; then
        rmdir /app/application.yaml 2>/dev/null || true
    fi
    cp /app/application-default.yaml /app/application.yaml
    CONFIG_OPTIONS="${CONFIG_OPTIONS}file:/app/application.yaml"
fi

# 检查 shardingsphere-config.yaml  
if [ -f "/app/shardingsphere-config.yaml" ] && [ ! -d "/app/shardingsphere-config.yaml" ]; then
    echo "✅ Using external shardingsphere-config.yaml"
else
    echo "📋 Using default shardingsphere-config.yaml"
    # 如果挂载点是目录，删除并复制默认配置
    if [ -d "/app/shardingsphere-config.yaml" ]; then
        rmdir /app/shardingsphere-config.yaml 2>/dev/null || true
    fi
    cp /app/shardingsphere-default.yaml /app/shardingsphere-config.yaml
fi

# 检查必要的资源文件
if [ ! -f "/app/ip2region.xdb" ]; then
    echo "❌ ip2region.xdb not found!"
    exit 1
fi

# 显示启动信息
echo "📁 Configuration files:"
echo "   - Application config: $([ -f '/app/application.yaml' ] && echo '✅ Found' || echo '❌ Missing')"
echo "   - ShardingSphere config: $([ -f '/app/shardingsphere-config.yaml' ] && echo '✅ Found' || echo '❌ Missing')"
echo "   - IP region database: $([ -f '/app/ip2region.xdb' ] && echo '✅ Found' || echo '❌ Missing')"

# 启动应用
echo "🎯 Starting application with Spring profile: docker"
exec java ${JAVA_OPTS:--Xmx1024m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200} \
    -jar \
    -Dspring.config.location="${CONFIG_OPTIONS}" \
    -Dspring.profiles.active=docker \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Shanghai \
    app.jar