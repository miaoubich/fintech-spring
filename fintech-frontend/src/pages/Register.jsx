import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const ROLES = [
        { value: "SUPER", label: "Super" },
        { value: "ADMIN1", label: "Admin 1" },
        { value: "AUDIT1", label: "Audit 1" },
        { value: "AUDIT2", label: "Audit 2" },
        { value: "COMPLIANCE1", label: "Compliance 1" },
        { value: "COMPLIANCE2", label: "Compliance 2" },
        { value: "FRAUD1", label: "Fraud 1" },
        { value: "FRAUD2", label: "Fraud 2" },
        { value: "SUPPORT1", label: "Support 1" },
        { value: "SUPPORT2", label: "Support 2" },
        { value: "USER1", label: "User 1" },
        { value: "USER2", label: "User 2" },
        { value: "USER3", label: "User 3" }
    ];

  const [form, setForm] = useState({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
    role: "USER1" // Default role for new user
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({
      ...form,
      [event.target.name]: event.target.value,
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const payload = {
        username: form.username,
        email: form.email,
        firstName: form.firstName,
        lastName: form.lastName,
        password: form.password,
        role: form.role
      };

      await register(payload);

      setSuccess("Registration successful. Redirecting to login...");

      setTimeout(() => {
        navigate("/login");
      }, 1000);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Registration failed"
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Register</h1>

        {error && <div className="error">{error}</div>}
        {success && <div className="success">{success}</div>}

        <label>Username</label>
        <input name="username" value={form.username} onChange={handleChange} required/>
        
        <label>Email</label>
        <input name="email" type="email" value={form.email} onChange={handleChange} required/>
        
        <label>First Name</label>
        <input name="firstName" value={form.firstName} onChange={handleChange}/>
        
        <label>Last Name</label>
        <input name="lastName" value={form.lastName} onChange={handleChange}/>
        
        <label>Password</label>
        <input name="password" type="password" value={form.password} onChange={handleChange} required/>
        
        <label>Role</label>
        <select name="role" value={form.role} onChange={handleChange} required>
            {ROLES.map((role) => (
                <option key={role.value} value={role.value}>
                    {role.label}
                </option>
            ))}
        </select>

        <button type="submit" disabled={loading}>
          {loading ? "Registering..." : "Register"}
        </button>

        <p> Already have an account? <Link to="/login">Login</Link> </p>

      </form>
    </div>
  );
}