export type Product = {
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

type ProductCardProps = {
  product: Product
}

function getStockStatus(product: Product) {
  const label = product.stockLabel?.toLowerCase()
  if (label === 'sold out' || product.stock === 0) {
    return { text: 'Agotado', className: 'out-of-stock' }
  }
  if (label === 'last units' || label === 'low stock' || (product.stock !== null && product.stock !== undefined && product.stock <= 10)) {
    return { text: 'Poco Stock', className: 'low-stock' }
  }
  return { text: 'Stock', className: 'in-stock' }
}

function ProductCard({ product }: ProductCardProps) {
  const stockStatus = getStockStatus(product)

  return (
    <article className="product-card">
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
}

export default ProductCard
