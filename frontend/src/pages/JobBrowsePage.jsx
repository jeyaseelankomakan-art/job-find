import React, { useEffect, useState } from "react";
import JobFilters from "../components/JobFilters";
import JobCard from "../components/JobCard";
import jobService from "../services/jobService";

const defaultFilters = {
  title: "",
  location: "",
  salaryMin: "",
  salaryMax: "",
  experienceLevel: "",
  jobType: "",
};

const JobBrowsePage = () => {
  const [filters, setFilters] = useState(defaultFilters);
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [page, setPage] = useState(0);

  const loadJobs = async (searchParams = {}) => {
    setLoading(true);
    setError("");
    try {
      const data = await jobService.searchJobs({
        page,
        pageSize: 20,
        ...searchParams,
      });
      const content = data?.content || data?.items || data || [];
      setJobs(Array.isArray(content) ? content : []);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load jobs.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadJobs();
  }, [page]);

  const onFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const onSearch = () => {
    setPage(0);
    loadJobs(filters);
  };

  const onReset = () => {
    setFilters(defaultFilters);
    setPage(0);
    loadJobs({});
  };

  return (
    <section>
      <div className="page-head">
        <h1>Browse Jobs</h1>
        <p>
          Find roles that match your skills, salary goals, and location
          preferences.
        </p>
      </div>

      <JobFilters
        filters={filters}
        onChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
      />

      {error && <p style={{ color: "var(--danger)" }}>{error}</p>}
      {loading ? <p>Loading jobs...</p> : null}

      <div className="grid grid-cols-2">
        {jobs.map((job) => (
          <JobCard key={job.id} job={job} />
        ))}
      </div>

      {!loading && jobs.length === 0 ? (
        <p>No jobs found for this filter set.</p>
      ) : null}

      <div className="pager">
        <button
          className="btn-secondary"
          type="button"
          onClick={() => setPage((p) => Math.max(0, p - 1))}
        >
          Previous
        </button>
        <span style={{ alignSelf: "center" }}>Page {page + 1}</span>
        <button
          className="btn-secondary"
          type="button"
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </section>
  );
};

export default JobBrowsePage;
