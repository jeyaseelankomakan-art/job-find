import apiClient from "./apiClient";

const unwrap = (res) => res?.data?.data ?? res?.data;

const userService = {
  getMe: async () => {
    const response = await apiClient.get("/users/me");
    return unwrap(response);
  },

  updateProfile: async (userId, payload) => {
    const response = await apiClient.put(`/users/${userId}`, payload);
    return unwrap(response);
  },

  getUserSkills: async (userId) => {
    const response = await apiClient.get(`/users/${userId}/skills`);
    return unwrap(response);
  },

  addUserSkill: async (userId, payload) => {
    const response = await apiClient.post(`/users/${userId}/skills`, payload);
    return unwrap(response);
  },

  removeUserSkill: async (userId, skillId) => {
    const response = await apiClient.delete(
      `/users/${userId}/skills/${skillId}`,
    );
    return unwrap(response);
  },
};

export default userService;
