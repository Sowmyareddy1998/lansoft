package ls.lesm.restcontroller;



import java.security.Principal;
import java.time.LocalDate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ls.lesm.model.Designations;
import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.model.Salary;

import ls.lesm.service.impl.PromoteEmpServiceImp;

@Component
@RestController
@CrossOrigin("*")
public class PromoteEmpController {

	@Autowired
	PromoteEmpServiceImp promoteEmpServiceImp;

	@GetMapping("/getall")
	public List<MasterEmployeeDetails> getEmpDetails() {
		return promoteEmpServiceImp.getEmp();
	}

	@GetMapping("/getSameDesignationEmployees/{id}")

	List<MasterEmployeeDetails> getSameDesignationEmployees(@PathVariable String id) {

		List<MasterEmployeeDetails> EmployeeDetails = promoteEmpServiceImp.getSameDesignations(id);

		return EmployeeDetails;
	}

	@GetMapping("/setSupervisor/{id}/{id2}")
	ResponseEntity<String> promoteEmployeeDetail(@PathVariable String id, @PathVariable String id2) {
		promoteEmpServiceImp.promoteEmployeeDetails(id, id2);
		return new ResponseEntity<String>("assigned successfully", HttpStatus.ACCEPTED);

	}

	@GetMapping("/getNewSuperVisor/{id}")
	public List<MasterEmployeeDetails> getnewSuperVisor(@PathVariable String id) {

		return promoteEmpServiceImp.getnewSuperVisor(id);
	}

	@GetMapping("/update/{emp_id}/{sub_id}/{newSalary}")
	public ResponseEntity<String> promoteEmp(@PathVariable("emp_id") String emp, @PathVariable("sub_id")String superviserId,
			@PathVariable Double newSalary,Principal principal) {

		promoteEmpServiceImp.promoteEmployeeDetailss(emp, superviserId, newSalary,principal );

		return new ResponseEntity<String>("Promoted", HttpStatus.CREATED);

	}

//	    @PutMapping("/update1")
//	    public String up(@RequestBody Salary salary) {
//	        promoteEmpServiceImp.update(salary);
//	        
//	        return "update";
//	        
//	    }

}


