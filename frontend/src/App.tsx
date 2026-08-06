import { useState, useEffect, type SubmitEvent } from 'react'
import './App.css'

type CsrfToken = {
  headerName: string
  parameterName: string
  token: string
}

type UserResponse = {
  id: number
  email: string
  displayName: string
}

type CompanyResponse = {
  id: number
  name: string
  role: 'OWNER' | 'MEMBER'
  createdAt: string
}

type ApiError = {
  code?: string
  message?: string
}

function App() {
  const [email, setEmail] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const [user, setUser] = useState<UserResponse | null>(null)
  const [companies, setCompanies] = useState<CompanyResponse[]>([])
  const [selectedCompany, setSelectedCompany] = useState<CompanyResponse | null>(null)
  const [companyName, setCompanyName] = useState('')
  const [loading, setLoading] = useState(true)
  const [loadingCompanies, setLoadingCompanies] = useState(false)
  const [view, setView] = useState<'register' | 'login'>('register')

  function clearAuth() {
    setUser(null)
    setCompanies([])
    setSelectedCompany(null)
  }

  useEffect(() => {
    async function restoreSession() {
      try {
        const response = await fetch('/api/auth/me', {
          credentials: 'include',
        })
        if (response.ok) {
          const userData: UserResponse = await response.json()
          setUser(userData)
        } else if (response.status === 401) {
          clearAuth()
        }
      } catch (error) {
        console.error('Failed to restore session', error)
      } finally {
        setLoading(false)
      }
    }
    restoreSession()
  }, [])

  useEffect(() => {
    if (user) {
      loadCompanies()
    }
  }, [user])

  async function loadCompanies() {
    setLoadingCompanies(true)
    try {
      const response = await fetch('/api/companies', {
        credentials: 'include',
      })
      if (response.ok) {
        const data: CompanyResponse[] = await response.json()
        setCompanies(data)
      }
    } catch (error) {
      console.error('Failed to load companies', error)
    } finally {
      setLoadingCompanies(false)
    }
  }

  async function createCompany(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')

    try {
      const csrf = await getCsrfToken()
      const response = await fetch('/api/companies', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName]: csrf.token,
        },
        body: JSON.stringify({ name: companyName }),
      })

      if (response.ok) {
        const newCompany: CompanyResponse = await response.json()
        setCompanyName('')
        setMessage(`Company "${newCompany.name}" created successfully.`)
        await loadCompanies()
      } else {
        const error: ApiError = await response.json().catch(() => ({}))
        setMessage(error.message ?? 'Failed to create company.')
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'The backend is unavailable.')
    } finally {
      setSubmitting(false)
    }
  }

  async function getCsrfToken(): Promise<CsrfToken> {
    const csrfResponse = await fetch('/api/csrf', {
      credentials: 'include',
    })

    if (!csrfResponse.ok) {
      throw new Error('Could not obtain CSRF token')
    }

    return await csrfResponse.json()
  }

  async function register(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')

    try {
      const csrf = await getCsrfToken()

      const response = await fetch('/api/auth/register', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName]: csrf.token,
        },
        body: JSON.stringify({
          email,
          displayName,
          password,
        }),
      })

      if (!response.ok) {
        const error: ApiError = await response.json().catch(() => ({}))

        if (response.status === 409) {
          setMessage('A user with this email already exists.')
          return
        }

        setMessage(error.message ?? 'Registration failed.')
        return
      }

      const createdUser: UserResponse = await response.json()

      setMessage(`User ${createdUser.email} was registered successfully. You can now log in.`)
      setEmail('')
      setDisplayName('')
      setPassword('')
      setView('login')
    } catch (error) {
      setMessage(
        error instanceof Error
          ? error.message
          : 'The backend is unavailable.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  async function login(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')

    try {
      const csrf = await getCsrfToken()

      const response = await fetch('/api/auth/login', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName]: csrf.token,
        },
        body: JSON.stringify({
          email,
          password,
        }),
      })

      if (!response.ok) {
        const error: ApiError = await response.json().catch(() => ({}))
        if (response.status === 401) {
          setMessage('Invalid email or password.')
          return
        }
        setMessage(error.message ?? 'Login failed.')
        return
      }

      const userData: UserResponse = await response.json()
      setUser(userData)
      setEmail('')
      setPassword('')
    } catch (error) {
      setMessage(
        error instanceof Error
          ? error.message
          : 'The backend is unavailable.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  async function logout() {
    try {
      const csrf = await getCsrfToken()
      const response = await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
        headers: {
          [csrf.headerName]: csrf.token,
        },
      })

      if (response.ok) {
        clearAuth()
        setMessage('Logged out successfully.')
      } else {
        setMessage('Logout failed.')
      }
    } catch {
      setMessage('Logout failed.')
    }
  }

  if (loading) {
    return (
      <main className="page">
        <section className="card">
          <p>Loading session…</p>
        </section>
      </main>
    )
  }

  if (user) {
    return (
      <main className="page">
        <header className="header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <span>Logged in as <strong>{user.email}</strong></span>
          <button onClick={logout} className="link-button">Log out</button>
        </header>

        <section className="card">
          <p className="eyebrow">Tax Platform</p>
          <h1>Companies</h1>
          
          {loadingCompanies ? (
            <p>Loading companies…</p>
          ) : companies.length === 0 ? (
            <p className="intro">You don't have any companies yet.</p>
          ) : (
            <ul className="company-list" style={{ listStyle: 'none', padding: 0, margin: '1rem 0' }}>
              {companies.map((company) => (
                <li key={company.id} style={{ marginBottom: '0.5rem' }}>
                  <button 
                    onClick={() => setSelectedCompany(company)}
                    className={selectedCompany?.id === company.id ? 'active' : ''}
                    style={{ width: '100%', textAlign: 'left', padding: '0.5rem' }}
                  >
                    {company.name} ({company.role})
                  </button>
                </li>
              ))}
            </ul>
          )}

          {selectedCompany && (
            <div className="selected-company" style={{ marginTop: '1rem', padding: '1rem', border: '1px solid #ccc', borderRadius: '4px' }}>
              <h2>Selected: {selectedCompany.name}</h2>
              <p>Role: {selectedCompany.role}</p>
              <p>ID: {selectedCompany.id}</p>
            </div>
          )}

          <hr style={{ margin: '2rem 0' }} />

          <h2>Create new company</h2>
          <form onSubmit={createCompany}>
            <label>
              Company Name
              <input
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                required
              />
            </label>
            <button disabled={submitting}>
              {submitting ? 'Creating…' : 'Create company'}
            </button>
          </form>

          {message && <p className="message">{message}</p>}
        </section>
      </main>
    )
  }

  return (
    <main className="page">
      <section className="card">
        <p className="eyebrow">Tax Platform</p>
        {view === 'register' ? (
          <>
            <h1>Create account</h1>
            <p className="intro">
              Register the first local platform user.
            </p>

            <form onSubmit={register}>
              <label>
                Display name
                <input
                  value={displayName}
                  onChange={(event) => setDisplayName(event.target.value)}
                  minLength={2}
                  maxLength={100}
                  required
                />
              </label>

              <label>
                Email
                <input
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  maxLength={320}
                  required
                />
              </label>

              <label>
                Password
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  minLength={8}
                  maxLength={72}
                  required
                />
              </label>

              <button disabled={submitting}>
                {submitting ? 'Creating account…' : 'Create account'}
              </button>
            </form>
            <p className="intro" style={{ marginTop: '1rem' }}>
              Already have an account?{' '}
              <button className="link-button" onClick={() => setView('login')}>
                Log in
              </button>
            </p>
          </>
        ) : (
          <>
            <h1>Log in</h1>
            <p className="intro">Enter your credentials to access the platform.</p>

            <form onSubmit={login}>
              <label>
                Email
                <input
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  maxLength={320}
                  required
                />
              </label>

              <label>
                Password
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  minLength={8}
                  maxLength={72}
                  required
                />
              </label>

              <button disabled={submitting}>
                {submitting ? 'Logging in…' : 'Log in'}
              </button>
            </form>
            <p className="intro" style={{ marginTop: '1rem' }}>
              Don't have an account?{' '}
              <button className="link-button" onClick={() => setView('register')}>
                Register
              </button>
            </p>
          </>
        )}

        {message && <p className="message">{message}</p>}
      </section>
    </main>
  )
}

export default App
