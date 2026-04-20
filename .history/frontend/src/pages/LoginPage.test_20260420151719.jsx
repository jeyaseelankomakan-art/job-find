import React from "react";
import { MemoryRouter } from "react-router-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { vi } from "vitest";
import LoginPage from "./LoginPage";

const navigateMock = vi.fn();
const loginMock = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock("../hooks/useAuth", () => ({
  useAuth: () => ({
    login: loginMock,
    isAuthenticated: false,
    loading: false,
    error: null,
  }),
}));

describe("LoginPage", () => {
  beforeEach(() => {
    navigateMock.mockClear();
    loginMock.mockClear();
  });

  it("submits the login form and navigates to jobs", async () => {
    loginMock.mockResolvedValue({ data: { success: true } });

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );

    fireEvent.change(screen.getByLabelText(/email address/i), {
      target: { value: "user@example.com" },
    });
    fireEvent.change(screen.getByLabelText(/^password$/i), {
      target: { value: "Password123!" },
    });
    fireEvent.click(screen.getByRole("button", { name: /login/i }));

    await waitFor(() => {
      expect(loginMock).toHaveBeenCalledWith(
        "user@example.com",
        "Password123!",
      );
    });

    expect(navigateMock).toHaveBeenCalledWith("/jobs");
  });
});