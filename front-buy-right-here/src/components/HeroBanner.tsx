import heroDark from '../assets/heroDark.webp'
import heroLight from '../assets/heroLight.webp'
import type { Theme } from '../theme'

type HeroBannerProps = {
  theme: Theme
}

function HeroBanner({ theme }: HeroBannerProps) {
  return (
    <section className="hero relative isolate mx-auto my-5 min-h-[calc(100vh-6.5rem)] w-full max-w-7xl overflow-hidden rounded-box bg-neutral text-neutral-content sm:my-7">
      <img
        className="absolute inset-0 -z-20 size-full object-cover object-center"
        src={theme === 'dark' ? heroDark : heroLight}
        alt=""
      />
      <div className="hero-overlay bg-gradient-to-r from-neutral/95 via-neutral/55 to-transparent" />
      <div className="hero-content w-full justify-start px-7 py-10 text-left sm:px-16">
        <div className="max-w-xl">
          <p className="text-xs font-extrabold uppercase tracking-[0.25em] text-primary">Buy better. Live better.</p>
          <h1 className="my-4 text-5xl font-medium leading-[0.95] tracking-tight text-white sm:text-7xl">Everything you want, Buy Right Here.</h1>
          <p className="max-w-lg text-lg text-neutral-content/80">Thoughtfully selected essentials, made easy to discover and even easier to love.</p>
          <a className="btn btn-primary mt-8 px-8" href="/products">Explore products</a>
        </div>
      </div>
    </section>
  )
}

export default HeroBanner
