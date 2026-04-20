import React, { createContext, useState, useCallback, useEffect } from "react";
import authService from "../services/authService";

/**
 * Auth Context
 * Provides authentication state and methods to all components
 */
export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Initialize auth state on mount
  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (currentUser && authService.isAuthenticated()) {
      setUser(currentUser);
      setIsAuthenticated(true);
    }
    setLoading(false);
  }, []);

  /**
   * Register user
   */
  const register = useCallback(
    async (email, password, fullName, phone, role) => {
      setLoading(true);
      setError(null);
      try {
        const response = await authService.register(
          email,
          password,
          fullName,
          phone,
          role,
        );
        const newUser = response.data.data;
        setUser(newUser);
        authService.setUserData(newUser);
        setIsAuthenticated(false); // User needs to login after registration
        setLoading(false);
        return response.data;
      } catch (err) {
        const errorMsg = err.response?.data?.message || "Registration failed";
        setError(errorMsg);
        setLoading(false);
        throw err;
      }
    },
    [],
  );

  /**
   * Login user
   */
  const login = useCallback(async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authService.login(email, password);
      const { accessToken, refreshToken, user: userData } = response.data.data;

      authService.setTokens(accessToken, refreshToken);
      authService.setUserData(userData);
      setUser(userData);
      setIsAuthenticated(true);
      setLoading(false);

      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || "Login failed";
      setError(errorMsg);
      setLoading(false);
      throw err;
    }
  }, []);

  /**
   * Logout user
   */
  const logout = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      await authService.logout();
      setUser(null);
      setIsAuthenticated(false);
      setLoading(false);
    } catch (err) {
      setError("Logout failed");
      setLoading(false);
    }
  }, []);

  /**
   * Refresh token
   */
  const refresh = useCallback(async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
      return logout();
    }

    try {
      const response = await authService.refreshToken(refreshToken);
      const {
        accessToken,
        refreshToken: newRefreshToken,
        user: userData,
      } = response.data.data;

      authService.setTokens(accessToken, newRefreshToken);
      authService.setUserData(userData);
      setUser(userData);
      setIsAuthenticated(true);

      return response.data;
    } catch (err) {
      logout();
      throw err;
    }
  }, [logout]);

  /**
   * Forgot password
   */
  const forgotPassword = useCallback(async (email) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authService.forgotPassword(email);
      setLoading(false);
      return response.data;
    } catch (err) {
      const errorMsg =
        err.response?.data?.message || "Failed to process request";
      setError(errorMsg);
      setLoading(false);
      throw err;
    }
  }, []);

  /**
   * Reset password
   */
  const resetPassword = useCallback(async (token, newPassword) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authService.resetPassword(token, newPassword);
      setLoading(false);
      return response.data;
    } catch (err) {
      const errorMsg =
        err.response?.data?.message || "Failed to reset password";
      setError(errorMsg);
      setLoading(false);
      throw err;
    }
  }, []);

  const value = {
    user,
    isAuthenticated,
    loading,
    error,
    register,
    login,
    logout,
    refresh,
    forgotPassword,
    resetPassword,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthProvider;
