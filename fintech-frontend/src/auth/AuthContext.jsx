import { createContext, useContext, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom"; // <-- add this
import { authApi } from "../api/authApi";
import {
  clearTokens,
  getAccessToken,
  parseJwt,
  saveTokens,
} from "./tokenStorage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const existingToken = getAccessToken();
  const navigate = useNavigate(); // <-- add this

  const [accessToken, setAccessToken] = useState(existingToken);
  const [jwtClaims, setJwtClaims] = useState(parseJwt(existingToken));
  const [userInfo, setUserInfo] = useState(() => {
    const saved = sessionStorage.getItem("fintech_user_info");
    return saved ? JSON.parse(saved) : null;
  });

  const isAuthenticated = Boolean(accessToken);

  async function login({ username, password }) {
    const responseData = await authApi.login({ username, password });

    saveTokens({
      accessToken: responseData.accessToken,
      refreshToken: responseData.refreshToken,
    });

    const userSummary = {
      username: responseData.username,
      email: responseData.email,
      roles: responseData.roles,
    };
    sessionStorage.setItem("fintech_user_info", JSON.stringify(userSummary));

    setAccessToken(responseData.accessToken);
    setJwtClaims(parseJwt(responseData.accessToken));
    setUserInfo(userSummary);

    return responseData;
  }

  async function register(payload) {
    return authApi.register(payload);
  }

  function logout() {
    clearTokens();
    sessionStorage.removeItem("fintech_user_info");
    setAccessToken(null);
    setJwtClaims(null);
    setUserInfo(null);
    window.location.replace("/login");
  }

  const value = useMemo(
    () => ({
      accessToken,
      jwtClaims,
      userInfo,
      isAuthenticated,
      login,
      register,
      logout,
    }),
    [accessToken, jwtClaims, userInfo, isAuthenticated],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}