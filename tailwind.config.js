/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['Manrope', 'system-ui', 'sans-serif'],
        sans: ['Hanken Grotesk', 'system-ui', 'sans-serif'],
      },
      colors: {
        slateInk: '#0F172A',
        academicBlue: '#2563EB',
        emeraldKey: '#059669',
        emeraldVibrant: '#10B981',
        canvasBg: '#F8FAFC',
        canvasTint: '#F8F9FF',
      },
    },
  },
  plugins: [],
};
