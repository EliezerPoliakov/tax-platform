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
  const [loading, setLoading] = useState(true)
  const [view, setView] = useState<'register' | 'login'>('register')

  useEffect(() => {
    async function restoreSession() {
      try {
        const response = await fetch('/api/auth/me', {
          credentials: 'include',
        })
        if (response.ok) {
          const userData: UserResponse = await response.json()
          setUser(userData)
        }
      } catch (error) {
        console.error('Failed to restore session', error)
      } finally {
        setLoading(false)
      }
    }
    restoreSession()
  }, [])

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
        setUser(null)
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
        <section className="card">
          <p className="eyebrow">Tax Platform</p>
          <h1>Welcome, {user.displayName}</h1>
          <p className="intro">
            You are logged in as <strong>{user.email}</strong>.
          </p>
          <button onClick={logout}>Log out</button>
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
