import { useAuth } from "../auth/AuthContext";

export default function Dashboard() {
  const { userInfo, jwtClaims, logout } = useAuth();

  // Extract roles directly from the decoded Keycloak JWT
  const realmRoles = jwtClaims?.realm_access?.roles || [];
  const clientRoles = jwtClaims?.resource_access?.fintech?.roles || [];

  return (
    <div className="dashboard">
      <h1>Fintech Dashboard</h1>

      <div style={{ marginBottom: "20px" }}>
        <h2>User Info (from Backend)</h2>
        <p>
          <strong>Username:</strong>{" "}
          {userInfo?.username || jwtClaims?.preferred_username || "N/A"}
        </p>
        <p>
          <strong>Email:</strong> {userInfo?.email || jwtClaims?.email || "N/A"}
        </p>
        <p>
          <strong>Assigned App Roles:</strong>{" "}
          {userInfo?.roles?.join(", ") || "None"}
        </p>
      </div>

      <hr />

      <div style={{ marginTop: "20px" }}>
        <h2>Keycloak JWT Claims</h2>

        <h3>Client Roles (`fintech`)</h3>
        <pre>{JSON.stringify(clientRoles, null, 2)}</pre>

        <h3>Realm Roles</h3>
        <pre>{JSON.stringify(realmRoles, null, 2)}</pre>
      </div>

      <button onClick={logout} style={{ marginTop: "20px" }}>
        Logout
      </button>
    </div>
  );
}