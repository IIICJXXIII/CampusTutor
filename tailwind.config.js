/** @type {import('tailwindcss').Config} */
export default {
  // 关键是这一行！必须包含 ./src/**/*.{vue,js...}
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}