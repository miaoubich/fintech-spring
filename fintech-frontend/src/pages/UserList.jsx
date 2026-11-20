import { useEffect, useState } from "react";
import axiosClient from "../api/axiosClient";
import { useAuth } from "../auth/AuthContext";
import "../style/userList.css";

export default function UserList() {
  // FIX: AuthContext exposes userInfo, not user
  const { userInfo } = useAuth();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reconResult, setReconResult] = useState(null);

  // FIX: read roles from userInfo
  const userRoles = userInfo?.roles ?? [];
  const isSuperAdmin = userRoles.some(role => role.toLowerCase() === "super");

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axiosClient.get("/user/all");
      setUsers(response.data.content);
    } catch (err) {
      console.error("Failed to fetch users:", err);
      setError(err.response?.data?.message || "Failed to load users from user-service");
    } finally {
      setLoading(false);
    }
  };

  const runReconciliation = async () => {
    if (
      !window.confirm(
        "This will create missing Keycloak users and disable orphaned Keycloak accounts. Continue?"
      )
    )
      return;

    try {
      setReconResult(null);
      const res = await axiosClient.post("/user/reconcile");
      setReconResult(res.data);
    } catch (err) {
      alert(
        "Reconciliation failed: " +
          (err.response?.data?.message || err.message)
      );
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h2>System Users</h2>
        <div className="toolbar">
          <button className="btn-refresh" onClick={fetchUsers}>
            Refresh
          </button>

          {/* Only show to super role */}
          {isSuperAdmin && (
            <button className="btn-reconcile" onClick={runReconciliation}>
              Reconcile Keycloak
            </button>
          )}
        </div>
      </div>

      {/* Reconciliation Result Banner */}
      {reconResult && (
        <div className="recon-banner">
          Reconciliation complete:<br />
          Created in Keycloak: {reconResult.createdInKeycloak}<br />
          Disabled in Keycloak: {reconResult.disabledInKeycloak}<br />
          {reconResult.errors.length > 0 &&
            `Errors: ${reconResult.errors.join(", ")}`}
        </div>
      )}

      {loading && <p className="status-text">Loading users...</p>}
      {error && <div className="error-banner">{error}</div>}

      {!loading && !error && (
        <div className="table-responsive">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Full Name</th>
                <th>Status</th>
                <th>Assigned Roles</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u, idx) => (
                <tr key={u.username || idx}>
                  <td className="font-bold">{u.username}</td>
                  <td>{u.email}</td>
                  <td>{`${u.firstName || ""} ${u.lastName || ""}`.trim() || "-"}</td>
                  <td>
                    <span
                      className={
                        u.enabled ? "badge badge-active" : "badge badge-disabled"
                      }
                    >
                      {u.enabled ? "Active" : "Disabled"}
                    </span>
                  </td>
                  <td>
                    {u.roles && u.roles.length > 0 ? (
                      u.roles.map((role) => (
                        <span key={role} className="badge badge-role">
                          {role}
                        </span>
                      ))
                    ) : (
                      <span className="text-muted">None</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}