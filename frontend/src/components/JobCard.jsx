import React from "react";
import { Link } from "react-router-dom";

const JobCard = ({ job }) => {
  return (
    <article className="card" style={{ padding: 16 }}>
      <div style={{ display: "flex", justifyContent: "space-between", gap: 8 }}>
        <div>
          <h3 style={{ margin: "0 0 8px" }}>{job.title}</h3>
          <p style={{ margin: 0, color: "var(--muted)" }}>
            {job.location || "Location not specified"}
          </p>
        </div>
        <span className="badge">{job.jobType || "N/A"}</span>
      </div>

      <p style={{ color: "var(--muted)", margin: "12px 0" }}>
        {job.description?.slice(0, 160) || "No description"}
        {job.description?.length > 160 ? "..." : ""}
      </p>

      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <strong>
          {job.salaryMin?.toLocaleString?.() || 0} - {job.salaryMax?.toLocaleString?.() || 0} {job.currency || "USD"}
        </strong>
        <Link className="btn-primary" to={`/jobs/${job.id}`}>
          View Details
        </Link>
      </div>
    </article>
  );
};

export default JobCard;
