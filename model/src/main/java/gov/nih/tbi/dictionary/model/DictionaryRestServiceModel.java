
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class DictionaryRestServiceModel
{

	@XmlRootElement(name = "Strings")
	public static class StringList {
		@XmlElement(name = "String")
		List<String> strings = new ArrayList<String>();

		public void addAll(List<String> s) {

			strings.addAll(s);
		}

		public List<String> getList() {

			return strings;
		}
	}

    @XmlRootElement(name = "SubDomains")
    public static class SubDomainList
    {

        @XmlElement(name = "SubDomain")
        List<SubDomain> List = new ArrayList<SubDomain>();

        public SubDomainList()
        {

        }

        public void addAll(List<SubDomain> s)
        {

            List.addAll(s);
        }

        public List<SubDomain> getList()
        {

            return List;
        }
    }

    @XmlRootElement(name = "Domains")
    public static class DomainList
    {

        @XmlElement(name = "Domain")
        List<Domain> List = new ArrayList<Domain>();

        public DomainList()
        {

        }

        public void addAll(List<Domain> s)
        {

            List.addAll(s);
        }

        public List<Domain> getList()
        {

            return List;
        }
    }

    @XmlRootElement(name = "Subgroups")
    public static class SubgroupList
    {

        @XmlElement(name = "Subgroup")
        List<Subgroup> list = new ArrayList<Subgroup>();

        public SubgroupList()
        {

        }

        public void addAll(List<Subgroup> s)
        {

            list.addAll(s);
        }

        public List<Subgroup> getList()
        {

            return list;
        }
    }

    @XmlRootElement(name = "Classifications")
    public static class ClassificationList
    {

        @XmlElement(name = "Classification")
        List<Classification> List = new ArrayList<Classification>();

        public ClassificationList()
        {

        }

        public void addAll(List<Classification> s)
        {

            List.addAll(s);
        }

        public List<Classification> getList()
        {

            return List;
        }
    }

    @XmlRootElement(name = "Populations")
    public static class PopulationList
    {

        @XmlElement(name = "Population")
        List<Population> list = new ArrayList<Population>();

        public PopulationList()
        {

        }

        public void addAll(List<Population> populationList)
        {

            list.addAll(populationList);
        }

        public List<Population> getList()
        {

            return list;
        }
    }

    @XmlRootElement(name = "Categories")
    public static class CategoryList
    {

        @XmlElement(name = "Category")
        List<Category> list = new ArrayList<Category>();

        public CategoryList()
        {

        }

        public void addAll(List<Category> CategoryList)
        {

            list.addAll(CategoryList);
        }

        public List<Category> getList()
        {

            return list;
        }
    }

    @XmlRootElement(name = "Diseases")
    public static class DiseaseList
    {

        @XmlElement(name = "Disease")
        List<Disease> List = new ArrayList<Disease>();

        public DiseaseList()
        {

        }

        public void addAll(List<Disease> s)
        {

            List.addAll(s);
        }

        public List<Disease> getList()
        {

            return List;
        }
    }

    @XmlRootElement(name = "DataElements")
    public static class DataElementList
    {

        @XmlElement(name = "DataElement")
        List<DataElement> List = new ArrayList<DataElement>();

        public DataElementList()
        {

        }

        public void addAll(List<DataElement> s)
        {

            List.addAll(s);
        }

        public List<DataElement> getList()
        {

            return List;
        }
    }
    
    @XmlRootElement(name = "SemanticDataElements")
    public static class SemanticDataElementList
    {

        @XmlElement(name = "SemanticDataElement")
        List<SemanticDataElement> List = new ArrayList<SemanticDataElement>();

        public SemanticDataElementList()
        {

        }

        public void addAll(List<SemanticDataElement> s)
        {

            List.addAll(s);
        }

        public List<SemanticDataElement> getList()
        {

            return List;
        }
    }
    
    @XmlRootElement(name = "SemanticFormStructures")
    public static class SemanticFormStructureList
    {

        @XmlElement(name = "SemanticFormStructure")
        List<SemanticFormStructure> List = new ArrayList<SemanticFormStructure>();

        public SemanticFormStructureList()
        {

        }

        public void addAll(List<SemanticFormStructure> s)
        {

            List.addAll(s);
        }

        public List<SemanticFormStructure> getList()
        {

            return List;
        }
    }

    @XmlRootElement(name = "FormStructures")
    public static class DataStructureList
    {

        @XmlElement(name = "FormStructure")
        List<FormStructure> List = new ArrayList<FormStructure>();

        public DataStructureList()
        {

        }

        public void addAll(List<FormStructure> s)
        {

            List.addAll(s);
        }

        public void add(FormStructure s)
        {

            List.add(s);
        }

        public List<FormStructure> getList()
        {

            return List;
        }
    }

    @XmlRootElement(name = "StructuralFormStructures")
    public static class StructuralFormStructureList
    {

        @XmlElement(name = "StructuralFormStructure")
        List<StructuralFormStructure> List = new ArrayList<StructuralFormStructure>();

        public StructuralFormStructureList()
        {

        }

        public void addAll(List<StructuralFormStructure> s)
        {

            List.addAll(s);
        }

        public void add(StructuralFormStructure s)
        {

            List.add(s);
        }

        public List<StructuralFormStructure> getList()
        {

            return List;
        }
    }

    @XmlRootElement(name = "KeywordList")
    public static class KeywordList
    {

        @XmlElement(name = "Keyword")
        HashSet<Keyword> list = new HashSet<Keyword>();;

        public KeywordList()
        {

        }

        public void addAll(Collection<Keyword> s)
        {

            if (s != null)
            list.addAll(s);
        }

        public HashSet<Keyword> getList()
        {

            return list;
        }
    }

    /**
     * Note: I had to make this class because the filenames have illegal characters accorinding to rest.
     * 
     * @author mgree1
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "StringWrapper")
    public static class StringWrapper
    {

        @XmlElement(name = "String")
        String str = new String();

        public StringWrapper()
        {

        }

        public String getStr()
        {

            return this.str;
        }

        public void setStr(String str)
        {

            this.str = str;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "UserFileWrapper")
    public static class UserFileWrapper
    {

        @XmlElement(name = "UserFile")
        UserFile uf = new UserFile();

        public UserFileWrapper()
        {

        }

        public UserFile getUserFile()
        {

            return this.uf;
        }

        public void setUserFile(UserFile uf)
        {

            this.uf = uf;
        }
    }

    @XmlRootElement(name = "DEValueRangeMap")
    public static class DEValueRangeMap {
    	
    	@XmlJavaTypeAdapter(DEValueRangeMapAdapter.class)
        Map<String, Map<String, ValueRange>> deValueRangeMap = null;
    	
    	public DEValueRangeMap() {
    		deValueRangeMap = new HashMap<String, Map<String, ValueRange>>();
    	}
    	
    	public void putAll(Map<String, Map<String, ValueRange>> dvrMap) {
    		deValueRangeMap.putAll(dvrMap);
    	}
    	    	
    	public Map<String, Map<String, ValueRange>> getMap() {
    		return deValueRangeMap;
    	}
    }


	@XmlRootElement(name = "SchemaList")
	public static class SchemaList {

		@XmlElement(name = "Schema")
		List<Schema> schemaList = new ArrayList<Schema>();

		public SchemaList() {
		}

		public void addAll(List<Schema> s) {
			schemaList.addAll(s);
		}

		public void add(Schema s) {
			schemaList.add(s);
		}

		public List<Schema> getList() {
			return schemaList;
		}

	}
}
