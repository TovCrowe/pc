import { useEffect, useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import { createPolicy, getPolicy, updatePolicy } from '../api/policies'
import { listClients } from '../api/clients'
import { STATUSES, type PolicyRequest } from '../api/types'

const EMPTY: PolicyRequest = {
  vehicleMake: '',
  vehicleModel: '',
  vehicleYear: new Date().getFullYear(),
  vin: '',
  licensePlate: '',
  status: 'ACTIVE',
  policyNumber: '',
  premium: 0,
  startDate: '',
  endDate: '',
  clientId: 0,
}

/** Normalises a backend date (epoch millis or ISO string) to "YYYY-MM-DD". */
function toDateInput(value: string | number): string {
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '' : d.toISOString().slice(0, 10)
}

export default function PolicyFormPage() {
  const { id } = useParams()
  const isEdit = id != null
  const policyId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [form, setForm] = useState<PolicyRequest>(EMPTY)
  const [error, setError] = useState<string | null>(null)

  const clients = useQuery({ queryKey: ['clients'], queryFn: listClients })

  const existing = useQuery({
    queryKey: ['policies', policyId],
    queryFn: () => getPolicy(policyId),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing.data) {
      const p = existing.data
      setForm({
        vehicleMake: p.vehicleMake,
        vehicleModel: p.vehicleModel,
        vehicleYear: p.vehicleYear,
        vin: p.vin,
        licensePlate: p.licensePlate,
        status: p.status,
        policyNumber: p.policyNumber,
        premium: p.premium,
        startDate: toDateInput(p.startDate),
        endDate: toDateInput(p.endDate),
        clientId: p.clientId,
      })
    }
  }, [existing.data])

  const save = useMutation({
    mutationFn: (body: PolicyRequest) =>
      isEdit ? updatePolicy(policyId, body) : createPolicy(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['policies'] })
      navigate('/policies')
    },
    onError: () => setError('No se pudo guardar. Revisa los campos.'),
  })

  function update<K extends keyof PolicyRequest>(key: K, value: PolicyRequest[K]) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (!form.clientId) {
      setError('Selecciona un cliente.')
      return
    }
    save.mutate(form)
  }

  return (
    <section>
      <h1>{isEdit ? 'Editar póliza' : 'Nueva póliza'}</h1>
      <form className="card form form-grid" onSubmit={handleSubmit}>
        <label>
          Nº Póliza
          <input
            value={form.policyNumber}
            onChange={(e) => update('policyNumber', e.target.value)}
            required
          />
        </label>
        <label>
          Cliente
          <select
            value={form.clientId || ''}
            onChange={(e) => update('clientId', Number(e.target.value))}
            required
          >
            <option value="" disabled>
              — Selecciona —
            </option>
            {clients.data?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} {c.lastName}
              </option>
            ))}
          </select>
        </label>

        <label>
          Marca
          <input
            value={form.vehicleMake}
            onChange={(e) => update('vehicleMake', e.target.value)}
            required
          />
        </label>
        <label>
          Modelo
          <input
            value={form.vehicleModel}
            onChange={(e) => update('vehicleModel', e.target.value)}
            required
          />
        </label>
        <label>
          Año
          <input
            type="number"
            value={form.vehicleYear}
            onChange={(e) => update('vehicleYear', Number(e.target.value))}
            required
          />
        </label>
        <label>
          VIN
          <input value={form.vin} onChange={(e) => update('vin', e.target.value)} required />
        </label>
        <label>
          Placa
          <input
            value={form.licensePlate}
            onChange={(e) => update('licensePlate', e.target.value)}
            required
          />
        </label>
        <label>
          Estado
          <select
            value={form.status}
            onChange={(e) => update('status', e.target.value as PolicyRequest['status'])}
          >
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </label>
        <label>
          Prima
          <input
            type="number"
            step="0.01"
            value={form.premium}
            onChange={(e) => update('premium', Number(e.target.value))}
            required
          />
        </label>
        <label>
          Inicio
          <input
            type="date"
            value={form.startDate}
            onChange={(e) => update('startDate', e.target.value)}
            required
          />
        </label>
        <label>
          Fin
          <input
            type="date"
            value={form.endDate}
            onChange={(e) => update('endDate', e.target.value)}
            required
          />
        </label>

        {error && <p className="error span-2">{error}</p>}
        <div className="form-actions span-2">
          <button className="btn" type="submit" disabled={save.isPending}>
            {save.isPending ? 'Guardando…' : 'Guardar'}
          </button>
          <button type="button" className="btn-link" onClick={() => navigate('/policies')}>
            Cancelar
          </button>
        </div>
      </form>
    </section>
  )
}
