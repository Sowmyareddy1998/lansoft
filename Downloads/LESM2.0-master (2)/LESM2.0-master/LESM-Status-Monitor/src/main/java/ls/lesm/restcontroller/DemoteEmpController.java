package ls.lesm.restcontroller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ls.lesm.model.Designations;
import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.repository.DesignationsRepository;
import ls.lesm.repository.HistoryRepository;
import ls.lesm.repository.MasterEmployeeDetailsRepository;
import ls.lesm.repository.SalaryRepository;
import ls.lesm.repository.UserRepository;
import ls.lesm.service.impl.DemoteEmpService;

@RestController
@CrossOrigin("*")
public class DemoteEmpController {
	@Autowired
	DesignationsRepository designationsRepository;
	@Autowired
	MasterEmployeeDetailsRepository masterEmployeeDetailsRepository;
	@Autowired
	HistoryRepository historyRepository;

	@Autowired
	DemoteEmpService demoteEmpService;

	@Autowired
	UserRepository userRepository;
	@Autowired
	SalaryRepository salaryRepository;

	@GetMapping("/getAllDemoteDesignations")
	List<Designations> getEmployees() {

		return demoteEmpService.getEmp();
	}

	@PostMapping("/getAlldesignationEmployees/{id}")
	List<MasterEmployeeDetails> getAllDesignateEmployees(@PathVariable int id) {

		return demoteEmpService.getAllDesignateEmployes(id);

	}

	@GetMapping("/addSupervisor/{lanceId}")
	List<MasterEmployeeDetails> addSupervisor(@PathVariable String lanceId) {

		return demoteEmpService.addSupervisor1lan(lanceId);

	}

	@GetMapping("/addSecondSupervisor")
	List<MasterEmployeeDetails> addSecondSupervisor(@RequestParam(required = false, defaultValue = "null") String empId,
			@RequestParam(required = false, defaultValue = "null") String superId) {

		return demoteEmpService.addSecondSupervisor1(empId, superId);
	}

	@GetMapping("/demote/{emp_Id}/{super_Id1}/{super_Id2}/{Salary}")
	ResponseEntity<String> demote(@PathVariable("emp_Id") String emp,

			@PathVariable("super_Id1") String superId1, @PathVariable("super_Id2") String superId2,
			@PathVariable("Salary") double salary, Principal principal)

	{
		

		return demoteEmpService.demote1(emp, superId1, superId2, salary, principal);

	}
}
