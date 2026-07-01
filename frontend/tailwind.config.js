/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef4ff',
          100: '#dce8ff',
          500: '#3b5fe0',
          600: '#2f4bc4',
          700: '#28409f',
        },
      },
    },
  },
  plugins: [],
};
