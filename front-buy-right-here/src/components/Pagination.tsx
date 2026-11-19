type PaginationProps = {
  page: number
  totalPages: number
  onPrevious: () => void
  onNext: () => void
}

function Pagination({ page, totalPages, onPrevious, onNext }: PaginationProps) {
  return (
    <nav className="mt-9 flex items-center justify-center gap-3 text-sm text-base-content/60 sm:gap-5" aria-label="Paginación de productos">
      <button className="btn btn-outline btn-sm" type="button" onClick={onPrevious} disabled={page === 1}>
        Anterior
      </button>
      <span>Página {page} de {totalPages}</span>
      <button className="btn btn-outline btn-sm" type="button" onClick={onNext} disabled={page === totalPages}>
        Siguiente
      </button>
    </nav>
  )
}

export default Pagination
