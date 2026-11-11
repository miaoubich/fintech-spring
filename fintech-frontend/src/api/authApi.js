import apiClient from "./axiosClient";

function normalizeTokenResponse(data) {
    return {
        accessToken: data.accessToken || data.access_token || data.token,
        refreshToken: data.refreshToken || data.refresh_token,
        expiresIn: data.expiresIn || data.expires_in,
        tokenType: data.tokenType || data.token_type || "Bearer",
        raw: data,
    };
}

export const authApi = {
    register: async (payload) => {
        const response = await apiClient.post("/auth/register", payload);
        return response.data;
    },

    login: async (payload) => {
        const response = await apiClient.post("/auth/login", payload);
        return normalizeTokenResponse(response.data);
    },

    refresh: async (refreshToken) => {
        const response = await apiClient.post("/auth/refresh", { 
            refreshToken 
        });
        return normalizeTokenResponse(response.data);
    }
}