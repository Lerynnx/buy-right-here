import { useState } from 'react'
import productPlaceholder from '../assets/productPlaceholder.webp'

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

export function ProductCardSkeleton() {
  return (
    <article className="card w-full bg-base-100 shadow-sm" aria-hidden="true">
      <figure className="aspect-[1.2] bg-base-200">
        <div className="skeleton size-full rounded-none" />
      </figure>
      <div className="card-body">
        <div className="skeleton h-6 w-3/4" />
        <div className="skeleton h-4 w-full" />
        <div className="skeleton h-4 w-5/6" />
        <div className="card-actions mt-2 items-center justify-between">
          <div className="skeleton h-6 w-20" />
          <div className="skeleton h-6 w-20 rounded-full" />
        </div>
      </div>
    </article>
  )
}

function getStockStatus(product: Product) {
  const label = product.stockLabel?.toLowerCase()
  if (label === 'sold out' || product.stock === 0) {
    return { text: 'Sold out', className: 'badge-error' }
  }
  if (label === 'last units' || label === 'low stock' || (product.stock !== null && product.stock !== undefined && product.stock <= 10)) {
    return { text: 'Low stock', className: 'badge-warning' }
  }
  return { text: 'Stock', className: 'badge-success' }
}

function ProductCard({ product }: ProductCardProps) {
  const stockStatus = getStockStatus(product)
  const imageUrl = product.imageUrls?.[0]
  const [failedImageUrl, setFailedImageUrl] = useState<string | null>(null)
  const imageSource = imageUrl && failedImageUrl !== imageUrl ? imageUrl : productPlaceholder

  return (
    <article className="card w-full bg-base-100 shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      <figure className="aspect-[1.2] bg-base-200">
        <img
          className="size-full object-cover"
          src={imageSource}
          alt={product.name}
          onError={() => setFailedImageUrl(imageUrl ?? '')}
        />
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
