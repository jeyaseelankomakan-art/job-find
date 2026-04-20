import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import jobService from "../services/jobService";
import applicationService from "../services/applicationService";

const JobDetailsPage = () => {
  const { id } = useParams();
  const [job, setJob] = useState(null);
  const [coverLetter, setCoverLetter] = useState("");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const data = await jobService.getJobById(id);
        setJob(data);
      } catch (err) {
        setError(err?.response?.data?.message || "Failed to load job details.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  const handleApply = async () => {
    setError("");
    setMessage("");
    try {
      await applicationService.submit(Number(id), coverLetter);
      setMessage("Application submitted successfully.");
      setCoverLetter("");
    } catch (err) {
      setError(err?.response?.data?.message || "Could not submit application.");
    }
  };

  if (loading) return <p>Loading details...</p>;
  if (!job) return <p>Job not found.</p>;

  return (
    <section>
      <div className="page-head">
        <h1>{job.title}</h1>
        <p>
          {job.location || "Unknown location"} | {job.jobType || "N/A"}
        </p>
      </div>

      <article className="card" style={{ padding: 16 }}>
        <p>
          <strong>Experience:</strong> {job.experienceLevel || "Not specified"}
        </p>
        <p>
          <strong>Salary:</strong> {job.salaryMin || 0} - {job.salaryMax || 0}{" "}
          {job.currency || "USD"}
        </p>
        <p>
          <strong>Remote:</strong> {job.remote ? "Yes" : "No"}
        </p>
        <hr style={{ borderColor: "var(--line)" }} />
        <p>{job.description || "No description provided."}</p>
        {job.requirements ? (
          <>
            <h3>Requirements</h3>
            <p>{job.requirements}</p>
          </>
        ) : null}
      </article>

      <section className="card" style={{ padding: 16, marginTop: 16 }}>
        <h3 style={{ marginTop: 0 }}>Apply to this role</h3>
        <textarea
          className="textarea"
          placeholder="Write a short cover letter..."
          value={coverLetter}
          onChange={(e) => setCoverLetter(e.target.value)}
        />
        <div style={{ marginTop: 10 }}>
          <button className="btn-primary" type="button" onClick={handleApply}>
            Submit Application
          </button>
        </div>
        {message ? <p style={{ color: "var(--primary)" }}>{message}</p> : null}
        {error ? <p style={{ color: "var(--danger)" }}>{error}</p> : null}
      </section>
    </section>
  );
};

export default JobDetailsPage;
