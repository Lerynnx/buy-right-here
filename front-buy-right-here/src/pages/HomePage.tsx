import { useEffect, useState } from 'react'
import Keycloak from 'keycloak-js'
import HeroBanner from '../components/HeroBanner'
import SiteHeader from '../components/SiteHeader'
import StorefrontLayout from '../layouts/StorefrontLayout'
import { getInitialTheme, saveTheme, type Theme } from '../theme'

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8081',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'buy-right-now',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'frontend',
})

function HomePage() {
  const [theme, setTheme] = useState<Theme>(getInitialTheme)
  const [authenticated, setAuthenticated] = useState(false)

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    saveTheme(theme)
  }, [theme])

  useEffect(() => {
    void keycloak
      .init({ onLoad: 'check-sso', pkceMethod: 'S256', checkLoginIframe: false })
      .then((isAuthenticated) => {
        setAuthenticated(isAuthenticated)
      })
      .catch(() => {
        setAuthenticated(false)
      })
  }, [])

  const username =
    keycloak.tokenParsed?.preferred_username ??
    keycloak.tokenParsed?.name ??
    keycloak.tokenParsed?.email
  const login = () => {
    void keycloak.login({ redirectUri: window.location.href })
  }

  const logout = () => {
    void keycloak.logout({ redirectUri: window.location.origin })
  }

  return (
    <StorefrontLayout showFooter={false}>
      <SiteHeader
        authenticated={authenticated}
        username={username}
        onLogin={login}
        onLogout={logout}
        theme={theme}
        onThemeChange={setTheme}
      />
      <HeroBanner theme={theme} />
    </StorefrontLayout>
  )
}

export default HomePage
