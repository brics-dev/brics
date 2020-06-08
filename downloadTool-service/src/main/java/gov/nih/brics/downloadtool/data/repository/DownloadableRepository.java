package gov.nih.brics.downloadtool.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.repository.model.hibernate.Downloadable;

/**
 * Provides access to the Downloadable tables (through hibernate)
 * 
 * @author Joshua Park
 *
 */
public interface DownloadableRepository extends CrudRepository<Downloadable, Long> {
	
	@Modifying
	@Query("delete from Downloadable d where d.id in ?1")
	void deleteAllByIds(List<Long> ids);
	
	@Modifying
	@Query("delete from Downloadable d where d.downloadPackage.id = ?1")
	public int deleteByDownloadPackageId(Long downloadPackageId);
}
