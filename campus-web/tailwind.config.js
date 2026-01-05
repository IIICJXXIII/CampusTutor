/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // 定义文档要求的主色调
        brand: {
          blue: '#2563EB', // 信任感蓝 (接近 Tailwind blue-600)
          orange: '#F97316', // 活力感橙 (接近 Tailwind orange-500)
          gray: '#F3F4F6',   // 浅灰底色 (接近 Tailwind gray-100)
        }
      },
      fontFamily: {
        sans: ['PingFang SC', 'Microsoft YaHei', 'sans-serif'], // 统一无衬线字体
      }
    },
  },
  plugins: [],
}