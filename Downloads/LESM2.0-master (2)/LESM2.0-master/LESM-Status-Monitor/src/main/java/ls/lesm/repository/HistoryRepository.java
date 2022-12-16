package ls.lesm.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ls.lesm.model.History;
import ls.lesm.payload.request.Temp;

public interface HistoryRepository extends JpaRepository<History, Integer> {

	//List<History> findByCreatedAt(LocalDate date);

	List<History> findByCreatedAt(LocalDate localDate);

}

