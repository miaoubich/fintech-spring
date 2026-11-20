import { getAccessToken } from "./tokenStorage";

export function installBfcacheGuard() {
  if (typeof window === "undefined") return;

  const onPageShow = (event) => {
    // event.persisted = true when page is restored from Back-Forward Cache
    if (event.persisted && !getAccessToken()) {
      window.location.replace("/login");
    }
  };

  window.addEventListener("pageshow", onPageShow);
}