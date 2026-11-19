type PaginationProps = {
  page: number
  totalPages: number
  onPrevious: () => void
  onNext: () => void
}

function Pagination({ page, totalPages, onPrevious, onNext }: PaginationProps) {
  return (
    <nav className="pagination" aria-label="Paginación de productos">
      <button className="pagination-button" type="button" onClick={onPrevious} disabled={page === 1}>
        Anterior
      </button>
      <span>Página {page} de {totalPages}</span>
      <button className="pagination-button" type="button" onClick={onNext} disabled={page === totalPages}>
        Siguiente
      </button>
    </nav>
  )
}

export default Pagination
