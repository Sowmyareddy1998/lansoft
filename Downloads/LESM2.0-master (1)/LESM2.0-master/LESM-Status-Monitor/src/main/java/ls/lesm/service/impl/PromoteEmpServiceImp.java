package ls.lesm.service.impl;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.twilio.rest.api.v2010.account.Call.UpdateStatus;

import ls.lesm.exception.RecordNotFoundException;
import ls.lesm.exception.UserNameNotFoundException;
import ls.lesm.model.Departments;
import ls.lesm.model.Designations;
import ls.lesm.model.EmployeeStatus;
import ls.lesm.model.EmployeeType;
import ls.lesm.model.History;
import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.model.Salary;
import ls.lesm.model.SubDepartments;
import ls.lesm.model.UpdatedStatus;
import ls.lesm.model.User;
import ls.lesm.repository.DesignationsRepository;
import ls.lesm.repository.HistoryRepository;
import ls.lesm.repository.MasterEmployeeDetailsRepository;
import ls.lesm.repository.SalaryRepository;
import ls.lesm.repository.UserRepository;

@Service
public class PromoteEmpServiceImp {

	@Autowired
	MasterEmployeeDetailsRepository masterEmployeeDetailsRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Autowired
	private DesignationsRepository designationsRepository;

	@Autowired
	private SalaryRepository salaryRepository;

	public List<MasterEmployeeDetails> getEmp() {

		List<MasterEmployeeDetails> masterEmp = masterEmployeeDetailsRepository.findAll();
		if (masterEmp.isEmpty()) {
			throw new RecordNotFoundException("No records are found");
		} else {

			List<MasterEmployeeDetails> filter = new ArrayList<MasterEmployeeDetails>();

			for (MasterEmployeeDetails m : masterEmp) {
				if (m.getStatus() != EmployeeStatus.EXIT) {
					filter.add(m);
				}

			}
			return filter;
		}
	}

	public List<MasterEmployeeDetails> getSameDesignations(String emp) {

		MasterEmployeeDetails em = masterEmployeeDetailsRepository.findByLancesoft(emp);

		MasterEmployeeDetails employee = masterEmployeeDetailsRepository.findById(em.getEmpId()).get();

		if (employee == null) {
			throw new UserNameNotFoundException("Employee with that username not found");
		}

		int desg = employee.getDesignations().getDesgId();

		masterEmployeeDetailsRepository.findBydesignations_Id(desg);

		return masterEmployeeDetailsRepository.findBydesignations_Id(desg);
	}

	public String promoteEmployeeDetails(String oldsuperviserId, String newsuperviserId) {
		MasterEmployeeDetails oldsuperviser = masterEmployeeDetailsRepository.findByLancesoft(oldsuperviserId);
		MasterEmployeeDetails newsuperviser = masterEmployeeDetailsRepository.findByLancesoft(newsuperviserId);

		List<MasterEmployeeDetails> g = masterEmployeeDetailsRepository
				.findBymasterEmployeeDetails_Id(oldsuperviser.getEmpId());
		MasterEmployeeDetails newemployee = masterEmployeeDetailsRepository.findById(newsuperviser.getEmpId()).get();

		for (MasterEmployeeDetails m : g) {
			System.out.println(m);

			m.setSupervisor(newemployee);

			masterEmployeeDetailsRepository.save(m);

		}
		return "success";
	}

	public synchronized void promoteEmployeeDetailss(String emp, String superviserId, Double newSalary,
			Principal principal) {

		User loggedU = this.userRepository.findByUsername(principal.getName());

		String id = loggedU.getUsername();
		MasterEmployeeDetails updatedBy = this.masterEmployeeDetailsRepository.findByLancesoft(id);
		MasterEmployeeDetails employee = this.masterEmployeeDetailsRepository.findByLancesoft(emp);

		MasterEmployeeDetails trasferEmp = masterEmployeeDetailsRepository.findById(employee.getEmpId()).get();

		History historyEmp = new History(trasferEmp.getLancesoft(), trasferEmp.getFirstName(), trasferEmp.getLastName(),
				trasferEmp.getJoiningDate(), trasferEmp.getDOB(), trasferEmp.getLocation(), trasferEmp.getGender(),
				trasferEmp.getEmail(), trasferEmp.getCreatedAt(), trasferEmp.getVertical(), trasferEmp.getStatus(),
				trasferEmp.getAge(), trasferEmp.getIsInternal(), trasferEmp.getPhoneNo(), trasferEmp.getCreatedBy(),
				trasferEmp.getSubDepartments(), trasferEmp.getDepartments(), trasferEmp.getDesignations(),
				trasferEmp.getSupervisor(), trasferEmp.getEmployeeType(), UpdatedStatus.PROMOTE, LocalDate.now(),
				updatedBy);

		historyRepository.save(historyEmp);
		MasterEmployeeDetails employee1 = this.masterEmployeeDetailsRepository.findByLancesoft(superviserId);

		MasterEmployeeDetails DetailsSupervisor = masterEmployeeDetailsRepository.findById(employee1.getEmpId()).get();
		
		
		

		trasferEmp.setDesignations(trasferEmp.getDesignations().getDesignations());

	
		trasferEmp.setSupervisor(DetailsSupervisor);

		trasferEmp.setCreatedAt(LocalDate.now());
		masterEmployeeDetailsRepository.save(trasferEmp);
		if(newSalary!=0)
		{
			

		List<Salary> salrec = salaryRepository.findsBymasterEmployeeDetails_Id(trasferEmp.getEmpId());

		for (Salary sall : salrec) {

			// Salary sal =
			// salaryRepository.findBymasterEmployeeDetails_Id(trasferEmp.getEmpId()).get();

			
			salaryRepository
					.save(new Salary(newSalary, sall.getCreatedAt(), LocalDate.now(), sall.getCreatedBy(), trasferEmp));
			break;
		}
		
//	User u=	userRepository.findByUsername(trasferEmp.getLancesoft());
//	
//	if(u!=null)
//	{
//		userRepository.delete(u);
//		
//	}
		
		
		
		}	
		
		User u=	userRepository.findByUsername(trasferEmp.getLancesoft());
		
		if(u!=null)
		{
			userRepository.delete(u);
			
		}

	}

	public synchronized List<MasterEmployeeDetails> getnewSuperVisor(String id) {
		MasterEmployeeDetails employee = masterEmployeeDetailsRepository.findByLancesoft(id);

		MasterEmployeeDetails masterEmployeeDetails = masterEmployeeDetailsRepository.findById(employee.getEmpId())
				.get();

		int desg = masterEmployeeDetails.getSupervisor().getSupervisor().getDesignations().getDesgId();
System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+desg);
		return masterEmployeeDetailsRepository.findBydesignations_Id(desg);

	}

}
