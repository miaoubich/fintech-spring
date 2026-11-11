import { createContext, useContext, useMemo, useState } from "react";
import { authApi } from "../api/authApi";
import { clearTokens, getAccessToken, parseJwt, saveTokens } from "./tokenStorage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const existingToken = getAccessToken();

  const [accessToken, setAccessToken] = useState(existingToken);
  const [user, setUser] = useState(parseJwt(existingToken));

  const isAuthenticated = Boolean(accessToken);

  async function login({ username, password }) {
    const tokens = await authApi.login({
      username,
      password,
    });

    saveTokens(tokens);

    setAccessToken(tokens.accessToken);
    setUser(parseJwt(tokens.accessToken));

    return tokens;
  }

  async function register(payload) {
    return authApi.register(payload);
  }

  function logout() {
    clearTokens();
    setAccessToken(null);
    setUser(null);
  }

  const value = useMemo(
    () => ({
      accessToken,
      user,
      isAuthenticated,
      login,
      register,
      logout,
    }),
    [accessToken, user, isAuthenticated]
  );

  return <AuthContext.Provider value={value}>
            {children}
         </AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}