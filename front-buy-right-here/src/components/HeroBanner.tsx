import heroDark from '../assets/heroDark.webp'

function HeroBanner() {
  return (
    <section className="relative isolate mx-auto my-7 flex min-h-64 w-full max-w-7xl items-center overflow-hidden rounded-box bg-neutral px-7 py-10 text-neutral-content sm:my-10 sm:min-h-72 sm:px-12">
      <img className="absolute inset-0 -z-20 size-full object-cover object-center" src={heroDark} alt="" />
      <div className="absolute inset-0 -z-10 bg-gradient-to-r from-neutral via-neutral/85 to-neutral/20" />
      <div>
        <p className="text-xs font-extrabold uppercase tracking-[0.15em] text-primary">Compra mejor</p>
        <h1 className="my-2 max-w-3xl text-4xl font-medium tracking-tight text-white sm:text-6xl">Productos elegidos para ti.</h1>
        <p className="max-w-lg text-base text-neutral-content/80">Descubre una selección sencilla, clara y lista para llegar a tu casa.</p>
      </div>
    </section>
  )
}

export default HeroBanner
