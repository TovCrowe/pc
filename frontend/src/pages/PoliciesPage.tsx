import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { deletePolicy, listPolicies } from '../api/policies'

function formatDate(value: string): string {
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleDateString()
}

export default function PoliciesPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['policies'],
    queryFn: listPolicies,
  })

  const remove = useMutation({
    mutationFn: deletePolicy,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['policies'] }),
  })

  function handleDelete(id: number, label: string) {
    if (confirm(`¿Eliminar la póliza "${label}"?`)) {
      remove.mutate(id)
    }
  }

  if (isLoading) return <p>Cargando pólizas…</p>
  if (isError) return <p className="error">No se pudieron cargar las pólizas.</p>

  return (
    <section>
      <div className="page-head">
        <h1>Pólizas</h1>
        <button className="btn" onClick={() => navigate('/policies/new')}>
          + Nueva póliza
        </button>
      </div>

      {data && data.length === 0 ? (
        <p>No hay pólizas todavía.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Nº Póliza</th>
              <th>Vehículo</th>
              <th>Cliente</th>
              <th>Estado</th>
              <th>Prima</th>
              <th>Vigencia</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data?.map((p) => (
              <tr key={p.id}>
                <td>{p.policyNumber}</td>
                <td>
                  {p.vehicleMake} {p.vehicleModel} ({p.vehicleYear})
                </td>
                <td>{p.clientName}</td>
                <td>
                  <span className={`badge badge-${p.status.toLowerCase()}`}>{p.status}</span>
                </td>
                <td>${p.premium.toFixed(2)}</td>
                <td>
                  {formatDate(p.startDate)} → {formatDate(p.endDate)}
                </td>
                <td className="actions">
                  <Link to={`/policies/${p.id}`}>Editar</Link>
                  <button className="btn-danger" onClick={() => handleDelete(p.id, p.policyNumber)}>
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
