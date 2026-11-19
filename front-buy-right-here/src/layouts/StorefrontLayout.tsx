import type { ReactNode } from 'react'

type StorefrontLayoutProps = {
  children: ReactNode
}

function StorefrontLayout({ children }: StorefrontLayoutProps) {
  return <main className="storefront">{children}</main>
}

export default StorefrontLayout
