package ls.lesm.service;

import java.util.List;

import ls.lesm.model.Designations;
import ls.lesm.payload.response.ConsultantDropDownRes;
import ls.lesm.payload.response.DesignatinsResponse;
import ls.lesm.payload.response.ReportToDropDownRes;

public interface DropDownService {
	
	
	List<ConsultantDropDownRes> getAllConsByPractice(int subDId, String keyword);

	List<ReportToDropDownRes> getEmployeeByDesigAndSearch(int desgId, String keyword);
	
	List<Designations> getAllDesignationsEqualOrAbove(int desigId);
	
	List<Designations> getAboveAllDesignations(int desigId);
	
	List<DesignatinsResponse> getAllDesignationExceptHR();

}
