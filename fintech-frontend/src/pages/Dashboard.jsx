import { useAuth } from "../auth/AuthContext";

export default function Dashboard() {
  const { user, logout } = useAuth();

  const realmRoles = user?.realm_access?.roles || [];
  const fintechRoles = user?.resource_access?.fintech?.roles || [];

  return (
    <div className="dashboard">
      <h1>Fintech Dashboard</h1>

      <p>
        Logged in as: <strong>{user?.preferred_username || user?.sub}</strong>
      </p>

      <h3>Client Roles</h3>
      <pre>{JSON.stringify(fintechRoles, null, 2)}</pre>

      <h3>Realm Roles</h3>
      <pre>{JSON.stringify(realmRoles, null, 2)}</pre>

      <button onClick={logout}>Logout</button>
    </div>
  );
}