package ls.lesm.restcontroller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ls.lesm.model.Clients;
import ls.lesm.model.EmployeesAtClientsDetails;
import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.payload.request.UpdateClientDetialsDropDown;
import ls.lesm.payload.response.ConsultantDropDownRes;
import ls.lesm.payload.response.RecruiterDropDownResponse;
import ls.lesm.repository.ClientsRepository;
import ls.lesm.repository.EmployeesAtClientsDetailsRepository;
import ls.lesm.repository.MasterEmployeeDetailsRepository;
import ls.lesm.service.impl.EmployeeDetailsServiceImpl;
import ls.lesm.service.impl.RecruiterServiceImpl;

@RestController
@RequestMapping("/api/v1/drop-down")
@CrossOrigin("*")
public class DropDownController {
	
	@Autowired
	private RecruiterServiceImpl recruiterServiceImpl;
	
	@Autowired
	private MasterEmployeeDetailsRepository masterEmployeeDetailsRepository;
	
	@Autowired
	private ClientsRepository clientRepo;
	
	@Autowired
	private EmployeesAtClientsDetailsRepository employeesAtClientsDetailsRepository;
	
	@Autowired
	private EmployeeDetailsServiceImpl employeeDetailsServiceImpl;
	
	@GetMapping("/rec")
	public ResponseEntity<List<RecruiterDropDownResponse>> temp(Principal principal) {
		
	
		return new ResponseEntity<List<RecruiterDropDownResponse>>(this.recruiterServiceImpl.recruitersDropDown(principal), HttpStatus.OK);
	}
	
	@GetMapping("/employee-clients")
	public ResponseEntity<List<UpdateClientDetialsDropDown>> empClientDropDown(@RequestParam int empId){
		
		Optional<MasterEmployeeDetails> emp=this.masterEmployeeDetailsRepository.findById(empId);
		List<EmployeesAtClientsDetails> clientList=this.employeesAtClientsDetailsRepository.findByMasterEmployeeDetails(emp.get());
	//	System.out.print("==="+clientList);
		
		List<UpdateClientDetialsDropDown> list=new ArrayList<UpdateClientDetialsDropDown>();

		for(EmployeesAtClientsDetails c: clientList) {
		UpdateClientDetialsDropDown dropD=new UpdateClientDetialsDropDown();
		dropD.setClientId(c.getEmpAtClientId());
		dropD.setClientName(c.getClients().getClientsNames());
		dropD.setPoEDate(c.getPOEdate());
		dropD.setPoSDate(c.getPOSdate());
		list.add(dropD);
		}
	
		//System.out.print("===================");
		return new ResponseEntity<List<UpdateClientDetialsDropDown>>(list,HttpStatus.OK);


	}
	
	@GetMapping("/clients-name")
	public ResponseEntity<List<Clients>> getClientNameByNameKey(@RequestParam String name){
		List<Clients> clients=this.clientRepo.findByClientsNamesStartsWith(name);
		return new ResponseEntity<List<Clients>>(clients,HttpStatus.OK);
	}
	
	@GetMapping("/consultants")
	public ResponseEntity<List<ConsultantDropDownRes>> getAllCons(@RequestParam String lancesoftId, @RequestParam int subDId){
		
		
		return new ResponseEntity<List<ConsultantDropDownRes>>(this.employeeDetailsServiceImpl.getAllConsByPractice(subDId, lancesoftId),HttpStatus.OK);
	}

}
