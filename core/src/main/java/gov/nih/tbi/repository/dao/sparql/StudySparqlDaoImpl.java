package gov.nih.tbi.repository.dao.sparql;

import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.repository.dao.StudySparqlDao;
import gov.nih.tbi.repository.model.hibernate.Study;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class StudySparqlDaoImpl extends GenericSparqlDaoImpl<Study> implements StudySparqlDao
{

    public List<Study> getAllBasic()
    {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Study> getAll()
    {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Study get(String uri)
    {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Study save(Study object)
    {

        // TODO Auto-generated method stub
        return null;
    }
}
