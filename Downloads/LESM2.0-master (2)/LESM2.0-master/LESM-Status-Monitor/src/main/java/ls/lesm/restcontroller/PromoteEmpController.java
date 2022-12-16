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

	@GetMapping("/PromoteEmpDetails/{Emp_Id}/{Super_Id1}/{Super_Id2}/{salary}")
    public ResponseEntity<String> promoteEmp(@PathVariable("Emp_Id") String emp, @PathVariable("Super_Id1")String superId, @PathVariable("Super_Id2") String secmanager

            ,@PathVariable double  salary,Principal principal) {

 

        promoteEmpServiceImp.promoteEmployeeDetailss(emp,superId,secmanager, salary,principal );

 

        return new ResponseEntity<String>("Promoted successfully", HttpStatus.CREATED);

 

    }



    @GetMapping("/ReportsToPrimary/{lanceId}")
    List<MasterEmployeeDetails> primaryReporting(@PathVariable String lanceId)
    {

    return promoteEmpServiceImp.primary(lanceId);

 

          
    }


    @GetMapping("/ReportsToSecondary/{lanceId}/{primaryId}")
    List<MasterEmployeeDetails> secondReporting(@PathVariable String lanceId, @PathVariable("primaryId") String Sub_id1)
    {


        return promoteEmpServiceImp.secondaryreportstoDashboard(lanceId,Sub_id1);

 

          
    }
}


