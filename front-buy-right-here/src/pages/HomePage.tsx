import { useCallback, useEffect, useState } from 'react'
import Keycloak from 'keycloak-js'
import HeroBanner from '../components/HeroBanner'
import Pagination from '../components/Pagination'
import ProductCard, { type Product } from '../components/ProductCard'
import SiteHeader from '../components/SiteHeader'
import StorefrontLayout from '../layouts/StorefrontLayout'
import '../styles/storefront.css'

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8081',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'buy-right-now',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'frontend',
})

const apiUrl = import.meta.env.VITE_API_URL ?? '/api'
const pageSize = 12

function HomePage() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [authenticated, setAuthenticated] = useState(false)
  const [page, setPage] = useState(1)
  const [totalCount, setTotalCount] = useState(0)

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
  const totalPages = Math.max(1, Math.ceil(totalCount / pageSize))

  const login = () => {
    void keycloak.login({ redirectUri: window.location.href })
  }

  const logout = () => {
    void keycloak.logout({ redirectUri: window.location.origin })
  }

  return (
    <StorefrontLayout>
      <SiteHeader
        authenticated={authenticated}
        username={username}
        onLogin={login}
        onLogout={logout}
      />
      <HeroBanner />

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
            {products.map((product) => <ProductCard key={product.id} product={product} />)}
          </div>
        )}
        {!loading && !error && totalPages > 1 && (
          <Pagination
            page={page}
            totalPages={totalPages}
            onPrevious={() => setPage((current) => Math.max(1, current - 1))}
            onNext={() => setPage((current) => Math.min(totalPages, current + 1))}
          />
        )}
      </section>
    </StorefrontLayout>
  )
}

export default HomePage
