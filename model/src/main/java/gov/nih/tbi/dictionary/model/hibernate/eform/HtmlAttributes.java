package gov.nih.tbi.dictionary.model.hibernate.eform;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * HtmlAttributes implements different type of HTML attributes
 * used when displaying data to the user. The attributes
 * format alignment, fonts, and coloring.
 */
@Entity
@Table(name = "HTMLATTRIBUTES")
@XmlRootElement(name = "htmlAttributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlAttributes {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EFORM_SEQ")
	@SequenceGenerator(name = "EFORM_SEQ", sequenceName = "EFORM_SEQ", allocationSize = 1)
	@XmlElement(name="eformId")
	private Long id;
	private boolean formBorder = true;
    private boolean sectionBorder = true;
    private String formColor = "Black";
    private String sectionColor = "Black";
    private String formFont = "Arial";
    private String sectionFont = "Arial";
    private int formFontSize = 10;
    private int cellpadding = 2;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean getFormBorder() {
		return formBorder;
	}
	public void setFormBorder(boolean formBorder) {
		this.formBorder = formBorder;
	}
	public boolean getSectionBorder() {
		return sectionBorder;
	}
	public void setSectionBorder(boolean sectionBorder) {
		this.sectionBorder = sectionBorder;
	}
	public String getFormColor() {
		return formColor;
	}
	public void setFormColor(String formColor) {
		this.formColor = formColor;
	}
	public String getSectionColor() {
		return sectionColor;
	}
	public void setSectionColor(String sectionColor) {
		this.sectionColor = sectionColor;
	}
	public String getFormFont() {
		return formFont;
	}
	public void setFormFont(String formFont) {
		this.formFont = formFont;
	}
	public String getSectionFont() {
		return sectionFont;
	}
	public void setSectionFont(String sectionFont) {
		this.sectionFont = sectionFont;
	}
	public int getFormFontSize() {
		return formFontSize;
	}
	public void setFormFontSize(int formFontSize) {
		this.formFontSize = formFontSize;
	}
	public int getCellpadding() {
		return cellpadding;
	}
	public void setCellpadding(int cellpadding) {
		this.cellpadding = cellpadding;
	}

	
	
}

