import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { deleteClient, listClients } from '../api/clients'

export default function ClientsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['clients'],
    queryFn: listClients,
  })

  const remove = useMutation({
    mutationFn: deleteClient,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  })

  function handleDelete(id: number, name: string) {
    if (confirm(`¿Eliminar al cliente "${name}"?`)) {
      remove.mutate(id)
    }
  }

  if (isLoading) return <p>Cargando clientes…</p>
  if (isError) return <p className="error">No se pudieron cargar los clientes.</p>

  return (
    <section>
      <div className="page-head">
        <h1>Clientes</h1>
        <button className="btn" onClick={() => navigate('/clients/new')}>
          + Nuevo cliente
        </button>
      </div>

      {data && data.length === 0 ? (
        <p>No hay clientes todavía.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Apellido</th>
              <th>Email</th>
              <th>Teléfono</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data?.map((c) => (
              <tr key={c.id}>
                <td>{c.id}</td>
                <td>{c.name}</td>
                <td>{c.lastName}</td>
                <td>{c.email}</td>
                <td>{c.phone}</td>
                <td className="actions">
                  <Link to={`/clients/${c.id}`}>Editar</Link>
                  <button
                    className="btn-danger"
                    onClick={() => handleDelete(c.id, `${c.name} ${c.lastName}`)}
                  >
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
