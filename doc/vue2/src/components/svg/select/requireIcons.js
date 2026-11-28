// Vite 使用 import.meta.glob 替代 Webpack 的 require.context
const svgModules = import.meta.glob('../icon/*.svg', { eager: true })

const icons = Object.keys(svgModules).map(path => {
  // 从路径中提取图标名称，例如 '../icon/xxx.svg' -> 'xxx'
  const match = path.match(/\.\/(.*)\.svg$/)
  return match ? match[1] : ''
}).filter(Boolean)

export default icons
