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
    return { text: 'Agotado', className: 'badge-error' }
  }
  if (label === 'last units' || label === 'low stock' || (product.stock !== null && product.stock !== undefined && product.stock <= 10)) {
    return { text: 'Poco Stock', className: 'badge-warning' }
  }
  return { text: 'Stock', className: 'badge-success' }
}

function ProductCard({ product }: ProductCardProps) {
  const stockStatus = getStockStatus(product)

  return (
    <article className="card w-full bg-base-100 shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      <figure className="aspect-[1.2] bg-base-200">
        {product.imageUrls?.[0] ? (
          <img className="size-full object-cover" src={product.imageUrls[0]} alt={product.name} />
        ) : (
          <span className="text-6xl font-extrabold text-primary">{product.name.slice(0, 1).toUpperCase()}</span>
        )}
      </figure>
      <div className="card-body">
        <h2 className="card-title">
          {product.name}
          {product.brand && <div className="badge badge-outline">{product.brand}</div>}
        </h2>
        <p className="min-h-11 text-sm text-base-content/60">{product.description}</p>
        <div className="card-actions mt-2 items-center justify-between">
          <strong className="text-lg text-base-content">{product.price.toFixed(2)} €</strong>
          <span className={`badge ${stockStatus.className}`}>
            {stockStatus.text}
          </span>
        </div>
      </div>
    </article>
  )
}

export default ProductCard
