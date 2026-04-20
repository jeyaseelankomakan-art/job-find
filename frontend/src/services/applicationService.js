import apiClient from "./apiClient";

const unwrap = (res) => res?.data?.data ?? res?.data;

const applicationService = {
  submit: async (jobId, coverLetter) => {
    const response = await apiClient.post("/applications", {
      jobId,
      coverLetter,
    });
    return unwrap(response);
  },

  getMine: async (params = {}) => {
    const response = await apiClient.get("/applications/user/my", { params });
    return unwrap(response);
  },

  withdraw: async (applicationId) => {
    const response = await apiClient.post(
      `/applications/${applicationId}/withdraw`,
    );
    return unwrap(response);
  },

  getRecommended: async () => {
    const response = await apiClient.get("/applications/recommended");
    return unwrap(response);
  },
};

export default applicationService;
