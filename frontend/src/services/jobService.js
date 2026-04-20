import apiClient from "./apiClient";

const unwrap = (res) => res?.data?.data ?? res?.data;

const jobService = {
  getJobs: async (params = {}) => {
    const response = await apiClient.get("/jobs", { params });
    return unwrap(response);
  },

  searchJobs: async (params = {}) => {
    const response = await apiClient.get("/jobs/search", { params });
    return unwrap(response);
  },

  getJobById: async (id) => {
    const response = await apiClient.get(`/jobs/${id}`);
    return unwrap(response);
  },
};

export default jobService;
