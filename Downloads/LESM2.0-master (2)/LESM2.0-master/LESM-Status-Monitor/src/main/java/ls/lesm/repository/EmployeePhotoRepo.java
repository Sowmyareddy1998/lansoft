package ls.lesm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ls.lesm.model.EmployeePhoto;
import ls.lesm.model.MasterEmployeeDetails;

public interface EmployeePhotoRepo extends JpaRepository<EmployeePhoto, Integer>{

	EmployeePhoto findByMasterEmployeeDetails(MasterEmployeeDetails employee);

}
