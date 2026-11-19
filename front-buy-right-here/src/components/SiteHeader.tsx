type SiteHeaderProps = {
  authenticated: boolean
  username?: string
  onLogin: () => void
  onLogout: () => void
}

function SiteHeader({ authenticated, username, onLogin, onLogout }: SiteHeaderProps) {
  return (
    <header className="mx-auto flex w-full max-w-7xl items-center justify-between border-b border-base-300 py-6">
      <a className="text-xl font-extrabold tracking-tight text-base-content" href="/">buy right here</a>
      <div className="flex items-center gap-3">
        {authenticated ? (
          <>
            <div className="flex items-center gap-2 text-sm font-bold text-base-content">
              <div className="avatar">
                <div className="mask mask-squircle w-9">
                  <img
                    alt={`Avatar de ${username ?? 'usuario'}`}
                    src="https://img.daisyui.com/images/profile/demo/distracted1@192.webp"
                  />
                </div>
              </div>
              <span className="hidden sm:inline">{username}</span>
            </div>
            <button className="btn btn-outline btn-sm" onClick={onLogout}>Cerrar sesión</button>
          </>
        ) : (
          <button className="btn btn-primary btn-sm" onClick={onLogin}>Iniciar sesión</button>
        )}
      </div>
    </header>
  )
}

export default SiteHeader
