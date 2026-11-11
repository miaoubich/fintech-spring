const ACCESS_TOKEN_KEY = "fintech_access_token";
const REFRESH_TOKEN_KEY = "fintech_refresh_token";

export function saveTokens({ accessToken, refreshToken }) {
    if(accessToken) {
        localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    }

    if(refreshToken) {
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    }
}
    export function getAccessToken() {
        return localStorage.getItem(ACCESS_TOKEN_KEY);
    }

    export function getRefreshToken() {
        return localStorage.getItem(REFRESH_TOKEN_KEY);
    }

    export function clearTokens() {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
    }

    export function parseJwt(token) {
        if (!token) return null;

        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const bytes = Uiint8Array.from(atob(base64), c => c.charCodeAt(0));
            const jsonPayload = new TextDecoder().decode(bytes);

            return JSON.parse(jsonPayload);
        }
        catch {
            return null;
        }
    }


