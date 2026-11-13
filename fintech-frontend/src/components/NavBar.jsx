import { NavLink, useNavigate } from "react-router-dom";
import * as tokenStorage from "../auth/tokenStorage";
import "./NavBar.css";

// Add new entries here as you build each feature page.
// `to` must match a <Route> in App.jsx.
const NAV_ITEMS = [
    {to: "dashboard", label: "Dashboard", end: true},
    { to: "/trade",     label: "Trades" },
    { to: "/ledger",    label: "Ledger" },
    { to: "/wallet",    label: "Wallet" },
];

export default function NavBar() {
    const navigate = useNavigate();

    const handleLogout = () => {
    tokenStorage.clearTokens();
    navigate("/login", { replace: true });
  };

  return (
    <nav className="navbar">
      <NavLink to="/dashboard" className="navbar-brand">
        Fintech
      </NavLink>

      <ul className="navbar-links">
        {NAV_ITEMS.map(({ to, label, end }) => (
          <li key={to}>
            <NavLink
              to={to}
              end={end}
              className={({ isActive }) =>
                "navbar-link" + (isActive ? " navbar-link--active" : "")
              }
            >
              {label}
            </NavLink>
          </li>
        ))}
      </ul>

      <button type="button" className="navbar-logout" onClick={handleLogout}>
        Logout
      </button>
    </nav>
  );
}