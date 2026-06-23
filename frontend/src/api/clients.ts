import { http } from './http'
import type { Client, ClientRequest } from './types'

export async function listClients(): Promise<Client[]> {
  const { data } = await http.get<Client[]>('/clients')
  return data
}

export async function getClient(id: number): Promise<Client> {
  const { data } = await http.get<Client>(`/clients/${id}`)
  return data
}

export async function createClient(body: ClientRequest): Promise<Client> {
  const { data } = await http.post<Client>('/clients', body)
  return data
}

export async function updateClient(id: number, body: ClientRequest): Promise<Client> {
  const { data } = await http.put<Client>(`/clients/${id}`, body)
  return data
}

export async function deleteClient(id: number): Promise<void> {
  await http.delete(`/clients/${id}`)
}
