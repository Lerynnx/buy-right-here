import type { ReactNode } from 'react'
import SiteFooter from '../components/SiteFooter'

type StorefrontLayoutProps = {
  children: ReactNode
  showFooter?: boolean
}

function StorefrontLayout({ children, showFooter = true }: StorefrontLayoutProps) {
  return (
    <main className="min-h-screen bg-base-200 px-4 pb-16 sm:px-8">
      {children}
      {showFooter && <SiteFooter />}
    </main>
  )
}

export default StorefrontLayout
