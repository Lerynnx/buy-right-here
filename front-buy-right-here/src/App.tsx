import { useCallback, useEffect, useState } from 'react'
import Keycloak from 'keycloak-js'
import './App.css'

type Product = {
  id: number
  name: string
  description: string
  price: number
  stock: number | null
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

  const loadProducts = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch(`${apiUrl}/products?offset=0&limit=50`, {
        headers: keycloak.token
          ? { Authorization: `Bearer ${keycloak.token}` }
          : undefined,
      })
      if (response.status === 204) {
        setProducts([])
        return
      }
      if (!response.ok) {
        throw new Error(`No se pudieron cargar los productos (${response.status})`)
      }
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
        void loadProducts()
      })
      .catch(() => {
        setError('No se pudo inicializar la sesión de Keycloak')
        setLoading(false)
      })
  }, [loadProducts])

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
          <span className="cart-summary">Tu tienda online</span>
          {authenticated ? (
            <button className="button button-outline" onClick={logout}>Cerrar sesión</button>
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
          {!loading && !error && <span className="product-count">{products.length} productos</span>}
        </div>

        {loading && <p className="status-message">Cargando productos...</p>}
        {error && <p className="status-message status-error" role="alert">{error}</p>}
        {!loading && !error && products.length === 0 && (
          <p className="status-message">No hay productos disponibles ahora mismo.</p>
        )}
        {!loading && !error && products.length > 0 && (
          <div className="product-grid">
            {products.map((product) => (
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
                    <span className={product.stock === 0 ? 'out-of-stock' : 'in-stock'}>
                      {product.stock === 0 ? 'Agotado' : 'Disponible'}
                    </span>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}

export default App
