import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import ClientsPage from './pages/ClientsPage'
import ClientFormPage from './pages/ClientFormPage'
import PoliciesPage from './pages/PoliciesPage'
import PolicyFormPage from './pages/PolicyFormPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/clients" element={<ClientsPage />} />
          <Route path="/clients/new" element={<ClientFormPage />} />
          <Route path="/clients/:id" element={<ClientFormPage />} />
          <Route path="/policies" element={<PoliciesPage />} />
          <Route path="/policies/new" element={<PolicyFormPage />} />
          <Route path="/policies/:id" element={<PolicyFormPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/clients" replace />} />
    </Routes>
  )
}
