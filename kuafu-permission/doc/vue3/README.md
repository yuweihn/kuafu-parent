
# 使用node -v检查node是否已安装
建议node版本：v21.7.3

# 安装依赖
yarn --registry=https://registry.npmmirror.com

# 启动服务
yarn dev

# 构建测试环境
yarn build:qa

# 构建生产环境
yarn build:prd

# 前后端分离
vite.config.mjs -> build -> outDir: 'dist'

# 前后端不分离
vite.config.mjs -> build -> outDir: '../src/main/resources/static'
