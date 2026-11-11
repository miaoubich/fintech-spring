import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Login() {
    const { login } = useAuth();

    const navigate = useNavigate();
    const location = useLocation();
    
    const redirectPath = location.state?.from?.pathname || "/dashboard";

    const [form, setForm] = useState({
        username: "",
        password: "",
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    function handleChange(event) {
        setForm({
            ...form,
            [event.target.name]: event.target.value,
        });
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setLoading(true);
        setError("");
    
        try{
            await login(form);
            navigate(redirectPath, { replace: true });
        }
        catch (err) {
            setError(
                err.response?.data?.message ||
                  err.response?.data?.error || 
                "An error occurred. Please try again."
            );
        }
        finally {
            setLoading(false);
        }
    }

    return (
        <div className="auth-page">
            <form className="auth-card" onSubmit={handleSubmit}>
                <h1>Login</h1>

                {error && <div className="error">{error}</div>}

                <label>Username</label>
                <input name="username" value={form.username} onChange={handleChange}
                    placeholder="Enter username" required />

                <label>Password</label>
                <input name="password" type="password" value={form.password}
                    onChange={handleChange} placeholder="Enter password" required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>

                <p> No account? <Link to="/register">Register</Link> </p>
            </form>
        </div>
    );
}