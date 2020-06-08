package gov.nih.brics.downloadtool.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;

/**
 * Provides access to the DownloadPackage database tables (through hibernate)
 * 
 * @author Joshua Park
 *
 */
public interface DownloadPackageRepository extends CrudRepository<DownloadPackage, Long> {
	public List<DownloadPackage> findDistinctByUser(User user);
	public List<DownloadPackage> findByDownloadablesIsEmpty();
	@Modifying
	@Query("delete from DownloadPackage d where d.id in ?1")
	void deleteByIds(List<Long> ids);
}
