package ls.lesm.service.impl;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ls.lesm.exception.DateMissMatchException;
import ls.lesm.exception.RecordNotFoundException;
import ls.lesm.exception.TicketClosedException;
import ls.lesm.model.Designations;
import ls.lesm.model.MasterEmployeeDetails;
import ls.lesm.model.exp.ExpenseNotification;
import ls.lesm.model.recruiter.CandidateProfiles;
import ls.lesm.model.recruiter.CandidateStatus;
import ls.lesm.model.recruiter.Consultant;
import ls.lesm.model.recruiter.JobString;
import ls.lesm.model.recruiter.RecruiterProfitOrLoss;
import ls.lesm.model.recruiter.Status;
import ls.lesm.payload.request.ApproveProfilesRequest;
import ls.lesm.payload.request.CandidateStatusAndConsultantRequest;
import ls.lesm.payload.response.RecruiterDropDownResponse;
import ls.lesm.repository.DesignationsRepository;
import ls.lesm.repository.MasterEmployeeDetailsRepository;
import ls.lesm.repository.SubDepartmentsRepository;
import ls.lesm.repository.expRepo.ExpenseNotificatonRepo;
import ls.lesm.repository.recruiter.CandidateProfilesRepo;
import ls.lesm.repository.recruiter.CandidateStatusRepo;
import ls.lesm.repository.recruiter.ConsultantRepo;
import ls.lesm.repository.recruiter.JobStringRepo;
import ls.lesm.repository.recruiter.RecruiterProfitOrLossRepo;
import ls.lesm.service.IImageService;
import ls.lesm.service.RecruiterService;

@Service
//@Configuration
@EnableScheduling
public class RecruiterServiceImpl implements RecruiterService {

	@Autowired
	private JobStringRepo jobStringRepo;

	@Autowired
	private IImageService imageService;

	@Autowired
	private ExpenseNotificatonRepo expenseNotificatonRepo;

	@Autowired
	private MasterEmployeeDetailsRepository masterEmployeeDetailsRepository;
	
	@Autowired
	private CandidateProfilesRepo candidateProfilesRepo;
	
	@Autowired
	private SubDepartmentsRepository subDepartmentsRepository;
	
	@Autowired
	private DesignationsRepository designationsRepository;
	
	@Autowired
	private CandidateStatusRepo candidateStatusRepo;
	
	@Autowired
	private ConsultantRepo consultantRepo;
	
	@Autowired
	private RecruiterProfitOrLossRepo recruiterProfitOrLossRepo;

	@Override
	public String postJobString(JobString jobString, Principal principal, Set<Integer> empIds, MultipartFile file) {

		jobString.setStringCreatedBy(principal.getName());
		jobString.setCreatedAt(LocalDate.now());
		if (jobString.getOpenDate().isAfter(jobString.getCloseDate()))
			throw new DateMissMatchException("Open date should not be before close date");

		// open
		// this is for creating JobString ticket name with meaning full information
		String positionIntoString = jobString.getTotalPosition().toString();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MMMM.yyyy", Locale.ENGLISH);
		String openD = dtf.format(jobString.getOpenDate());
		String closeD = dtf.format(jobString.getCloseDate());
		LocalDateTime timeStamp = LocalDateTime.now();
		String timeStampInString = String.valueOf(timeStamp);
		String ticket = jobString.getClientName() + "_" + positionIntoString + "_" + openD + "_" + closeD + "_"
				+ jobString.getBudget() + "_" + timeStampInString;
		// this jobString ticket name contain jobStrign information
		// eg. copg_05_01.may.2022_05.may.2022_15lpa_currentHours
		jobString.setJobStringTicket(ticket);
		// close

		List<MasterEmployeeDetails> employees = this.masterEmployeeDetailsRepository.findAllById(empIds);
		MasterEmployeeDetails jobStringCreaterEmployee = this.masterEmployeeDetailsRepository
				.findByLancesoft(principal.getName());

		jobString.setMasterEmployeeDetails(employees);

		try {
			String fileName = imageService.save(file);
			String imageUrl = imageService.getImageUrl(fileName);
			jobString.setSampleResume(imageUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// creating a notification people who are tagged this jobString will get
		// notification
		for (MasterEmployeeDetails emp : employees) {
			ExpenseNotification notification = new ExpenseNotification();
			notification.setMessage("Hi " + emp.getFirstName() + " " + emp.getLastName() + ", "
					+ jobStringCreaterEmployee.getFirstName() + " " + jobStringCreaterEmployee.getLastName()
					+ " has sent you new Job String");
			this.masterEmployeeDetailsRepository.findById(emp.getEmpId()).map(id -> {
				notification.setMasterEmployeeDetails(id);
				return this.expenseNotificatonRepo.save(notification);
			});
		}

		this.jobStringRepo.save(jobString);

		return ticket;
	}

	//everyday at 11:00 pm this method will execute
	@Scheduled(cron = "0 0 23 * * *")
	@Override // this is autu job it will execute once every day, it will check all the opened
				// ticket close date, if ticket close date was yasterdays one then it will close
				// that ticket today
	public void closeTicketJob() {
		List<JobString> allOpendJobString = this.jobStringRepo.findAllByTicketStatus(true);
		// taggedEmpsRec=allOpendJobString.stream().map(JobString::getMasterEmployeeDetails).collect(Collectors.toList());

		for (JobString j : allOpendJobString) {
			if (j.getCloseDate().equals(LocalDate.now().minusDays(1))) {// camparing current date minus one day bcz if
																		// today is last date of close means we have
																		// today, ticket should close tomm
				j.setTicketStatus(false);

				MasterEmployeeDetails stringCreateEmp = this.masterEmployeeDetailsRepository// extracting emp who
																							// created this ticket, for
																							// notification
						.findByLancesoft(j.getStringCreatedBy());
				ExpenseNotification notiForCreater = new ExpenseNotification();// setting up notification for ticket
																				// creater
				notiForCreater.setMessage("Hi " + stringCreateEmp.getFirstName() + " " + stringCreateEmp.getLastName()
						+ ", this ticket " + j.getJobStringTicket() + " created by you has been closed");
				notiForCreater.setMasterEmployeeDetails(stringCreateEmp);
				this.expenseNotificatonRepo.save(notiForCreater);
				List<MasterEmployeeDetails> taggedEmpsRec = j.getMasterEmployeeDetails();// extracting all the recrutier
																							// who are tagged for this
																							// jobstring
				for (MasterEmployeeDetails recEmp : taggedEmpsRec) {
					ExpenseNotification noti = new ExpenseNotification();// setting up noti for recruiter
					noti.setMessage("Hi " + recEmp.getFirstName() + " " + recEmp.getLastName() + ", this ticket "
							+ j.getJobStringTicket() + " has been closed");
					noti.setMasterEmployeeDetails(recEmp);

					this.expenseNotificatonRepo.save(noti);
				}

				this.jobStringRepo.save(j);
			}
		}

	}

	@Override
	public List<JobString> getOpenedJobStringByLoggedInEmp(Principal principal) {
		
		MasterEmployeeDetails loggedInEmp=this.masterEmployeeDetailsRepository.findByLancesoft(principal.getName());
		
		List<JobString> allTaggedJobString=this.jobStringRepo.findAllByMasterEmployeeDetailsAndTicketStatus(loggedInEmp,  true);
		return allTaggedJobString;
	}

	@Override
	public String sendProfiles(CandidateProfiles profiles, Integer jobStringId, Principal principal,
			MultipartFile resume) {

		profiles.setCreatedAt(LocalDate.now());
		profiles.setManagerApproval(Status.PENDING);
		
		try {//sending file to firebase bucket
			String fileName = imageService.save(resume);
			String resumeUrl = imageService.getImageUrl(fileName);
			profiles.setCandiResume(resumeUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//setting jobString id to this profile 
		Optional<JobString> jobString = this.jobStringRepo.findById(jobStringId);
		if(jobString.get().isTicketStatus()==true) //checking ticket is opened or closed when it is opend then only allowing to send profiles
			profiles.setJobString(jobString.get());
		else 
			throw new TicketClosedException("This Ticket (" +jobString.get().getJobStringTicket()+ ") has been closed on "+jobString.get().getCloseDate());
		
		MasterEmployeeDetails loggedInEmp = this.masterEmployeeDetailsRepository.findByLancesoft(principal.getName());
		profiles.setMasterEmployeeDetails(loggedInEmp);//setting recruiter emp id for thid profile
		CandidateProfiles profile = this.candidateProfilesRepo.save(profiles);

		//finding a jobString creater emp for sending notification about new profile
		MasterEmployeeDetails stringCreaterEmp = this.masterEmployeeDetailsRepository
				                                     .findByLancesoft(jobString.get().getStringCreatedBy());
		
		ExpenseNotification notification = new ExpenseNotification();
		notification.setMessage("Hi " + stringCreaterEmp.getFirstName() + " " + stringCreaterEmp.getLastName() + ", "
				+ loggedInEmp.getFirstName() + " " + loggedInEmp.getLastName()
				+ " has sent you new profile for this ticket (" + jobString.get().getJobStringTicket()+")");
		notification.setMasterEmployeeDetails(stringCreaterEmp);
		this.expenseNotificatonRepo.save(notification);

		return profile.getCandidateId();

	}

	@Override
	public List<RecruiterDropDownResponse> recruitersDropDown(Principal principal) {
		Designations desg=this.designationsRepository.findByDesgNamesLike("Recruiter");
		if(desg==null)
			throw new RecordNotFoundException("Recruiter designation not exist, please add designation as (Recruiter)");
		MasterEmployeeDetails supervisor=this.masterEmployeeDetailsRepository.findByLancesoft(principal.getName());
		List<MasterEmployeeDetails> recruiters=this.masterEmployeeDetailsRepository.findByDesignationsAndSupervisor(desg,supervisor);
		List<RecruiterDropDownResponse> list=new ArrayList<RecruiterDropDownResponse>();
		for(MasterEmployeeDetails rec: recruiters) {
			RecruiterDropDownResponse dropDownRes=new RecruiterDropDownResponse();
			list.add(dropDownRes);
			dropDownRes.setEmpId(rec.getEmpId());
			dropDownRes.setLancesoftId(rec.getLancesoft());
			dropDownRes.setName(rec.getFirstName()+" "+rec.getLastName());
		}
	
		return list;
	}

	@Override
	public void managerProfileApproval(List<ApproveProfilesRequest> req) {
		for(ApproveProfilesRequest r:req) {

			CandidateProfiles profile=this.candidateProfilesRepo.findById(r.getId()).get();

			//ExpenseNotification noti=new ExpenseNotification();
			//noti.setMessage();
			profile.setManagerApproval(r.getStatus());
			this.candidateProfilesRepo.save(profile); 
		}

	}

	@Override

	public CandidateStatusAndConsultantRequest scheduleInterview(CandidateStatusAndConsultantRequest schedule,String candidateId,Principal principal) {
		
		schedule.getCandidateStatus().setL1Status(Status.PENDING);
		schedule.getCandidateStatus().setL2Status(Status.PENDING);
		//schedule.set
		
		this.candidateProfilesRepo.findById(candidateId).map(id->{
			schedule.getCandidateStatus().setCandidateProfiles(id);
			return id;
		});
		
		if(schedule.getCandidateStatus().getL1ScheduleAt().equals(LocalDate.now()) ||schedule.getCandidateStatus().getL1ScheduleAt().isAfter(LocalDate.now()))
			schedule.getCandidateStatus().setL1Status(schedule.getCandidateStatus().getL1Status());
		if(schedule.getCandidateStatus().getL2ScheduleAt().equals(LocalDate.now()) || schedule.getCandidateStatus().getL2ScheduleAt().isAfter(LocalDate.now()) )
			schedule.getCandidateStatus().setL2Status(schedule.getCandidateStatus().getL2Status());
		if(schedule.getCandidateStatus().getL1Status()==Status.PASSED && schedule.getCandidateStatus().getL2Status()==Status.PASSED)
			schedule.getCandidateStatus().setReleasedOffer(true);
		if(schedule.getCandidateStatus().isReleasedOffer()==true)
			schedule.getCandidateStatus().setJoined(schedule.getCandidateStatus().isJoined());
		
			
			
			schedule.getCandidateStatus().setMasterEmployeeDetails(this.masterEmployeeDetailsRepository.findByLancesoft(principal.getName()));
		CandidateStatus status= this.candidateStatusRepo.save(schedule.getCandidateStatus());
		if(status.isJoined()==true) {
		//	schedule.getConsultans().setClientBilling(schedule.getConsultans().getClientBilling());
			schedule.getConsultans().setConId(status.getCandidateProfiles().getCandidateId());
			//schedule.getConsultans().setClientName(status.getJobString().getClientName());
			schedule.getConsultans().setConsultanName(status.getCandidateProfiles().getCandidateName());	
			schedule.getConsultans().setContractEndAt(schedule.getConsultans().getContractEndAt());
			schedule.getConsultans().setEmail(schedule.getConsultans().getEmail());
			schedule.getConsultans().setEmployeeType(schedule.getConsultans().getEmployeeType());
			schedule.getConsultans().setJoiningDate(schedule.getConsultans().getJoiningDate());
			schedule.getConsultans().setMasterEmployeeDetails(status.getMasterEmployeeDetails());
			schedule.getConsultans().setMobileNo(schedule.getConsultans().getMobileNo());
			schedule.getConsultans().setSalary(schedule.getConsultans().getSalary());
			this.consultantRepo.save(schedule.getConsultans());
		}
		
       return schedule;
	}

	@Override
	public List<RecruiterProfitOrLoss> getRecruiterProfitOrLoss( Principal principal) {
		//this.masterEmployeeDetailsRepository.findByLancesoft(principal.getName())
		//int[] ids= {147,148};
		//this.masterEmployeeDetailsRepository
		MasterEmployeeDetails supEmp=this.masterEmployeeDetailsRepository.findByLancesoft(principal.getName());
		
		List<MasterEmployeeDetails> allRec=this.masterEmployeeDetailsRepository.findBySupervisorAndDesignations(supEmp,this.designationsRepository.findByDesgNames("Recruiter"));
		
	List<Consultant> consultans=this.consultantRepo.findByMasterEmployeeDetailsIn(allRec);
		for(Consultant c:consultans) {
			RecruiterProfitOrLoss rec =new RecruiterProfitOrLoss();
			rec.setMasterEmployeeDetails(c.getMasterEmployeeDetails());
			rec.setProfitOrLoss(8.33*c.getSalary()/100);
			this.recruiterProfitOrLossRepo.save(rec);
		}
		
		return this.recruiterProfitOrLossRepo.findByMasterEmployeeDetailsIn(allRec);
		//return null;
	}
	
	

}
