// Mirrors the backend DTOs (com.pc.pc.dto.*).

export type Status = 'ACTIVE' | 'INACTIVE' | 'PENDING' | 'CANCELLED'

export const STATUSES: Status[] = ['ACTIVE', 'INACTIVE', 'PENDING', 'CANCELLED']

export interface Client {
  id: number
  name: string
  lastName: string
  email: string
  phone: string
}

export interface ClientRequest {
  name: string
  lastName: string
  email: string
  phone: string
}

export interface Policy {
  id: number
  vehicleMake: string
  vehicleModel: string
  vehicleYear: number
  vin: string
  licensePlate: string
  status: Status
  policyNumber: string
  premium: number
  startDate: string
  endDate: string
  clientId: number
  clientName: string
}

export interface PolicyRequest {
  vehicleMake: string
  vehicleModel: string
  vehicleYear: number
  vin: string
  licensePlate: string
  status: Status
  policyNumber: string
  premium: number
  startDate: string
  endDate: string
  clientId: number
}
