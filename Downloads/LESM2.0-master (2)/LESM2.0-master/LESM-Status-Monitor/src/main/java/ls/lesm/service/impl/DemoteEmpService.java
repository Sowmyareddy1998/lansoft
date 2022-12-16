package ls.lesm.service.impl;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ls.lesm.exception.RecordNotFoundException;
import ls.lesm.exception.UserNameNotFoundException;
import ls.lesm.model.Designations;
import ls.lesm.model.EmployeeStatus;
import ls.lesm.model.History;
import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.model.Salary;
import ls.lesm.model.SecondaryManager;
import ls.lesm.model.UpdatedStatus;
import ls.lesm.model.User;
import ls.lesm.repository.DesignationsRepository;
import ls.lesm.repository.HistoryRepository;
import ls.lesm.repository.MasterEmployeeDetailsRepository;
import ls.lesm.repository.SalaryRepository;
import ls.lesm.repository.SecondaryManagerRepository;
import ls.lesm.repository.UserRepository;

@Service
public class DemoteEmpService {

	@Autowired
	MasterEmployeeDetailsRepository masterEmployeeDetailsRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	HistoryRepository historyRepository;

	@Autowired
	SalaryRepository salaryRepository;

	@Autowired
	SecondaryManagerRepository subordinateManagerRepository;

	@Autowired
	DesignationsRepository designationsRepository;

	// get the list of designations
	public List<Designations> getEmp() {

		List<Designations> desg = designationsRepository.findAll();
		List<Designations> filter = new ArrayList<>();

		if (desg == null) {
			throw new RecordNotFoundException("records not found");
		}
		for (Designations d : desg) {
			if (!d.getDesgNames().toUpperCase().equals("CONSULTANT")
					&& !d.getDesgNames().toUpperCase().equals("SUPER ADMIN")) {
				filter.add(d);
			}
		}
		return filter;
	}

	// get the list of employees by desgId

	public List<MasterEmployeeDetails> getAllDesignateEmployes(int id) {
		Optional<Designations> desgnations = designationsRepository.findById(id);
		List<MasterEmployeeDetails> filter1 = new ArrayList<>();
		List<MasterEmployeeDetails> med = masterEmployeeDetailsRepository
				.findByAlldesgIdWithNames(desgnations.get().getDesgId());
		if (med == null) {
			throw new UserNameNotFoundException("Designations are not found");
		}
		System.err.println(med);
		for (MasterEmployeeDetails m1 : med) {
			if ((m1.getStatus() != EmployeeStatus.EXIT) && (m1.getStatus() != EmployeeStatus.ABSCOND)
					&& (m1.getStatus() != EmployeeStatus.TERMINATE)) {
				filter1.add(m1);
			}
		}
		if (filter1.isEmpty()) {
			throw new UserNameNotFoundException("data not found");
		}
		return filter1;

	}

	// add Primary Supervisor

	public List<MasterEmployeeDetails> addSupervisor1lan(String lanceId) {

		Designations desg = masterEmployeeDetailsRepository.findByLancesoft(lanceId).getDesignations().getDesignations();


		List<MasterEmployeeDetails> finallist = new ArrayList<>();

		for (; desg.getDesgId() != 1; desg = desg.getDesignations()) {
			List<MasterEmployeeDetails> employess = masterEmployeeDetailsRepository
					.findBydesignations_Id(desg.getDesgId());

			for (MasterEmployeeDetails m1 : employess) {

				if ((m1.getStatus() == EmployeeStatus.EXIT) || (m1.getStatus() == EmployeeStatus.ABSCOND)
						|| (m1.getStatus() == EmployeeStatus.TERMINATE)) {

				} else {
					finallist.add(m1);
				}

			}

		}

		finallist.remove(masterEmployeeDetailsRepository.findByLancesoft(lanceId));

		// System.out.println(finallist);

		return finallist;

	}

	// add Second Supervisor

	public List<MasterEmployeeDetails> addSecondSupervisor1(String empId, String superId) {

		List<MasterEmployeeDetails> finallist = new ArrayList<>();

		if (empId != null || superId != null) {
			try {
				Designations desg = masterEmployeeDetailsRepository.findByLancesoft(empId).getDesignations();

				for (; desg.getDesgId() != 1; desg = desg.getDesignations()) {

					List<MasterEmployeeDetails> employess = masterEmployeeDetailsRepository
							.findBydesignations_Id(desg.getDesgId());

					for (MasterEmployeeDetails m1 : employess) {

						if ((m1.getStatus() == EmployeeStatus.EXIT) || (m1.getStatus() == EmployeeStatus.ABSCOND)
								|| (m1.getStatus() == EmployeeStatus.TERMINATE)) {

						} else {
							finallist.add(m1);
						}

					}

				}

			} catch (Exception e) {
				// TODO: handle exception
			}
			finallist.remove(masterEmployeeDetailsRepository.findByLancesoft(empId));
			finallist.remove(masterEmployeeDetailsRepository.findByLancesoft(superId));

			// System.out.println(finallist);

			return finallist;
		} else {

			return null;
		}
	}

	// submit
	public ResponseEntity<String> demote1(String emp, String superId1, String superId2, double salary,
			Principal principal) {

		User u = userRepository.findByUsername(principal.getName());
		String s = u.getUsername();

		MasterEmployeeDetails upadtedBy = masterEmployeeDetailsRepository.findByLancesoft(s);

		MasterEmployeeDetails med = masterEmployeeDetailsRepository.findByLancesoft(emp);

		if (med != null) {

			User u1 = userRepository.findByUsername(med.getLancesoft());

			if (u1 != null) {
				userRepository.delete(u1);
			}

			History hs = new History(med.getLancesoft(), med.getFirstName(), med.getLastName(), med.getJoiningDate(),
					med.getDOB(), med.getLocation(), med.getGender(), med.getEmail(), med.getCreatedAt(),
					med.getVertical(), med.getStatus(), med.getAge(), med.getIsInternal(), med.getPhoneNo(),
					med.getCreatedBy(), med.getSubDepartments(), med.getDepartments(), med.getDesignations(),
					med.getSupervisor(), med.getEmployeeType(), UpdatedStatus.DEMOTE, LocalDate.now(), upadtedBy);
			historyRepository.save(hs);

			MasterEmployeeDetails sup = masterEmployeeDetailsRepository.findByLancesoft(superId1);
			med.setSupervisor(sup);

			for (Designations d : designationsRepository.findAll()) {

				if (med.getDesignations() == d.getDesignations())

				{
					
					med.setDesignations(d);
					break;
				}

			}
			
			
			masterEmployeeDetailsRepository.save(med);

			if (superId2 != null) {
				int empId = med.getEmpId();

				MasterEmployeeDetails sup2 = masterEmployeeDetailsRepository.findByLancesoft(superId2);
                
				if(sup2!=null) {
				
					SecondaryManager sub = subordinateManagerRepository.findByEmployee(empId);// get employee in
					
					if (sub != null) {// subordinate table
						sub.setSecondaryManager(sup2);

						subordinateManagerRepository.save(sub);
					}
				

			}

			if (salary != 0) {

				Salary s2 = new Salary();
				s2.setSalary(salary);
				s2.setUpdatedAt(LocalDate.now());
				s2.setMasterEmployeeDetails(med);
				s2.setCreatedBy(med.getCreatedBy());
				s2.setCreatedAt(med.getCreatedAt());
				System.err.println(s2);

				salaryRepository.save(s2);

			}

			

		} 
			return new ResponseEntity("DEMOTED", HttpStatus.ACCEPTED);
		}
		
	else {
		return new ResponseEntity("Data not found with id :" + emp, HttpStatus.BAD_REQUEST);
	      }
		}
	}

