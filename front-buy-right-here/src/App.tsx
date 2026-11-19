import HomePage from './pages/HomePage'
import ProductsPage from './pages/ProductsPage'

function App() {
  return window.location.pathname === '/products' ? <ProductsPage /> : <HomePage />
}

export default App
