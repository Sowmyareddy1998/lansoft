package ls.lesm.repository.recruiter;

import org.springframework.data.jpa.repository.JpaRepository;

import ls.lesm.model.recruiter.CandidateProfiles;

public interface CandidateProfilesRepo extends JpaRepository<CandidateProfiles, String> {
	

}
