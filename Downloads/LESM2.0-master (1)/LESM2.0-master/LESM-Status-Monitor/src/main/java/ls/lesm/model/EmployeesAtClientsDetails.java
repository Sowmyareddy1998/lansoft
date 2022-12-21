package  ls.lesm.model;



import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedNativeQuery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ls.lesm.payload.response.DataResponse;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

            			
public class EmployeesAtClientsDetails implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(generator = "emp_at_cdetails_gen",strategy = GenerationType.AUTO)
	private Integer EmpAtClientId;
	private Double clientSalary;
	private LocalDate POSdate;// purchase order start date
	
	@Column(nullable=true)
	private LocalDate POEdate;// purchase order end date
	
	@Column(length=30)
	private String desgAtClient;
	
	//private Long clientTenure;// toatl months at client(posdate to poedate)
	//private Double totalEarningAtClients;// clientTenure*cliendt salary
	
	@JsonIgnore
	private LocalDate createdAt;//timpStamp
	
	@JsonIgnore
	@Column(length=30)
	private String createdBy;//principal
	
	private String clientEmail;
	
	private String clientManagerName;
	
	@JsonIgnore
	//@Fetch(FetchMode.JOIN) 
	@JsonIgnoreProperties({"hibernateLazyInitializer"})
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="emp_id_fk")
	private MasterEmployeeDetails masterEmployeeDetails;
	
	//@JsonIgnore
	//@Fetch(FetchMode.JOIN) 
	@JsonIgnoreProperties({"hibernateLazyInitializer"})
	@OneToOne( fetch=FetchType.EAGER)
	@JoinColumn(name="clients_fk")
	private Clients clients;
	
	
	@Transient
	private Long tenure;
	
	private Double totalEarningAtclient;

}
