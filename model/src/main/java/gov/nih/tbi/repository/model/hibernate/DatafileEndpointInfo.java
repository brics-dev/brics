
package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "DATAFILE_ENDPOINT_INFO")
@XmlRootElement(name="DatafileEndpointInfo")
public class DatafileEndpointInfo implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -1171835990463947605L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATAFILE_ENDPOINT_INFO_SEQ")
    @SequenceGenerator(name = "DATAFILE_ENDPOINT_INFO_SEQ", sequenceName = "DATAFILE_ENDPOINT_INFO_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "ENDPOINT_NAME")
    private String endpointName;

    @Column(name = "CONN_URL")
    private String url;

    @Column(name = "CONN_NAME")
    private String userName;

    @Column(name = "CONN_PASS")
    private String password;

    @Column(name = "CONN_PORT")
    private Integer port;

    @Column(name = "KEY_PATH")
    private String keyPath;
    
    
    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getEndpointName()
    {

        return endpointName;
    }

    public void setEndpointName(String endpointName)
    {

        this.endpointName = endpointName;
    }

    public String getUrl()
    {

        return url;
    }

    public void setUrl(String url)
    {

        this.url = url;
    }

    public String getUserName()
    {

        return userName;
    }

    public void setUserName(String userName)
    {

        this.userName = userName;
    }

    public String getPassword()
    {

        return password;
    }

    public void setPassword(String password)
    {

        this.password = password;
    }

    public Integer getPort()
    {

        return port;
    }

    public void setPort(Integer port)
    {

        this.port = port;
    }

	public String getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}

}
