type SiteHeaderProps = {
  authenticated: boolean
  username?: string
  onLogin: () => void
  onLogout: () => void
}

function SiteHeader({ authenticated, username, onLogin, onLogout }: SiteHeaderProps) {
  return (
    <header className="site-header">
      <a className="brand" href="/">buy right here</a>
      <div className="header-actions">
        {authenticated ? (
          <>
            <div className="user-summary">
              <span className="user-avatar">{String(username ?? 'U').slice(0, 1).toUpperCase()}</span>
              <span>{username}</span>
            </div>
            <button className="button button-outline" onClick={onLogout}>Cerrar sesión</button>
          </>
        ) : (
          <button className="button button-primary" onClick={onLogin}>Iniciar sesión</button>
        )}
      </div>
    </header>
  )
}

export default SiteHeader
