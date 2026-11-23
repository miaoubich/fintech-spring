import { getAccessToken } from "./tokenStorage";

export function installAuthGuard() {
  if (typeof window === "undefined") return;

  const redirectIfNotAuthenticated = () => {
    const token = getAccessToken();
    const path = window.location.pathname;

    if (!token && path !== "/login" && path !== "/register") {
      window.location.replace("/login");
    }
  };

  // Restore from Back-Forward Cache (Back button)
  window.addEventListener("pageshow", (event) => {
    if (event.persisted) {
      redirectIfNotAuthenticated();
    }
  });

  // Tab becomes visible again (backup safety net)
  document.addEventListener("visibilitychange", () => {
    if (!document.hidden) {
      redirectIfNotAuthenticated();
    }
  });
}