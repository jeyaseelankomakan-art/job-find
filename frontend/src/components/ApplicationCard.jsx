import React from "react";

const ApplicationCard = ({ item, onWithdraw }) => {
  const canWithdraw = item.status === "PENDING";

  return (
    <article className="card" style={{ padding: 16 }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div>
          <h3 style={{ margin: "0 0 4px" }}>Application #{item.id}</h3>
          <p style={{ margin: 0, color: "var(--muted)" }}>
            Job ID: {item.jobId}
          </p>
        </div>
        <span className="badge">{item.status}</span>
      </div>

      <p style={{ margin: "10px 0", color: "var(--muted)" }}>
        Match Score: <strong>{Math.round(item.matchScore || 0)}%</strong>
      </p>

      {canWithdraw && (
        <button
          type="button"
          className="btn-danger"
          onClick={() => onWithdraw(item.id)}
        >
          Withdraw
        </button>
      )}
    </article>
  );
};

export default ApplicationCard;
