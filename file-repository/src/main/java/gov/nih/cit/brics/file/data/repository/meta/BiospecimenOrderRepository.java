package gov.nih.cit.brics.file.data.repository.meta;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

public interface BiospecimenOrderRepository extends CrudRepository<BiospecimenOrder, Long> {

}
