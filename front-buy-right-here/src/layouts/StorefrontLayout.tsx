import type { ReactNode } from 'react'
import SiteFooter from '../components/SiteFooter'

type StorefrontLayoutProps = {
  children: ReactNode
}

function StorefrontLayout({ children }: StorefrontLayoutProps) {
  return (
    <main className="min-h-screen bg-base-200 px-4 pb-16 sm:px-8">
      {children}
      <SiteFooter />
    </main>
  )
}

export default StorefrontLayout
