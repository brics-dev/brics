
package gov.nih.tbi.ordermanager.model;

import gov.nih.tbi.commons.model.hibernate.Address;
import gov.nih.tbi.commons.model.hibernate.User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 
 * @author vpacha
 * 
 */
@Entity
@Table(name = "Biospecimen_Order")
public class BiospecimenOrder implements Serializable
{

    private static final long serialVersionUID = 2674795254902640308L;
    
    public static final String Date_Submitted = "dateSubmitted";
    public static final String Order_Status = "orderStatus";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BIOSPECIMEN_ORDER_SEQ")
    @SequenceGenerator(name = "BIOSPECIMEN_ORDER_SEQ", sequenceName = "BIOSPECIMEN_ORDER_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "Investigator_Name")
    private String investigatorName;
    @Column(name = "Institution")
    private String institution;
    @Column(name = "Order_Title")
    private String orderTitle;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Address.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "Address_ID")
    private Address address;
    @Column(name = "Email")
    private String email;
    @Column(name = "Phone")
    private String phone;
    @Column(name = "Fax")
    private String fax;
    @Column(name = "Grant_Title")
    private String grantTitle;
    @Column(name = "Grant_Number")
    private Integer grantNumber;
    @Column(name = "Grant_Principal_Investigator")
    private String grantPrincipalInvestigator;
    @Column(name = "Date_Created")
    private Date dateCreated;
    @Column(name = "Date_Submitted")
    private Date dateSubmitted;
    @Column(name = "abstract_text")
    private String abstractText;
    @Column(name = "experimental_design_power_analysis")
    private String experimentalDesignPowerAnalysis;
    @Column(name = "ship_to_name")
    private String shipToName;
    @Column(name = "ship_to_institution")
    private String shipToInstitution;
    @Column(name = "affiliation")
    private String affiliation;
    @Column(name = "affiliation_phone")
    private String affiliationPhone;
    @Column(name = "affiliation_email")
    private String affiliationEmail;
    @Column(name = "affiliation_special_instructions")
    private String affiliationSpecialInstructions;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "Funding_Status_ID")
    private FundingStatus fundingStatus;

    @Column(name = "Funding_Agency")
    private String fundingAgency;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "biospecimenOrder", cascade = CascadeType.ALL, targetEntity = BiospecimenItem.class, orphanRemoval = true)
    private Collection<BiospecimenItem> requestedItems;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "Order_Status_ID")
    private OrderStatus orderStatus;

    /**
     * This is the user who is placing the order
     */
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "User_ID")
    private User user;
    /**
     * This is the administrator brac user who will approve the order
     */
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "Brac_User_ID")
    private User bracUser;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "biospecimenOrder", targetEntity = OrderManagerDocument.class, orphanRemoval = true)
    private List<OrderManagerDocument> documentList;

    /*
     * eventually need to also add the Comment object with @OneToMany annotation
     * 
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "biospecimenOrder", cascade = CascadeType.ALL, targetEntity = Comment.class, orphanRemoval = true)
    private List<Comment> commentList;

    public Long getId()
    {

        return this.id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getInvestigatorName()
    {

        return investigatorName;
    }

    public void setInvestigatorName(String investigatorName)
    {

        this.investigatorName = investigatorName;
    }

    public String getInstitution()
    {

        return institution;
    }

    public void setInstitution(String institution)
    {

        this.institution = institution;
    }

    public Address getAddress()
    {

        return address;
    }

    public void setAddress(Address address)
    {

        this.address = address;
    }

    public String getEmail()
    {

        return email;
    }

    public void setEmail(String email)
    {

        this.email = email;
    }

    public String getPhone()
    {

        return phone;
    }

    public void setPhone(String phone)
    {

        this.phone = phone;
    }

    public String getFax()
    {

        return fax;
    }

    public void setFax(String fax)
    {

        this.fax = fax;
    }

    public String getGrantTitle()
    {

        return grantTitle;
    }

    public void setGrantTitle(String grantTitle)
    {

        this.grantTitle = grantTitle;
    }

    public Integer getGrantNumber()
    {

        return grantNumber;
    }

    public void setGrantNumber(Integer grantNumber)
    {

        this.grantNumber = grantNumber;
    }

    public String getGrantPrincipalInvestigator()
    {

        return grantPrincipalInvestigator;
    }

    public void setGrantPrincipalInvestigator(String grantPrincipalInvestigator)
    {

        this.grantPrincipalInvestigator = grantPrincipalInvestigator;
    }

    public FundingStatus getFundingStatus()
    {

        return fundingStatus;
    }

    public void setFundingStatus(FundingStatus fundingStatus)
    {

        this.fundingStatus = fundingStatus;
    }

    public String getFundingAgency()
    {

        return fundingAgency;
    }

    public void setFundingAgency(String fundingAgency)
    {

        this.fundingAgency = fundingAgency;
    }

    public Collection<BiospecimenItem> getRequestedItems()
    {

        return requestedItems;
    }

    public void setRequestedItems(Collection<BiospecimenItem> requestedItems)
    {

        this.requestedItems = requestedItems;
    }

    public OrderStatus getOrderStatus()
    {

        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus)
    {

        this.orderStatus = orderStatus;
    }

    public User getUser()
    {

        return user;
    }

    public void setUser(User user)
    {

        this.user = user;
    }

    public String getOrderTitle()
    {

        return orderTitle;
    }

    public void setOrderTitle(String orderTitle)
    {

        this.orderTitle = orderTitle;
    }

    public List<Comment> getCommentList()
    {

        return commentList;
    }

    public void setCommentList(List<Comment> commentList)
    {

        this.commentList = commentList;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BiospecimenOrder other = (BiospecimenOrder) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else
            if (!id.equals(other.id))
                return false;
        return true;
    }

    public Date getDateCreated()
    {

        return dateCreated;
    }

    public void setDateCreated(Date dateCreated)
    {

        this.dateCreated = dateCreated;
    }

    public Date getDateSubmitted()
    {

        return this.dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted)
    {

        this.dateSubmitted = dateSubmitted;
    }

    public User getBracUser()
    {

        return bracUser;
    }

    public void setBracUser(User bracUser)
    {

        this.bracUser = bracUser;
    }

    public String getAbstractText()
    {

        return abstractText;
    }

    public void setAbstractText(String abstractText)
    {

        this.abstractText = abstractText;
    }

    public String getExperimentalDesignPowerAnalysis()
    {

        return experimentalDesignPowerAnalysis;
    }

    public void setExperimentalDesignPowerAnalysis(String experimentalDesignPowerAnalysis)
    {

        this.experimentalDesignPowerAnalysis = experimentalDesignPowerAnalysis;
    }

    public String getShipToName()
    {

        return shipToName;
    }

    public void setShipToName(String shipToName)
    {

        this.shipToName = shipToName;
    }

    public String getShipToInstitution()
    {

        return shipToInstitution;
    }

    public void setShipToInstitution(String shipToInstitution)
    {

        this.shipToInstitution = shipToInstitution;
    }

    public List<OrderManagerDocument> getDocumentList()
    {

        return documentList;
    }

    public void setDocumentList(List<OrderManagerDocument> documentList)
    {

        this.documentList = documentList;
    }

    public String getAffiliation()
    {

        return affiliation;
    }

    public void setAffiliation(String affiliation)
    {

        this.affiliation = affiliation;
    }

    public String getAffiliationPhone()
    {

        return affiliationPhone;
    }

    public void setAffiliationPhone(String affiliationPhone)
    {

        this.affiliationPhone = affiliationPhone;
    }

    public String getAffiliationEmail()
    {

        return affiliationEmail;
    }

    public void setAffiliationEmail(String affiliationEmail)
    {

        this.affiliationEmail = affiliationEmail;
    }

    public String getAffiliationSpecialInstructions()
    {

        return affiliationSpecialInstructions;
    }

    public void setAffiliationSpecialInstructions(String affiliationSpecialInstructions)
    {

        this.affiliationSpecialInstructions = affiliationSpecialInstructions;
    }

    public Set<BioRepository> getBioRepositoryList()
    {

        if (requestedItems == null || requestedItems.isEmpty())
        {
            return new HashSet<BioRepository>();
        }
        Set<BioRepository> list = new HashSet<BioRepository>();
        for (BiospecimenItem item : requestedItems)
        {
            if (item.getBioRepository() != null)
            {
                list.add(item.getBioRepository());
            }

        }
        return list;
    }

}
