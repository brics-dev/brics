package gov.nih.brics.auth.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import gov.nih.tbi.account.model.hibernate.SessionLog;

public interface SessionLogRepository extends CrudRepository<SessionLog, Long> {
	
	@Query("SELECT s FROM SessionLog s WHERE LOWER(s.username) = LOWER(:username) AND s.timeOut IS NULL ORDER BY s.timeIn")
	List<SessionLog> getActiveSessionsForUser(@Param("username") String username);
	
	@Query("SELECT s FROM SessionLog s WHERE s.timeOut IS NULL")
	List<SessionLog> getAllActiveSessions();
}
