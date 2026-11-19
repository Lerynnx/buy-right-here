import heroDark from '../assets/heroDark.webp'
import heroLight from '../assets/heroLight.webp'
import type { Theme } from '../theme'

type HeroBannerProps = {
  theme: Theme
}

function HeroBanner({ theme }: HeroBannerProps) {
  return (
    <section className="hero relative isolate mx-auto my-7 min-h-64 w-full max-w-7xl overflow-hidden rounded-box bg-neutral text-neutral-content sm:my-10 sm:min-h-72">
      <img
        className="absolute inset-0 -z-20 size-full object-cover object-center"
        src={theme === 'dark' ? heroDark : heroLight}
        alt=""
      />
      <div className="hero-overlay bg-gradient-to-r from-neutral via-neutral/85 to-neutral/20" />
      <div className="hero-content w-full justify-start px-7 py-10 text-left sm:px-12">
        <div>
          <p className="text-xs font-extrabold uppercase tracking-[0.15em] text-primary">Shop smarter</p>
          <h1 className="my-2 max-w-3xl text-4xl font-medium tracking-tight text-white sm:text-6xl">Products picked for you.</h1>
          <p className="max-w-lg text-base text-neutral-content/80">Discover a simple, curated selection ready to arrive at your door.</p>
        </div>
      </div>
    </section>
  )
}

export default HeroBanner
