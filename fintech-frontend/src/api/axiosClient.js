import axios from "axios";
import {getAccessToken} from "../auth/tokenStorage";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

// List of public endpoint patterns that should NOT receive a Bearer token
const PUBLIC_ENDPOINTS = [
  "/auth/login",
  "/auth/register",
  "/auth/refresh"
];

apiClient.interceptors.request.use((config) => {
   // Check if the target URL is a public endpoint
    const isPublicEndpoint = PUBLIC_ENDPOINTS.some((endpoint) =>
      config.url?.includes(endpoint)
    );

    // Only attach token if NOT a public endpoint
    if (!isPublicEndpoint) {
      const token = getAccessToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;