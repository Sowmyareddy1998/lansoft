package ls.lesm.repository.recruiter;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.model.recruiter.JobString;

public interface JobStringRepo extends JpaRepository<JobString, Integer> {

	List<JobString> findAllByTicketStatus(boolean b);

	List<JobString> findAllByMasterEmployeeDetailsAndTicketStatus(MasterEmployeeDetails loggedInEmp, boolean b);

	//void findAllByMasterEmployeeDetails();

}
