import { useState, type SubmitEvent } from 'react'
import './App.css'

type CsrfToken = {
  headerName: string
  parameterName: string
  token: string
}

type RegistrationResponse = {
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

  async function register(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setMessage('')

    try {
      const csrfResponse = await fetch('/api/csrf', {
        credentials: 'include',
      })

      if (!csrfResponse.ok) {
        throw new Error('Could not obtain CSRF token')
      }

      const csrf: CsrfToken = await csrfResponse.json()

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

      const createdUser: RegistrationResponse = await response.json()

      setMessage(`User ${createdUser.email} was registered successfully.`)
      setEmail('')
      setDisplayName('')
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

  return (
    <main className="page">
      <section className="card">
        <p className="eyebrow">Tax Platform</p>
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

        {message && <p className="message">{message}</p>}
      </section>
    </main>
  )
}

export default App
