package ls.lesm.model.recruiter;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ls.lesm.model.MasterEmployeeDetails;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobString {
	
	@Id
	@GeneratedValue(generator = "jobStr_id_gen",strategy = GenerationType.AUTO)
	private Integer jobStringId;
	
	@Lob 
	@Column(length=512)
	private String JD; 
	
	private Integer totalPosition;
	
	private String stringCreatedBy;
	
	private String budget;
	
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate openDate;
	
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate closeDate;
	
	@Column(length=30)
	private String clientName;
	
	private String sampleResume;
	
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate createdAt;
	
	
	private String jobStringTicket;
	
	private boolean ticketStatus;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "string_tags", joinColumns = { @JoinColumn(name = "job_string_id") }, inverseJoinColumns = {
			@JoinColumn(name = "emp_rec_id") })
	private List<MasterEmployeeDetails> masterEmployeeDetails;
	

	 public void addTag(MasterEmployeeDetails tag) {
		    this.masterEmployeeDetails.addAll(masterEmployeeDetails);
		    tag.getJobString().add(this);
		  }

}
