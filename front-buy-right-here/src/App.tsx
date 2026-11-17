import { useCallback, useEffect, useState } from 'react'
import Keycloak from 'keycloak-js'
import './App.css'

type Product = {
  id: number
  name: string
  description: string
  price: number
  stock: number | null
  stockLabel?: string
  imageUrls?: string[]
  brand?: string
  rating?: number
}

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8081',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'buy-right-now',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'frontend',
})

const apiUrl = import.meta.env.VITE_API_URL ?? '/api'

function App() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [authenticated, setAuthenticated] = useState(false)
  const [page, setPage] = useState(1)
  const [totalCount, setTotalCount] = useState(0)
  const pageSize = 12

  const loadProducts = useCallback(async (requestedPage: number) => {
    setLoading(true)
    setError(null)
    try {
      const offset = (requestedPage - 1) * pageSize
      const response = await fetch(`${apiUrl}/products?offset=${offset}&limit=${pageSize}`, {
        headers: keycloak.token
          ? { Authorization: `Bearer ${keycloak.token}` }
          : undefined,
      })
      if (response.status === 204) {
        setProducts([])
        setTotalCount(0)
        return
      }
      if (!response.ok) {
        throw new Error(`No se pudieron cargar los productos (${response.status})`)
      }
      const headerCount = response.headers.get('X-Total-Count')
      setTotalCount(headerCount ? Number(headerCount) : 0)
      setProducts((await response.json()) as Product[])
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Error desconocido')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void keycloak
      .init({ onLoad: 'check-sso', pkceMethod: 'S256', checkLoginIframe: false })
      .then((isAuthenticated) => {
        setAuthenticated(isAuthenticated)
        void loadProducts(1)
      })
      .catch(() => {
        setError('No se pudo inicializar la sesión de Keycloak')
        setLoading(false)
      })
  }, [loadProducts])

  useEffect(() => {
    if (page > 1) {
      void loadProducts(page)
    }
  }, [page, loadProducts])

  const username =
    keycloak.tokenParsed?.preferred_username ??
    keycloak.tokenParsed?.name ??
    keycloak.tokenParsed?.email

  const getStockStatus = (product: Product) => {
    const label = product.stockLabel?.toLowerCase()
    if (label === 'sold out' || product.stock === 0) {
      return { text: 'Agotado', className: 'out-of-stock' }
    }
    if (label === 'last units' || label === 'low stock' || (product.stock !== null && product.stock !== undefined && product.stock <= 10)) {
      return { text: 'Poco Stock', className: 'low-stock' }
    }
    return { text: 'Stock', className: 'in-stock' }
  }

  const totalPages = Math.max(1, Math.ceil(totalCount / pageSize))
  const login = () => {
    void keycloak.login({ redirectUri: window.location.href })
  }

  const logout = () => {
    void keycloak.logout({ redirectUri: window.location.origin })
  }

  return (
    <main className="storefront">
      <header className="site-header">
        <a className="brand" href="/">buy right here</a>
        <div className="header-actions">
          {authenticated ? (
            <>
              <div className="user-summary">
                <span className="user-avatar">{String(username ?? 'U').slice(0, 1).toUpperCase()}</span>
                <span>{username}</span>
              </div>
              <button className="button button-outline" onClick={logout}>Cerrar sesión</button>
            </>
          ) : (
            <button className="button button-primary" onClick={login}>Iniciar sesión</button>
          )}
        </div>
      </header>

      <section className="hero-banner">
        <div>
          <p className="eyebrow">Compra mejor</p>
          <h1>Productos elegidos para ti.</h1>
          <p className="hero-copy">Descubre una selección sencilla, clara y lista para llegar a tu casa.</p>
        </div>
      </section>

      <section className="catalog" aria-labelledby="catalog-title">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Catálogo</p>
            <h2 id="catalog-title">Nuestros productos</h2>
          </div>
          {!loading && !error && <span className="product-count">{totalCount} productos</span>}
        </div>

        {loading && <p className="status-message">Cargando productos...</p>}
        {error && <p className="status-message status-error" role="alert">{error}</p>}
        {!loading && !error && products.length === 0 && (
          <p className="status-message">No hay productos disponibles ahora mismo.</p>
        )}
        {!loading && !error && products.length > 0 && (
          <div className="product-grid">
            {products.map((product) => {
              const stockStatus = getStockStatus(product)
              return (
                <article className="product-card" key={product.id}>
                  <div className="product-image">
                    {product.imageUrls?.[0] ? (
                      <img src={product.imageUrls[0]} alt={product.name} />
                    ) : (
                      <span>{product.name.slice(0, 1).toUpperCase()}</span>
                    )}
                  </div>
                  <div className="product-info">
                    {product.brand && <p className="product-brand">{product.brand}</p>}
                    <h3>{product.name}</h3>
                    <p className="product-description">{product.description}</p>
                    <div className="product-footer">
                      <strong>{product.price.toFixed(2)} €</strong>
                      <span className={`stock-badge ${stockStatus.className}`}>
                        {stockStatus.text}
                      </span>
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        )}
        {!loading && !error && totalPages > 1 && (
          <nav className="pagination" aria-label="Paginación de productos">
            <button
              className="pagination-button"
              type="button"
              onClick={() => setPage((current) => Math.max(1, current - 1))}
              disabled={page === 1}
            >
              Anterior
            </button>
            <span>Página {page} de {totalPages}</span>
            <button
              className="pagination-button"
              type="button"
              onClick={() => setPage((current) => Math.min(totalPages, current + 1))}
              disabled={page === totalPages}
            >
              Siguiente
            </button>
          </nav>
        )}
      </section>
    </main>
  )
}

export default App
