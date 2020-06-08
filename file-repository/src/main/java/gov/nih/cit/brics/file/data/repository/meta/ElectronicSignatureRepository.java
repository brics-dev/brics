package gov.nih.cit.brics.file.data.repository.meta;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.ElectronicSignature;

public interface ElectronicSignatureRepository extends CrudRepository<ElectronicSignature, Long> {

}
