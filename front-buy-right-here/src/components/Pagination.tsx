type PaginationProps = {
  page: number
  totalPages: number
  onPrevious: () => void
  onNext: () => void
}

function Pagination({ page, totalPages, onPrevious, onNext }: PaginationProps) {
  return (
    <nav className="mt-9 flex items-center justify-center gap-3 text-sm text-base-content/60 sm:gap-5" aria-label="Product pagination">
      <button className="btn btn-outline btn-sm" type="button" onClick={onPrevious} disabled={page === 1}>
        Previous
      </button>
      <span>Page {page} of {totalPages}</span>
      <button className="btn btn-outline btn-sm" type="button" onClick={onNext} disabled={page === totalPages}>
        Next
      </button>
    </nav>
  )
}

export default Pagination
