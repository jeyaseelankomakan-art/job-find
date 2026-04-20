import React, { useEffect, useState } from "react";
import ApplicationCard from "../components/ApplicationCard";
import applicationService from "../services/applicationService";

const MyApplicationsPage = () => {
  const [applications, setApplications] = useState([]);
  const [recommended, setRecommended] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const mine = await applicationService.getMine({ page: 0, pageSize: 20 });
      const content = mine?.content || mine?.items || mine || [];
      setApplications(Array.isArray(content) ? content : []);

      const recs = await applicationService.getRecommended();
      setRecommended(Array.isArray(recs) ? recs : recs?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load applications.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const onWithdraw = async (applicationId) => {
    setError("");
    try {
      await applicationService.withdraw(applicationId);
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message || "Failed to withdraw application.",
      );
    }
  };

  return (
    <section>
      <div className="page-head">
        <h1>My Applications</h1>
        <p>
          Track submitted applications and monitor your current match scores.
        </p>
      </div>

      {error ? <p style={{ color: "var(--danger)" }}>{error}</p> : null}
      {loading ? <p>Loading applications...</p> : null}

      <h2>Submitted</h2>
      <div className="grid">
        {applications.map((item) => (
          <ApplicationCard key={item.id} item={item} onWithdraw={onWithdraw} />
        ))}
      </div>
      {!loading && applications.length === 0 ? (
        <p>No applications submitted yet.</p>
      ) : null}

      <h2 style={{ marginTop: 20 }}>Recommended (score 55%+)</h2>
      <div className="grid">
        {recommended.map((item) => (
          <ApplicationCard
            key={`rec-${item.id}`}
            item={item}
            onWithdraw={() => {}}
          />
        ))}
      </div>
      {!loading && recommended.length === 0 ? (
        <p>No recommendations yet.</p>
      ) : null}
    </section>
  );
};

export default MyApplicationsPage;
