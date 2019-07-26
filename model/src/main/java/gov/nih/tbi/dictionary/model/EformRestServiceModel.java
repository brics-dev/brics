
package gov.nih.tbi.dictionary.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;

public class EformRestServiceModel{

    @XmlRootElement(name = "BasicEforms")
    public static class BasicEformList{

        @XmlElement(name = "BasicEforms")
        List<BasicEform> List = new ArrayList<BasicEform>();

        public BasicEformList(){}

        public void addAll(List<BasicEform> s){
            List.addAll(s);
        }

        public void add(BasicEform s){
            List.add(s);
        }

        public List<BasicEform> getList(){
            return List;
        }
    }
    
    @XmlRootElement(name = "Eforms")
    public static class EformList{

        @XmlElement(name = "Eforms")
        List<Eform> List = new ArrayList<Eform>();

        public EformList(){}

        public void addAll(List<Eform> s){
            List.addAll(s);
        }

        public void add(Eform s){
            List.add(s);
        }

        public List<Eform> getList(){
            return List;
        }
    }
    
    @XmlRootElement(name = "DataElementNames")
    public static class DataElementNameList{

        @XmlElement(name = "DataElementNames")
        List<String> List = new ArrayList<String>();

        public DataElementNameList(){}

		public void addAll(Collection<String> s) {
            List.addAll(s);
        }

        public void add(String s){
            List.add(s);
        }

        public List<String> getList(){
            return List;
        }
    }
}
