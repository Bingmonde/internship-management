/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
	"./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // "prose-neutral" : "#f9f9f9",
        // "success" : "#4CAF50",
        // "success-hover" : "#45a049",
        // "red" : "#FF0000",
        // "selected" : "#DB7093",
        "prose-neutral" : "#f9f4f5",
        "prose-alternate" : "#FAF0CA",
        "success" : "#f4d35e",
        "success-hover" : "#eebe10",
        "red" : "#f95738",
        "red-hover": "#f03e1f",
        "selected" : "#70587c",
        "unselected" : "#c5baaf",
        "darkpurple": "#502f4c",
        "darkpurple-option" : "#70587C",
        "lightpurple": "#c8b8db",
        "light-green": "#5a8a0f",
        "light-green-hover": "#496f0b",
        "greenish" : "#1E555C",
        "greenish-hover" : "#19474d",
        "table-odd" : "#fafaf5",
        "table-even" : "#f8f5fa",
        "card" : "#ebe4f0",
      },
      animation: {
        zoomIn: 'zoomIn 1s ease-in-out infinite alternate',
      },
      keyframes: {
        zoomIn: {
          '0%, 100%': { transform: 'scale(1.1)'},
          '50%': { transform: 'scale(1)'},
        }
      }
    },
  },
  plugins: [],
}

