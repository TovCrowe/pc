import { http } from './http'
import type { Policy, PolicyRequest } from './types'

export async function listPolicies(): Promise<Policy[]> {
  const { data } = await http.get<Policy[]>('/policies')
  return data
}

export async function getPolicy(id: number): Promise<Policy> {
  const { data } = await http.get<Policy>(`/policies/${id}`)
  return data
}

export async function createPolicy(body: PolicyRequest): Promise<Policy> {
  const { data } = await http.post<Policy>('/policies', body)
  return data
}

export async function updatePolicy(id: number, body: PolicyRequest): Promise<Policy> {
  const { data } = await http.put<Policy>(`/policies/${id}`, body)
  return data
}

export async function deletePolicy(id: number): Promise<void> {
  await http.delete(`/policies/${id}`)
}
