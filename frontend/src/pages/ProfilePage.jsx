import React, { useEffect, useMemo, useState } from "react";
import SkillSelector from "../components/SkillSelector";
import userService from "../services/userService";

const ProfilePage = () => {
  const [me, setMe] = useState(null);
  const [skills, setSkills] = useState([]);
  const [form, setForm] = useState({
    fullName: "",
    phone: "",
    location: "",
    bio: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const userId = useMemo(() => me?.id, [me]);

  const load = async () => {
    setError("");
    try {
      const profile = await userService.getMe();
      setMe(profile);
      setForm({
        fullName: profile?.fullName || "",
        phone: profile?.phone || "",
        location: profile?.location || "",
        bio: profile?.bio || "",
      });

      if (profile?.id) {
        const userSkills = await userService.getUserSkills(profile.id);
        setSkills(
          Array.isArray(userSkills) ? userSkills : userSkills?.content || [],
        );
      }
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load profile.");
    }
  };

  useEffect(() => {
    load();
  }, []);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const saveProfile = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    try {
      if (!userId) return;
      const updated = await userService.updateProfile(userId, form);
      setMe(updated);
      setSuccess("Profile updated successfully.");
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to save profile.");
    }
  };

  const addSkill = async (payload) => {
    setError("");
    setSuccess("");
    try {
      if (!userId) return;
      await userService.addUserSkill(userId, payload);
      await load();
      setSuccess("Skill added.");
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to add skill.");
    }
  };

  const removeSkill = async (skillId) => {
    setError("");
    setSuccess("");
    try {
      if (!userId) return;
      await userService.removeUserSkill(userId, skillId);
      await load();
      setSuccess("Skill removed.");
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to remove skill.");
    }
  };

  return (
    <section>
      <div className="page-head">
        <h1>My Profile</h1>
        <p>Keep your profile updated to improve job match quality.</p>
      </div>

      {error ? <p style={{ color: "var(--danger)" }}>{error}</p> : null}
      {success ? <p style={{ color: "var(--primary)" }}>{success}</p> : null}

      <form className="card" style={{ padding: 16 }} onSubmit={saveProfile}>
        <div className="grid grid-cols-2">
          <input
            className="input"
            name="fullName"
            value={form.fullName}
            onChange={onChange}
            placeholder="Full name"
          />
          <input
            className="input"
            name="phone"
            value={form.phone}
            onChange={onChange}
            placeholder="Phone"
          />
          <input
            className="input"
            name="location"
            value={form.location}
            onChange={onChange}
            placeholder="Location"
          />
          <input
            className="input"
            name="bio"
            value={form.bio}
            onChange={onChange}
            placeholder="Short bio"
          />
        </div>
        <div style={{ marginTop: 12 }}>
          <button className="btn-primary" type="submit">
            Save Profile
          </button>
        </div>
      </form>

      <section className="card" style={{ padding: 16, marginTop: 16 }}>
        <h2 style={{ marginTop: 0 }}>Skills</h2>
        <SkillSelector onAdd={addSkill} />
        <div style={{ marginTop: 12 }} className="grid">
          {skills.map((item) => (
            <div
              key={item.id}
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                padding: "10px 12px",
                border: "1px solid var(--line)",
                borderRadius: 10,
              }}
            >
              <span>
                {item.skill?.name || `Skill #${item.skillId}`} (
                {item.proficiencyLevel})
              </span>
              <button
                className="btn-danger"
                type="button"
                onClick={() => removeSkill(item.skillId)}
              >
                Remove
              </button>
            </div>
          ))}
          {skills.length === 0 ? (
            <p>No skills yet. Add your first skill.</p>
          ) : null}
        </div>
      </section>
    </section>
  );
};

export default ProfilePage;
