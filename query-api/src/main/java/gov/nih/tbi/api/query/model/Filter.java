package gov.nih.tbi.api.query.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * Filter
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-02-13T14:19:40.675036-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Filter {
	/**
	 * Gets or Sets precedenceStart
	 */
	public enum PrecedenceStartEnum {
		DOUBLE_LEFT_PARENTHESIS("(("),

		LEFT_PARENTHESIS("(");

		private String value;

		PrecedenceStartEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static PrecedenceStartEnum fromValue(String text) {
			for (PrecedenceStartEnum b : PrecedenceStartEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("precedenceStart")
	private PrecedenceStartEnum precedenceStart = null;

	/**
	 * Gets or Sets precedenceEnd
	 */
	public enum PrecedenceEndEnum {
		DOUBLE_RIGHT_PARENTHESIS("))"),

		RIGHT_PARENTHESIS(")");

		private String value;

		PrecedenceEndEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static PrecedenceEndEnum fromValue(String text) {
			for (PrecedenceEndEnum b : PrecedenceEndEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("precedenceEnd")
	private PrecedenceEndEnum precedenceEnd = null;

	@JsonProperty("form")
	private String form = null;

	@JsonProperty("repeatableGroup")
	private String repeatableGroup = null;

	@JsonProperty("dataElement")
	private String dataElement = null;

	@JsonProperty("negation")
	private Boolean negation = false;

	@JsonProperty("operator")
	private Operator operator = Operator.AND;

	@JsonProperty("value")
	@Valid
	private List<String> value = null;

	@JsonProperty("rangeStart")
	private String rangeStart = null;

	@JsonProperty("rangeEnd")
	private String rangeEnd = null;

	@JsonIgnore
	private String name;

	/**
	 * Gets or Sets mode
	 */
	public enum ModeEnum {
		INCLUSIVE("inclusive"),

		EXACT("exact");

		private String value;

		ModeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static ModeEnum fromValue(String text) {
			for (ModeEnum b : ModeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("mode")
	private ModeEnum mode = ModeEnum.INCLUSIVE;

	/**
	 * Get precedenceStart
	 * 
	 * @return precedenceStart
	 **/
	@ApiModelProperty(value = "")

	public PrecedenceStartEnum getPrecedenceStart() {
		return precedenceStart;
	}

	public void setPrecedenceStart(PrecedenceStartEnum precedenceStart) {
		this.precedenceStart = precedenceStart;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get precedenceEnd
	 * 
	 * @return precedenceEnd
	 **/
	@ApiModelProperty(value = "")

	public PrecedenceEndEnum getPrecedenceEnd() {
		return precedenceEnd;
	}

	public void setPrecedenceEnd(PrecedenceEndEnum precedenceEnd) {
		this.precedenceEnd = precedenceEnd;
	}

	/**
	 * Get form
	 * 
	 * @return form
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	/**
	 * Get repeatableGroup
	 * 
	 * @return repeatableGroup
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public String getRepeatableGroup() {
		return repeatableGroup;
	}

	public void setRepeatableGroup(String repeatableGroup) {
		this.repeatableGroup = repeatableGroup;
	}

	/**
	 * Get dataElement
	 * 
	 * @return dataElement
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public String getDataElement() {
		return dataElement;
	}

	public void setDataElement(String dataElement) {
		this.dataElement = dataElement;
	}

	/**
	 * Get negation
	 * 
	 * @return negation
	 **/
	@ApiModelProperty(value = "")

	public Boolean isNegation() {
		return negation;
	}

	public void setNegation(Boolean negation) {
		this.negation = negation;
	}

	/**
	 * Get operator
	 * 
	 * @return operator
	 **/
	@ApiModelProperty(required = true, value = "")

	@Valid
	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	/**
	 * Get value
	 * 
	 * @return value
	 **/
	@ApiModelProperty(value = "")

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	/**
	 * Get rangeStart
	 * 
	 * @return rangeStart
	 **/
	@ApiModelProperty(value = "")

	public String getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(String rangeStart) {
		this.rangeStart = rangeStart;
	}

	/**
	 * Get rangeEnd
	 * 
	 * @return rangeEnd
	 **/
	@ApiModelProperty(value = "")

	public String getRangeEnd() {
		return rangeEnd;
	}

	public void setRangeEnd(String rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	/**
	 * Get mode
	 * 
	 * @return mode
	 **/
	@ApiModelProperty(value = "")

	public ModeEnum getMode() {
		return mode;
	}

	public void setMode(ModeEnum mode) {
		this.mode = mode;
	}

}
