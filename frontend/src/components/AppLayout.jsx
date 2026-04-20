import React, { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const AppLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate("/login", { replace: true });
  };

  const closeMenu = () => setMenuOpen(false);

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-brand-row">
          <Link to="/jobs" className="brand">
            JobMatch
          </Link>
          <button
            type="button"
            className="menu-toggle"
            onClick={() => setMenuOpen((open) => !open)}
            aria-expanded={menuOpen}
            aria-label="Toggle navigation menu"
          >
            Menu
          </button>
        </div>
        <div className={`topbar-nav-area ${menuOpen ? "is-open" : ""}`}>
          <nav className="topnav">
            <NavLink to="/jobs" onClick={closeMenu}>
              Jobs
            </NavLink>
            <NavLink to="/applications" onClick={closeMenu}>
              My Applications
            </NavLink>
            <NavLink to="/profile" onClick={closeMenu}>
              Profile
            </NavLink>
          </nav>
          <div className="topbar-user">
            <span className="topbar-user-name">
              {user?.fullName || user?.email || "User"}
            </span>
            <button
              onClick={handleLogout}
              className="btn-secondary"
              type="button"
            >
              Logout
            </button>
          </div>
        </div>
      </header>
      <main className="content-wrap">
        <Outlet />
      </main>
    </div>
  );
};

export default AppLayout;
