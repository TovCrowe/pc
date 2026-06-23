import { useEffect, useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import { createClient, getClient, updateClient } from '../api/clients'
import type { ClientRequest } from '../api/types'

const EMPTY: ClientRequest = { name: '', lastName: '', email: '', phone: '' }

export default function ClientFormPage() {
  const { id } = useParams()
  const isEdit = id != null
  const clientId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [form, setForm] = useState<ClientRequest>(EMPTY)
  const [error, setError] = useState<string | null>(null)

  const existing = useQuery({
    queryKey: ['clients', clientId],
    queryFn: () => getClient(clientId),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing.data) {
      const { name, lastName, email, phone } = existing.data
      setForm({ name, lastName, email, phone })
    }
  }, [existing.data])

  const save = useMutation({
    mutationFn: (body: ClientRequest) =>
      isEdit ? updateClient(clientId, body) : createClient(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] })
      navigate('/clients')
    },
    onError: () => setError('No se pudo guardar. Revisa los campos.'),
  })

  function update<K extends keyof ClientRequest>(key: K, value: ClientRequest[K]) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    save.mutate(form)
  }

  return (
    <section>
      <h1>{isEdit ? 'Editar cliente' : 'Nuevo cliente'}</h1>
      <form className="card form" onSubmit={handleSubmit}>
        <label>
          Nombre
          <input value={form.name} onChange={(e) => update('name', e.target.value)} required />
        </label>
        <label>
          Apellido
          <input
            value={form.lastName}
            onChange={(e) => update('lastName', e.target.value)}
            required
          />
        </label>
        <label>
          Email
          <input
            type="email"
            value={form.email}
            onChange={(e) => update('email', e.target.value)}
            required
          />
        </label>
        <label>
          Teléfono
          <input value={form.phone} onChange={(e) => update('phone', e.target.value)} required />
        </label>
        {error && <p className="error">{error}</p>}
        <div className="form-actions">
          <button className="btn" type="submit" disabled={save.isPending}>
            {save.isPending ? 'Guardando…' : 'Guardar'}
          </button>
          <button type="button" className="btn-link" onClick={() => navigate('/clients')}>
            Cancelar
          </button>
        </div>
      </form>
    </section>
  )
}
