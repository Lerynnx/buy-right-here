export type Theme = 'light' | 'dark'

const themeStorageKey = 'buy-right-here-theme'

export function getInitialTheme(): Theme {
  const savedTheme = window.localStorage.getItem(themeStorageKey)
  if (savedTheme === 'light' || savedTheme === 'dark') {
    return savedTheme
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function saveTheme(theme: Theme) {
  window.localStorage.setItem(themeStorageKey, theme)
}
