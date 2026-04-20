# Job Matching Engine - Algorithm & Implementation

## Table of Contents

1. [Overview](#overview)
2. [Matching Factors](#matching-factors)
3. [Scoring Algorithm](#scoring-algorithm)
4. [Implementation Logic](#implementation-logic)
5. [Recommendations Engine](#recommendations-engine)
6. [Future ML Enhancements](#future-ml-enhancements)

---

## Overview

The matching engine calculates compatibility between:

1. **User ↔ Job Match** (for job recommendations)
2. **Job ↔ User Match** (for candidate recommendations)

**Output**: Match score (0-100%) and detailed breakdown

---

## Matching Factors

### 1. Skill Match (Weight: 40%)

Compares job requirements against user's skills

**Formula**:

```
Skill Match % = (Matched Skills Count / Total Required Skills) × 100

Where:
- Matched Skills = Skills user has that are required
- Total Required Skills = All skills job requires

Bonus for proficiency level:
- If user's level >= required level: Full match
- If user's level < required level: 50% match (still has skill, needs upskilling)
```

**Example**:

```
Job requires: Java (ADVANCED), Spring Boot (INTERMEDIATE), MySQL (INTERMEDIATE)
User has: Java (EXPERT), Spring Boot (ADVANCED), Docker (ADVANCED)

Matched Skills:
- Java: EXPERT >= ADVANCED → Full match (1)
- Spring Boot: ADVANCED >= INTERMEDIATE → Full match (1)
- MySQL: Missing → 0 match

Skill Match = (2 + 0) / 3 × 100 = 66.67%
```

### 2. Experience Match (Weight: 30%)

Compares years of experience and job level

**Formula**:

```
Experience Match % = (User Years / Required Years) × 100

Capped at:
- 100% if user has equal or more years
- Minimum 20% if user has at least started career

Job Level Matching:
- ENTRY (0-1 years)
- MID (2-4 years)
- SENIOR (5-9 years)
- LEAD (10+ years)
```

**Scoring Logic**:

```
If user_level >= required_level:
    Experience Match = 100% + bonus for over-qualification
Else if user_level == (required_level - 1):
    Experience Match = 80% (close to target level)
Else if user_level == (required_level - 2):
    Experience Match = 50% (willing to grow into role)
Else:
    Experience Match = 20% (significant gap)
```

**Example**:

```
Job requires: SENIOR (5-9 years)
User has: 6 years experience (SENIOR level)

Experience Match = 100%
```

### 3. Location Match (Weight: 15%)

Matches user location with job location

**Formula**:

```
If job is remote:
    Location Match = 100% (any user can apply)

Else if job location != remote:
    If user_location == job_location:
        Location Match = 100%
    Else if cities are in same metro area:
        Location Match = 80%
    Else if same state/country:
        Location Match = 50%
    Else:
        Location Match = 30% (different region, requires relocation)
```

**Example**:

```
Job location: San Francisco, CA (non-remote)

User A (SF, CA): 100% match
User B (Oakland, CA): 80% match (same bay area)
User C (LA, CA): 50% match (same state)
User D (Austin, TX): 30% match (different state)
User E (remote): 100% (if job allows remote)
```

### 4. Salary Match (Weight: 10%)

Matches expected salary with job salary range

**Formula**:

```
If user_expected_salary is within job_salary_range:
    Salary Match = 100%

Else if user_expected_salary > job_max_salary:
    Percentage over = (user_expected - job_max) / job_max × 100
    If percentage over < 20%:
        Salary Match = 80%
    Else if percentage over < 50%:
        Salary Match = 50%
    Else:
        Salary Match = 10% (significant mismatch)

Else if user_expected_salary < job_min_salary:
    User might be overqualified or have wrong expectations
    Salary Match = 100% (favorable to user)
```

**Example**:

```
Job salary: $120,000 - $160,000

User A (expected $140,000): 100% match
User B (expected $100,000): 100% match (less than min is OK)
User C (expected $180,000): 80% match (12.5% over max)
User D (expected $250,000): 10% match (56% over max)
```

---

## Scoring Algorithm

### Overall Match Score Formula

```
Total Match Score = (Skill Match × 0.40) +
                    (Experience Match × 0.30) +
                    (Location Match × 0.15) +
                    (Salary Match × 0.10) +
                    (Bonus Factors × 0.05)

Bonus Factors (5%):
- Cultural fit indicators
- Company industry match
- Job type match (full-time preferred)
```

### Match Score Interpretation

| Score  | Category  | Recommendation      |
| ------ | --------- | ------------------- |
| 85-100 | Excellent | Highly recommended  |
| 70-84  | Good      | Recommended         |
| 55-69  | Fair      | Consider applying   |
| 40-54  | Poor      | May need upskilling |
| <40    | Weak      | Not recommended     |

### Mandatory Skills Filtering

**Important**: If job has mandatory skills and user is missing them:

```
If user missing ANY mandatory skill:
    Overall Match Score = 0 (or flag as not qualified)

Else:
    Calculate normal match score
```

**Example**:

```
Job requires: Java (MANDATORY), Spring Boot (optional)
User has: Spring Boot, Python

Result: 0% match (missing mandatory Java skill)
```

---

## Implementation Logic

### Java Service Implementation

```java
@Service
@Transactional
public class MatchingService {

    @Autowired
    private UserSkillRepository userSkillRepo;

    @Autowired
    private JobSkillRepository jobSkillRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JobRepository jobRepo;

    /**
     * Calculate comprehensive match score between user and job
     */
    public MatchScore calculateMatchScore(Long userId, Long jobId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Job job = jobRepo.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Check mandatory skills first
        if (!hasMandatorySkills(user, job)) {
            return MatchScore.builder()
                .overallScore(0.0)
                .skillMatch(0.0)
                .reason("Missing mandatory skills")
                .qualified(false)
                .build();
        }

        // Calculate individual scores
        double skillMatch = calculateSkillMatch(user, job);
        double experienceMatch = calculateExperienceMatch(user, job);
        double locationMatch = calculateLocationMatch(user, job);
        double salaryMatch = calculateSalaryMatch(user, job);
        double bonusScore = calculateBonusScore(user, job);

        // Calculate weighted overall score
        double overallScore = (skillMatch * 0.40) +
                            (experienceMatch * 0.30) +
                            (locationMatch * 0.15) +
                            (salaryMatch * 0.10) +
                            (bonusScore * 0.05);

        return MatchScore.builder()
            .overallScore(Math.round(overallScore * 100.0) / 100.0)
            .skillMatch(Math.round(skillMatch * 100.0) / 100.0)
            .experienceMatch(Math.round(experienceMatch * 100.0) / 100.0)
            .locationMatch(Math.round(locationMatch * 100.0) / 100.0)
            .salaryMatch(Math.round(salaryMatch * 100.0) / 100.0)
            .qualified(overallScore >= 0.55)
            .reason(generateReason(overallScore, skillMatch, experienceMatch))
            .build();
    }

    /**
     * Check if user has all mandatory skills
     */
    private boolean hasMandatorySkills(User user, Job job) {
        List<JobSkill> mandatorySkills = job.getJobSkills().stream()
            .filter(JobSkill::isMandatory)
            .collect(Collectors.toList());

        List<Long> userSkillIds = user.getUserSkills().stream()
            .map(us -> us.getSkill().getId())
            .collect(Collectors.toList());

        return mandatorySkills.stream()
            .allMatch(ms -> userSkillIds.contains(ms.getSkill().getId()));
    }

    /**
     * Calculate skill match percentage
     */
    private double calculateSkillMatch(User user, Job job) {
        List<JobSkill> requiredSkills = job.getJobSkills();
        if (requiredSkills.isEmpty()) {
            return 1.0; // 100% if no skills required
        }

        double matchedCount = 0.0;

        for (JobSkill jobSkill : requiredSkills) {
            Optional<UserSkill> userSkill = user.getUserSkills().stream()
                .filter(us -> us.getSkill().getId().equals(jobSkill.getSkill().getId()))
                .findFirst();

            if (userSkill.isPresent()) {
                // User has the skill
                ProficiencyLevel userLevel = userSkill.get().getProficiencyLevel();
                ProficiencyLevel requiredLevel = jobSkill.getProficiencyLevel();

                // Full match if user level >= required level
                if (userLevel.compareTo(requiredLevel) >= 0) {
                    matchedCount += 1.0;
                } else {
                    // Partial match if user has skill but lower proficiency
                    matchedCount += 0.5;
                }
            }
        }

        return matchedCount / requiredSkills.size();
    }

    /**
     * Calculate experience match percentage
     */
    private double calculateExperienceMatch(User user, Job job) {
        int userExperience = user.getYearsOfExperience() != null ?
            user.getYearsOfExperience() : 0;
        int requiredExperience = job.getYearsRequired();

        // Get job level requirements
        ExperienceLevel jobLevel = job.getExperienceLevel();
        ExperienceLevel userLevel = getUserExperienceLevel(userExperience);

        if (userLevel.getValue() >= jobLevel.getValue()) {
            return 1.0; // 100%
        } else if (userLevel.getValue() == jobLevel.getValue() - 1) {
            return 0.80; // 80% - one level below
        } else if (userLevel.getValue() == jobLevel.getValue() - 2) {
            return 0.50; // 50% - two levels below
        } else {
            return 0.20; // 20% - significant gap
        }
    }

    /**
     * Get user's experience level based on years
     */
    private ExperienceLevel getUserExperienceLevel(int years) {
        if (years < 1) return ExperienceLevel.ENTRY;
        if (years < 5) return ExperienceLevel.MID;
        if (years < 10) return ExperienceLevel.SENIOR;
        return ExperienceLevel.LEAD;
    }

    /**
     * Calculate location match percentage
     */
    private double calculateLocationMatch(User user, Job job) {
        if (job.isRemote()) {
            return 1.0; // 100% - any location works
        }

        String userLocation = user.getLocation();
        String jobLocation = job.getLocation();

        // Exact match
        if (userLocation.equalsIgnoreCase(jobLocation)) {
            return 1.0;
        }

        // Extract city/state for comparison
        String[] userParts = userLocation.split(",");
        String[] jobParts = jobLocation.split(",");

        if (userParts.length > 0 && jobParts.length > 0) {
            String userCity = userParts[0].trim();
            String jobCity = jobParts[0].trim();
            String userState = userParts.length > 1 ? userParts[1].trim() : "";
            String jobState = jobParts.length > 1 ? jobParts[1].trim() : "";

            // Same metro area (simplified check)
            if (isMetroArea(userCity, jobCity)) {
                return 0.80;
            }

            // Same state/country
            if (userState.equalsIgnoreCase(jobState)) {
                return 0.50;
            }

            // Different region
            return 0.30;
        }

        return 0.30;
    }

    /**
     * Check if cities are in same metro area
     */
    private boolean isMetroArea(String city1, String city2) {
        // Simplified - in production, use actual metro area data
        Map<String, List<String>> metros = Map.of(
            "Bay Area", Arrays.asList("San Francisco", "Oakland", "San Jose", "Palo Alto"),
            "LA Area", Arrays.asList("Los Angeles", "Pasadena", "Long Beach", "Santa Monica"),
            "NYC Area", Arrays.asList("New York", "Brooklyn", "Queens", "Jersey City")
        );

        for (List<String> metroList : metros.values()) {
            if (metroList.contains(city1) && metroList.contains(city2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate salary match percentage
     */
    private double calculateSalaryMatch(User user, Job job) {
        // If user hasn't set expected salary, assume flexible
        if (user.getSalaryExpectation() == null) {
            return 1.0;
        }

        BigDecimal userExpected = user.getSalaryExpectation();
        BigDecimal jobMin = job.getSalaryMin();
        BigDecimal jobMax = job.getSalaryMax();

        // Within range
        if (userExpected.compareTo(jobMin) >= 0 && userExpected.compareTo(jobMax) <= 0) {
            return 1.0;
        }

        // Below minimum (favorable to employer)
        if (userExpected.compareTo(jobMin) < 0) {
            return 1.0;
        }

        // Above maximum
        BigDecimal percentageOver = userExpected.subtract(jobMax)
            .divide(jobMax, 2, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal(100));

        if (percentageOver.compareTo(new BigDecimal(20)) < 0) {
            return 0.80;
        } else if (percentageOver.compareTo(new BigDecimal(50)) < 0) {
            return 0.50;
        } else {
            return 0.10;
        }
    }

    /**
     * Calculate bonus factors (culture, industry, job type)
     */
    private double calculateBonusScore(User user, Job job) {
        double bonus = 0.0;

        // Job type preference
        if (job.getJobType() == JobType.FULL_TIME) {
            bonus += 0.5;
        }

        return Math.min(bonus, 1.0); // Cap at 100%
    }

    /**
     * Generate human-readable reason for match score
     */
    private String generateReason(double overall, double skill, double experience) {
        if (overall >= 0.85) {
            return "Excellent match! Strong skills and experience alignment";
        } else if (overall >= 0.70) {
            return "Good match. " + (skill < 0.50 ? "Consider upskilling in required areas." : "");
        } else if (overall >= 0.55) {
            return "Fair match. You have " + (int)(skill*100) + "% of required skills and " + (int)(experience*100) + "% experience match.";
        } else {
            return "Match needs improvement. Focus on skill gaps and experience.";
        }
    }
}

/**
 * Match Score DTO
 */
@Data
@Builder
public class MatchScore {
    private Double overallScore;      // 0-100%
    private Double skillMatch;        // 0-100%
    private Double experienceMatch;   // 0-100%
    private Double locationMatch;     // 0-100%
    private Double salaryMatch;       // 0-100%
    private Boolean qualified;        // >= 55% score
    private String reason;            // Human-readable explanation
    private List<String> recommendations; // Actionable steps to improve
}
```

---

## Recommendations Engine

### Job Recommendations for Users

**Query Logic**:

```sql
-- Find top 20 jobs matching user's skills and experience
SELECT j.* FROM jobs j
JOIN job_skills js ON j.id = js.job_id
WHERE j.status = 'PUBLISHED'
  AND j.closing_date > NOW()
  AND (
    -- User has skill match
    EXISTS (
      SELECT 1 FROM user_skills us
      WHERE us.user_id = ?
        AND us.skill_id = js.skill_id
    )
    OR js.is_mandatory = FALSE -- Non-mandatory skills are flexible
  )
  AND j.experience_level <= ? -- User's level or higher
ORDER BY (
  -- Calculate match score (simplified in DB)
  CASE
    WHEN j.location = ? THEN 100
    WHEN j.is_remote THEN 100
    ELSE 50
  END
) DESC
LIMIT 20;
```

**Backend Logic**:

```java
@Service
public class RecommendationService {

    @Autowired
    private JobRepository jobRepo;

    @Autowired
    private MatchingService matchingService;

    /**
     * Get top 20 recommended jobs for user
     */
    public List<JobRecommendation> getRecommendedJobs(Long userId, int limit) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get published, open jobs
        List<Job> jobs = jobRepo.findPublishedOpenJobs(
            user.getExperienceLevel(),
            PageRequest.of(0, limit * 3) // Get more to filter
        );

        // Calculate match scores
        List<JobRecommendation> recommendations = jobs.stream()
            .map(job -> {
                MatchScore score = matchingService.calculateMatchScore(userId, job.getId());
                return JobRecommendation.builder()
                    .jobId(job.getId())
                    .jobTitle(job.getTitle())
                    .companyName(job.getCompany().getName())
                    .matchScore(score.getOverallScore())
                    .skillMatch(score.getSkillMatch())
                    .experienceMatch(score.getExperienceMatch())
                    .reason(score.getReason())
                    .build();
            })
            .filter(r -> r.getMatchScore() >= 0.55) // Only qualified matches
            .sorted(Comparator.comparingDouble(JobRecommendation::getMatchScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());

        return recommendations;
    }
}
```

### Candidate Recommendations for Companies

Similar logic, but:

1. Find users matching job requirements
2. Calculate user-to-job match score
3. Filter by skill match >= 70%
4. Sort by overall score descending

---

## Future ML Enhancements

### Phase 2: Machine Learning Features

```
1. Resume Parsing (AI):
   - Extract skills from PDF automatically
   - Parse education history
   - Identify experience gaps

2. Skill Similarity:
   - "Java" is similar to "Kotlin", "Scala"
   - Distance metrics for skill recommendations

3. Job-User Clustering:
   - Group similar jobs
   - Identify job patterns for users
   - Collaborative filtering

4. Salary Prediction:
   - ML model: (location, skills, experience) → salary range
   - Help users set realistic expectations

5. Success Prediction:
   - Predict if application will be successful
   - Based on historical data
```

### Vector Embeddings (Future)

```python
# Using embeddings for semantic search
from sentence_transformers import SentenceTransformer

model = SentenceTransformer('all-MiniLM-L6-v2')

# Embed job description
job_embedding = model.encode(job.description)

# Embed resume
resume_embedding = model.encode(user.cv_text)

# Calculate semantic similarity (cosine distance)
similarity = cosine_similarity(job_embedding, resume_embedding)
```
