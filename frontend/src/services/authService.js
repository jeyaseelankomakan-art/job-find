import apiClient from "./apiClient";

/**
 * Authentication Service
 * Handles all authentication-related API calls
 */
const authService = {
  /**
   * Register a new user
   */
  register: (email, password, fullName, phone, role) => {
    return apiClient.post("/auth/register", {
      email,
      password,
      fullName,
      phone,
      role,
    });
  },

  /**
   * Login user
   */
  login: (email, password) => {
    return apiClient.post("/auth/login", {
      email,
      password,
    });
  },

  /**
   * Refresh token
   */
  refreshToken: (refreshToken) => {
    return apiClient.post("/auth/refresh", {
      refreshToken,
    });
  },

  /**
   * Logout user (clear local storage)
   */
  logout: () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    return Promise.resolve();
  },

  /**
   * Forgot password
   */
  forgotPassword: (email) => {
    return apiClient.post("/auth/forgot-password", {
      email,
    });
  },

  /**
   * Reset password
   */
  resetPassword: (token, newPassword) => {
    return apiClient.post("/auth/reset-password", {
      token,
      newPassword,
    });
  },

  /**
   * Get current user from local storage
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated: () => {
    return !!localStorage.getItem("accessToken");
  },

  /**
   * Get access token
   */
  getAccessToken: () => {
    return localStorage.getItem("accessToken");
  },

  /**
   * Set user data in local storage
   */
  setUserData: (user) => {
    localStorage.setItem("user", JSON.stringify(user));
  },

  /**
   * Set tokens in local storage
   */
  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
  },
};

export default authService;
