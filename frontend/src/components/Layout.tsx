import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { username, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app">
      <header className="topbar">
        <span className="brand">🚗 Auto Policy Management</span>
        <nav className="nav">
          <NavLink to="/clients">Clientes</NavLink>
          <NavLink to="/policies">Pólizas</NavLink>
        </nav>
        <div className="user">
          <span>{username}</span>
          <button className="btn-link" onClick={handleLogout}>
            Salir
          </button>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
