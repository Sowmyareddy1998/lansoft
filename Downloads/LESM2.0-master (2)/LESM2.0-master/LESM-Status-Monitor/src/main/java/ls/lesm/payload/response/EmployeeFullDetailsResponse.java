package ls.lesm.payload.response;

import java.util.List;
import java.util.Set;

import com.amazonaws.services.ec2.model.Address;

import lombok.Data;
import ls.lesm.model.EmployeesAtClientsDetails;
import ls.lesm.model.InternalExpenses;
@Data
public class EmployeeFullDetailsResponse {
	
	private EmployeeDetailsResponse detailsResponse;
	private Double salary;
	private List<InternalExpenses> internalExpenses;
	private List<EmployeesAtClientsDetails> employeeAtClientsDetails;
	private List<ls.lesm.model.Address> addres;

}
