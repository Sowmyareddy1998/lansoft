package ls.lesm.service;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import ls.lesm.model.recruiter.CandidateProfiles;
import ls.lesm.model.recruiter.JobString;
import ls.lesm.model.recruiter.RecruiterProfitOrLoss;
import ls.lesm.payload.request.ApproveProfilesRequest;
import ls.lesm.payload.request.CandidateStatusAndConsultantRequest;
import ls.lesm.payload.response.RecruiterDropDownResponse;

public interface RecruiterService {
	
	String postJobString(JobString jobString, Principal principal, Set<Integer> empIds,MultipartFile file);
	
	void closeTicketJob();
	
	List<JobString> getOpenedJobStringByLoggedInEmp(Principal principal);
	
	String sendProfiles(CandidateProfiles profiles, Integer jobStringId, Principal principal,MultipartFile resume);
	
	List<RecruiterDropDownResponse> recruitersDropDown(Principal principal);
	
	void managerProfileApproval(List<ApproveProfilesRequest> req);

	CandidateStatusAndConsultantRequest scheduleInterview(CandidateStatusAndConsultantRequest schedule,String candidateId,Principal principal);
	
	List<RecruiterProfitOrLoss> getRecruiterProfitOrLoss(Principal principal);
    

}
