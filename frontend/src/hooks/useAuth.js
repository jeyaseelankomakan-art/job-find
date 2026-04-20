import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

/**
 * Custom Hook to use Auth Context
 * Provides easy access to authentication state and methods
 */
export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
};

export default useAuth;
