package ls.lesm.service.impl;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ls.lesm.exception.DateMissMatchException;
import ls.lesm.exception.DuplicateEntryException;
import ls.lesm.model.Address;
import ls.lesm.model.Departments;
import ls.lesm.model.EmployeesAtClientsDetails;
import ls.lesm.model.InternalExpenses;
import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.model.Salary;
import ls.lesm.model.SubDepartments;
import ls.lesm.model.User;
import ls.lesm.payload.request.ClientEmpUpdateReq;
import ls.lesm.payload.request.EmployeeDetailsRequest;
import ls.lesm.payload.request.EmployeeDetailsUpdateRequest;
import ls.lesm.payload.response.AllEmpCardDetails;
import ls.lesm.payload.response.ConsultantDropDownRes;
import ls.lesm.payload.response.EmpCorrespondingDetailsResponse;
import ls.lesm.payload.response.EmployeeDetailsResponse;
import ls.lesm.payload.response.EmployeeFullDetailsResponse;
import ls.lesm.repository.AddressRepositoy;
import ls.lesm.repository.AddressTypeRepository;
import ls.lesm.repository.ClientsRepository;
import ls.lesm.repository.DepartmentsRepository;
import ls.lesm.repository.DesignationsRepository;
import ls.lesm.repository.EmployeeTypeRepository;
import ls.lesm.repository.EmployeesAtClientsDetailsRepository;
import ls.lesm.repository.InternalExpensesRepository;
import ls.lesm.repository.MasterEmployeeDetailsRepository;
import ls.lesm.repository.SalaryRepository;
import ls.lesm.repository.SubDepartmentsRepository;
import ls.lesm.repository.UserRepository;
import ls.lesm.service.EmployeeDetailsService;

@Service
public class EmployeeDetailsServiceImpl implements EmployeeDetailsService {

	@Autowired
	private AddressRepositoy addressRepositoy;

	@Autowired
	private AddressTypeRepository addressTypeRepository;

	@Autowired
	private MasterEmployeeDetailsRepository masterEmployeeDetailsRepository;

	@Autowired
	private DepartmentsRepository departmentsRepository;

	@Autowired
	private SubDepartmentsRepository subDepartmentsRepositorye;

	@Autowired
	private DesignationsRepository designationsRepository;

	@Autowired
	private EmployeesAtClientsDetailsRepository employeesAtClientsDetailsRepository;

	@Autowired
	private InternalExpensesRepository internalExpensesRepository;

	@Autowired
	private SalaryRepository salaryRepository;

	@Autowired
	private ClientsRepository clientsRepository;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private EmployeeTypeRepository employeeTypeRepo;

	// UMER
	@Override
	public Address insertEmpAddress(Address address, Principal principal, Integer addTypeId) {
		// address.setCreatedAt(LocalDate.now());
		address.setCreatedBy(principal.getName());
		Optional<Object> optional = addressTypeRepository.findById(addTypeId).map(type -> {
			address.setAdressType(type);
			return type;
		});
		return addressRepositoy.save(address);
	}

	// UMER
	public EmployeeDetailsRequest insetEmpDetails(EmployeeDetailsRequest empDetails, Principal principal) {

		empDetails.getMasterEmployeeDetails().setCreatedAt(LocalDate.now());
		empDetails.getMasterEmployeeDetails().setCreatedBy(principal.getName());
		if (empDetails.getMasterEmployeeDetails().getDOB().isAfter(LocalDate.now())) {
			throw new DateMissMatchException("Date Of Birth can not be after todays date");
		}
		empDetails.getMasterEmployeeDetails()
				.setLancesoft(empDetails.getMasterEmployeeDetails().getLancesoft().toUpperCase());

		

		empDetails.getAddress().setCreatedAt(LocalDate.now());
		empDetails.getAddress().setCreatedBy(principal.getName());

		empDetails.getSalary().setCreatedAt(LocalDate.now());
		empDetails.getSalary().setCreatedBy(principal.getName());

		
		

		this.addressRepositoy.save(empDetails.getAddress());
		
		this.salaryRepository.save(empDetails.getSalary());
		return empDetails;
	}

	// UMER
	@Override
	public EmployeesAtClientsDetails insertClientsDetails(EmployeesAtClientsDetails clientDetails, Principal principal,
			String empId, Integer clientId) {
		clientDetails.setCreatedBy(principal.getName());
		clientDetails.setCreatedAt(LocalDate.now());
		MasterEmployeeDetails employee = this.masterEmployeeDetailsRepository.findByLancesoft(empId);
		this.masterEmployeeDetailsRepository.findById(employee.getEmpId()).map(id -> {
			clientDetails.setMasterEmployeeDetails(id);
			return id;
		});
		this.clientsRepository.findById(clientId).map(cId -> {
			clientDetails.setClients(cId);
			return cId;
		});
		if (clientDetails.getPOSdate().isBefore(employee.getJoiningDate())) {
			throw new DateMissMatchException(
					"PO Start date can not be before employee joining date: This is employee Joing Date: "
							+ employee.getJoiningDate());
		}

		if (clientDetails.getPOEdate() == null) {
			this.employeesAtClientsDetailsRepository.save(clientDetails);
			return clientDetails;
		}
		if (clientDetails.getPOSdate().isAfter(clientDetails.getPOEdate())) {
			throw new DateMissMatchException("Po start date can not be before po end date");
		}
		
		else {
			this.employeesAtClientsDetailsRepository.save(clientDetails);
			return clientDetails;
		}

	}

	// UMER
	@Override
	public Page<EmployeesAtClientsDetails> getAllEmpClinetDetails(PageRequest pageReuquest) {

		Page<EmployeesAtClientsDetails> list = employeesAtClientsDetailsRepository.findAll(pageReuquest);
		return list;

	}

	// UMER
	@Override
	public Page<AllEmpCardDetails> getAllEmpCardDetails(PageRequest pageRequest) {
		Page<AllEmpCardDetails> page = this.masterEmployeeDetailsRepository.getAlEmpCardDetails(pageRequest);
		return page;
	}

	// UMER
	@Override
	public Page<AllEmpCardDetails> getSortedEmpCardDetailsByDesg(Integer desgId, PageRequest pageRequest) {
		Page<AllEmpCardDetails> page = this.masterEmployeeDetailsRepository.getSortedEmpCardDetailsByDesg(desgId,
				pageRequest);
		return page;
	}

	// UMER
	@Override
	public EmpCorrespondingDetailsResponse getEmpCorresDetails(EmpCorrespondingDetailsResponse corssDetails,
			int empid) {
		Optional<InternalExpenses> data = this.internalExpensesRepository.findBymasterEmployeeDetails_Id(empid);
		if (data.isPresent())
			corssDetails.setInternalExpenses(data.get());// .setBenchTenure(data.get().getBenchTenure())

		Optional<Salary> data3 = this.salaryRepository.findBymasterEmployeeDetails_Id(empid);
		if (data3.isPresent())
			corssDetails.setSalary(data3.get());// .setSalary(data3.get().getSalary());

		EmployeeDetailsResponse data4 = this.masterEmployeeDetailsRepository.getEmpDetailsById(empid);
		corssDetails.setEmployeeDetailsResponse(data4);
		return corssDetails;
	}

	// UMER
//	@Transactional
	@Override
	public EmployeeDetailsUpdateRequest  updateEmployee(EmployeeDetailsUpdateRequest empReq, int id) {
		MasterEmployeeDetails employee = this.masterEmployeeDetailsRepository.findById(id).get();
	
		System.out.println("------------Dedisnaiton before update" + employee.getDesignations().getDesgNames());

		if (employee!=null) {
			employee.setFirstName(empReq.getMasterEmployeeDetails().getFirstName());
			employee.setLastName(empReq.getMasterEmployeeDetails().getLastName());

			employee.setDOB(empReq.getMasterEmployeeDetails().getDOB());

			employee.setEmail(empReq.getMasterEmployeeDetails().getEmail());
			employee.setJoiningDate(empReq.getMasterEmployeeDetails().getJoiningDate());
			
			//MasterEmployeeDetails emp=this.masterEmployeeDetailsRepository.findByLancesoft(empReq.getMasterEmployeeDetails().getLancesoft());
			
			if(employee.getLancesoft().equals(empReq.getMasterEmployeeDetails().getLancesoft()))
				employee.setLancesoft(empReq.getMasterEmployeeDetails().getLancesoft());
			
			if(employee.getLancesoft()!=empReq.getMasterEmployeeDetails().getLancesoft()) {
				
				MasterEmployeeDetails matchEmp=this.masterEmployeeDetailsRepository.findByLancesoft(empReq.getMasterEmployeeDetails().getLancesoft());
				if(matchEmp==null) {
					
					
					User user= this.userRepo.findByUsername(employee.getLancesoft());
					System.out.println("================="+employee.getLancesoft());
					user.setUsername(empReq.getMasterEmployeeDetails().getLancesoft());
					this.userRepo.save(user);
					employee.setLancesoft(empReq.getMasterEmployeeDetails().getLancesoft());
				}
				
				else
					throw new DuplicateEntryException("With this "+empReq.getMasterEmployeeDetails().getLancesoft()+" employee id employee already exist");
					
			}
				
			employee.setLocation(empReq.getMasterEmployeeDetails().getLocation());
			employee.setPhoneNo(empReq.getMasterEmployeeDetails().getPhoneNo());
			employee.setGender(empReq.getMasterEmployeeDetails().getGender());
			employee.setVertical(empReq.getMasterEmployeeDetails().getVertical());;
			employee.setStatus(empReq.getMasterEmployeeDetails().getStatus());
			employee.setSupervisor(this.masterEmployeeDetailsRepository.findById(empReq.getSupervisor()).get());
			if(empReq.getSubDepartments()>0) {
				SubDepartments subD=this.subDepartmentsRepositorye.findById(empReq.getSubDepartments()).get();
				employee.setSubDepartments(subD);
			}
			if(empReq.getSubDepartments()==0) {
				employee.setSubDepartments(null);
			}
			
			if(empReq.getDepartments()>0) {
				Departments depart=this.departmentsRepository.findById(empReq.getDepartments()).get();
				employee.setDepartments(depart);
			}
			if(empReq.getDepartments()==0) {
				employee.setDepartments(null);
			}
			
				
			
			employee.setDesignations(this.designationsRepository.findById(empReq.getDesignations()).get());
			employee.setEmployeeType(this.employeeTypeRepo.findById(empReq.getEmployeeType()).get());

			List<Salary> salaries = this.salaryRepository.findsBymasterEmployeeDetails_Id(employee.getEmpId());
			List<Integer> salIds = salaries.stream().map(Salary::getSalId).collect(Collectors.toList());// extracting all
																										// the salary id's
			// System.out.println("==========="+salId);
			int latestSalId = Collections.max(salIds);// finding the latest salary id form extracted sal ids

			Salary salary = this.salaryRepository.findById(latestSalId).get();// with latest salary id finding the sal
																				// record
			
			//Salary sal=this.salaryRepository.findByMasterEmployeeDetails(employee);
			salary.setSalary(empReq.getSalary());
			this.salaryRepository.save(salary);
			

		}
		if (empReq.getMasterEmployeeDetails().getDOB().isAfter(LocalDate.now())) {
			throw new DateMissMatchException("Date Of Birth can not be after todays date");
		}
//		MasterEmployeeDetails emp = this.masterEmployeeDetailsRepository
//				.findByLancesoft(empReq.getMasterEmployeeDetails().getLancesoft());
//		
//		if (emp.getLancesoft() != empReq.getMasterEmployeeDetails().getLancesoft() && emp != null) {
//			throw new DuplicateEntryException("Employee with this employee Id alreday exist in database");
//		} else {
			MasterEmployeeDetails updatedEmployee = this.masterEmployeeDetailsRepository.save(employee);
			System.out.println("-===============-Dedisnaiton after update" + updatedEmployee.getDesignations().getDesgNames());
	//	}
		Address add = this.addressRepositoy.findByEmpId(employee.getEmpId());
		ArrayList<Address> addList = new ArrayList<Address>();
	
			add.setCity(empReq.getAddress().getCity());
			add.setCountry(empReq.getAddress().getCountry());
			add.setZipCod(empReq.getAddress().getZipCod());
			add.setState(empReq.getAddress().getState());
			add.setStreet(empReq.getAddress().getStreet());
			addList.add(add);
		
//		for (Address add : empAddress) {
//			// for()
//			add.setCity(empReq.getAddress().getCity());
//			add.setCountry(empReq.getAddress().getCountry());
//			add.setZipCod(empReq.getAddress().getZipCod());
//			add.setState(empReq.getAddress().getState());
//			add.setStreet(empReq.getAddress().getStreet());
//			addList.add(add);
//		}
		List<Address> UpdateEmpAddress = this.addressRepositoy.saveAll(addList);
//		Optional<InternalExpenses> exp = this.internalExpensesRepository.findByEmployeeById(id);
//		InternalExpenses UpdatedExp = this.internalExpensesRepository.save(exp.get());

		 //empReq.setAddress(UpdateEmpAddress);
		// empReq.setInternalExpenses(UpdatedExp);

		return empReq;
	}

	// UMER
	@Override
	public ClientEmpUpdateReq updateEmpClientDetails(ClientEmpUpdateReq req,int clientId) {
          
		Optional<EmployeesAtClientsDetails> details=this.employeesAtClientsDetailsRepository.findById(clientId);
		//System.out.print("============"+details);
		MasterEmployeeDetails emp=this.masterEmployeeDetailsRepository.findById(details.get().getMasterEmployeeDetails().getEmpId()).get();
		if(details.isPresent()) {
         details.get().setClientEmail(req.getClientManagerEmail());
         details.get().setClientManagerName(req.getClientManagerEmail());
         details.get().setClients(this.clientsRepository.findById(req.getClientId()).get());
         details.get().setClientSalary(req.getClientSalary());
         details.get().setDesgAtClient(req.getDesgAtClient());
         
         if(req.getPoSDate().isBefore(emp.getJoiningDate()))
        	 throw new DateMissMatchException("PO start date is before employee joing date Joinging date ::("+emp.getJoiningDate()+")");
         if(req.getPoSDate().isAfter(req.getPoEDate()))
        	 throw new DateMissMatchException("PO start date can not be after PO end date");
         details.get().setPOSdate(req.getPoSDate());
         details.get().setPOEdate(req.getPoEDate());
         this.employeesAtClientsDetailsRepository.save(details.get());
		}
		
		return req;
	}

	// UMER
	@Override
	public EmployeeFullDetailsResponse empDetails(EmployeeFullDetailsResponse response, Integer empId) {
		//getting custom employee details in custom response class eg:- designation, department, and etc;
		EmployeeDetailsResponse allDetailsOfEmp = this.masterEmployeeDetailsRepository.getEmpDetailsById(empId);
		
		
		//finding employee just for geting emplyee crossponding details
		MasterEmployeeDetails employe=this.masterEmployeeDetailsRepository.findById(empId).get();
		
		List<InternalExpenses> exp= this.internalExpensesRepository.findByMasterEmployeeDetails(employe);
		
		List<Address> address = this.addressRepositoy.findByMasterEmployeeDetails(employe);
		
		List<EmployeesAtClientsDetails> clientDetails=this.employeesAtClientsDetailsRepository.findByMasterEmployeeDetails(employe);
		
		response.setDetailsResponse(allDetailsOfEmp);
		response.setInternalExpenses(exp);
		response.setAddres(address);
		response.setEmployeeAtClientsDetails(clientDetails);
		
		List<Salary> salaries = this.salaryRepository.findsBymasterEmployeeDetails_Id(empId);
		List<Integer> salIds = salaries.stream().map(Salary::getSalId).collect(Collectors.toList());// extracting all
																									// the salary id's
		// System.out.println("==========="+salId);
		int latestSalId = Collections.max(salIds);// finding the latest salary id form extracted sal ids

		Salary salary = this.salaryRepository.findById(latestSalId).get();// with latest salary id finding the sal
																			// record
		response.setSalary(salary.getSalary());
		return response;
	}

	@Override
	public ClientEmpUpdateReq getClientDetailForUpdate(ClientEmpUpdateReq req, int clientDetailId) {
		Optional<EmployeesAtClientsDetails> detail=this.employeesAtClientsDetailsRepository.findById(clientDetailId);
		req.setClientManagerEmail(detail.get().getClientManagerName());
		req.setClientManagerName(detail.get().getClientManagerName());
		req.setClientSalary(detail.get().getClientSalary());
		req.setDesgAtClient(detail.get().getDesgAtClient());
		req.setPoEDate(detail.get().getPOEdate());
		req.setPoSDate(detail.get().getPOSdate());
		req.setClientId(detail.get().getClients().getClientsId());
		return req;
	}

	@Override
	public List<ConsultantDropDownRes> getAllConsByPractice(int subDId,String lancesoftId) {
//		this.designationsRepository.findByDesgNames("Consultant");
//		this.subDepartmentsRepositorye.findBySubDepartmentNames("java");
		
		List<MasterEmployeeDetails> consultant=this.masterEmployeeDetailsRepository
				.findByDesignationsAndSubDepartmentsAndLancesoftContaining(
				this.designationsRepository.findByDesgNamesIgnoreCase("Consultant"),
				this.subDepartmentsRepositorye.findById(subDId).get(),
				lancesoftId);
		List<ConsultantDropDownRes> conList=new ArrayList<ConsultantDropDownRes>();
		for(MasterEmployeeDetails m: consultant) {
			ConsultantDropDownRes res=new ConsultantDropDownRes();
			res.setName(m.getFirstName()+" "+m.getLastName());
			res.setLancesoftId(m.getLancesoft());
			res.setEmpId(m.getEmpId());
			conList.add(res);
		}
		return conList;
	}
	
	
	

}
