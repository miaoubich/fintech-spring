import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { getAccessToken } from "../auth/tokenStorage";
import { useEffect } from "react";

export default function ProtectedRoute() {
  // const auth = useAuth();
  // const isAuthenticated = auth.isAuthenticated;
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  // Redirect if the page is restored from browser back/forward cache
  useEffect(() => {
    const handlePageShow = (event) => {
      if (event.persisted && !getAccessToken()) {
        window.location.replace("/login");
      }
    };

    window.addEventListener("pageshow", handlePageShow);
    return () => window.removeEventListener("pageshow", handlePageShow);
  }, []);

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}