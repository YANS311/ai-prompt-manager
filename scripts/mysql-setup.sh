#!/bin/bash

# MySQL 数据库初始化脚本
# 用途：创建数据库、配置用户权限

echo "========================================="
echo "  AI Prompt Manager - MySQL 设置脚本"
echo "========================================="

# 数据库配置
DB_NAME="promptdb"
DB_USER="root"
DB_CHARSET="utf8mb4"
DB_COLLATION="utf8mb4_unicode_ci"

# 创建数据库
echo "1. 创建数据库: $DB_NAME"
mysql -u$DB_USER -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET $DB_CHARSET COLLATE $DB_COLLATION;"

# 检查数据库是否创建成功
if mysql -u$DB_USER -e "USE $DB_NAME;" 2>/dev/null; then
    echo "✅ 数据库创建成功！"
else
    echo "❌ 数据库创建失败，请检查MySQL是否运行："
    echo "   brew services start mysql"
    exit 1
fi

# 显示数据库信息
echo ""
echo "2. 数据库信息:"
mysql -u$DB_USER -e "SELECT
    SCHEMA_NAME as '数据库名',
    DEFAULT_CHARACTER_SET_NAME as '字符集',
    DEFAULT_COLLATION_NAME as '排序规则'
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = '$DB_NAME';"

echo ""
echo "========================================="
echo "✅ MySQL 配置完成！"
echo "========================================="
echo "连接信息:"
echo "  Host:     localhost:3306"
echo "  Database: $DB_NAME"
echo "  Username: $DB_USER"
echo "  Password: (空密码)"
echo ""
echo "管理命令:"
echo "  启动服务: brew services start mysql"
echo "  停止服务: brew services stop mysql"
echo "  连接数据库: mysql -u$DB_USER $DB_NAME"
echo "  删除数据库: mysql -u$DB_USER -e 'DROP DATABASE $DB_NAME;'"
echo "========================================="
