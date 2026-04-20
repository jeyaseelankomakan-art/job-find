import React, { useState } from "react";

const SkillSelector = ({ onAdd }) => {
  const [skillId, setSkillId] = useState("");
  const [proficiencyLevel, setProficiencyLevel] = useState("INTERMEDIATE");
  const [yearsOfExperience, setYearsOfExperience] = useState(1);

  const submit = (e) => {
    e.preventDefault();
    if (!skillId) return;
    onAdd({
      skillId: Number(skillId),
      proficiencyLevel,
      yearsOfExperience: Number(yearsOfExperience),
    });
    setSkillId("");
    setProficiencyLevel("INTERMEDIATE");
    setYearsOfExperience(1);
  };

  return (
    <form
      onSubmit={submit}
      className="grid grid-cols-3"
      style={{ marginTop: 12 }}
    >
      <input
        className="input"
        type="number"
        value={skillId}
        onChange={(e) => setSkillId(e.target.value)}
        placeholder="Skill ID"
      />
      <select
        className="select"
        value={proficiencyLevel}
        onChange={(e) => setProficiencyLevel(e.target.value)}
      >
        <option value="BEGINNER">Beginner</option>
        <option value="INTERMEDIATE">Intermediate</option>
        <option value="ADVANCED">Advanced</option>
        <option value="EXPERT">Expert</option>
      </select>
      <div style={{ display: "flex", gap: 8 }}>
        <input
          className="input"
          type="number"
          min="0"
          value={yearsOfExperience}
          onChange={(e) => setYearsOfExperience(e.target.value)}
          placeholder="Years"
        />
        <button className="btn-primary" type="submit">
          Add
        </button>
      </div>
    </form>
  );
};

export default SkillSelector;
