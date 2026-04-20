import React from "react";

const JobFilters = ({ filters, onChange, onSearch, onReset }) => {
  return (
    <section className="card" style={{ padding: 16, marginBottom: 16 }}>
      <div className="grid grid-cols-3">
        <input
          className="input"
          name="title"
          value={filters.title}
          onChange={onChange}
          placeholder="Title (e.g. Java Developer)"
        />
        <input
          className="input"
          name="location"
          value={filters.location}
          onChange={onChange}
          placeholder="Location"
        />
        <select
          className="select"
          name="jobType"
          value={filters.jobType}
          onChange={onChange}
        >
          <option value="">All job types</option>
          <option value="FULL_TIME">Full Time</option>
          <option value="PART_TIME">Part Time</option>
          <option value="CONTRACT">Contract</option>
          <option value="INTERNSHIP">Internship</option>
        </select>
        <input
          className="input"
          type="number"
          name="salaryMin"
          value={filters.salaryMin}
          onChange={onChange}
          placeholder="Minimum salary"
        />
        <input
          className="input"
          type="number"
          name="salaryMax"
          value={filters.salaryMax}
          onChange={onChange}
          placeholder="Maximum salary"
        />
        <select
          className="select"
          name="experienceLevel"
          value={filters.experienceLevel}
          onChange={onChange}
        >
          <option value="">All levels</option>
          <option value="ENTRY">Entry</option>
          <option value="MID">Mid</option>
          <option value="SENIOR">Senior</option>
          <option value="LEAD">Lead</option>
        </select>
      </div>
      <div style={{ marginTop: 12, display: "flex", gap: 10 }}>
        <button className="btn-primary" type="button" onClick={onSearch}>
          Search
        </button>
        <button className="btn-secondary" type="button" onClick={onReset}>
          Reset
        </button>
      </div>
    </section>
  );
};

export default JobFilters;
