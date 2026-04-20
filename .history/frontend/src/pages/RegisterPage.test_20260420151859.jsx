import React from "react";
import { MemoryRouter } from "react-router-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { vi } from "vitest";
import RegisterPage from "./RegisterPage";

const navigateMock = vi.fn();
const registerMock = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock("../hooks/useAuth", () => ({
  useAuth: () => ({
    register: registerMock,
    isAuthenticated: false,
    loading: false,
    error: null,
  }),
}));

describe("RegisterPage", () => {
  beforeEach(() => {
    navigateMock.mockClear();
    registerMock.mockClear();
  });

  it("submits the registration form and redirects to login", async () => {
    registerMock.mockResolvedValue({ data: { success: true } });

    render(
      <MemoryRouter>
        <RegisterPage />
      </MemoryRouter>,
    );

    fireEvent.change(screen.getByLabelText(/email address/i), {
      target: { value: "new.user@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/full name/i), {
      target: { value: "New User" },
    });
    fireEvent.change(screen.getByLabelText(/^password$/i), {
      target: { value: "Password123!" },
    });
    fireEvent.change(screen.getByLabelText(/confirm password/i), {
      target: { value: "Password123!" },
    });
    fireEvent.click(screen.getByRole("checkbox"));
    fireEvent.click(screen.getByRole("button", { name: /sign up/i }));

    await waitFor(() => {
      expect(registerMock).toHaveBeenCalledWith(
        "new.user@example.com",
        "Password123!",
        "New User",
        "",
        "JOB_SEEKER",
      );
    });

    expect(navigateMock).toHaveBeenCalledWith("/login", {
      state: { message: "Registration successful! Please log in." },
    });
  });
});
