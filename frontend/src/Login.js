import React, { useState } from 'react';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();

    // Prevent double submit
    if (loading) return;

    setLoading(true);
    setMessage(null);

    try {
      console.log('Sending login for', username); // helps debug
      const res = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      const data = await res.json();

      if (res.ok) {
        setMessage({ type: 'success', text: data.message });
      } else {
        setMessage({ type: 'error', text: data.message || 'Login failed' });
      }
    } catch (err) {
      setMessage({ type: 'error', text: 'Network error. Is backend running?' });
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="login-form">
      <label>
        Username
        <input value={username} onChange={e => setUsername(e.target.value)} required />
      </label>

      <label>
        Password
        <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
      </label>

      <button type="submit" disabled={loading}>
        {loading ? 'Signing in...' : 'Sign in'}
      </button>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}
    </form>
  );
}
