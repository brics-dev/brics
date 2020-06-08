package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class ProtocolRandomization extends CtdbDomainObject{
		private static final long serialVersionUID = 6966151441388036139L;
		
		private long protocolId = Integer.MIN_VALUE;;
		private long sequence = Integer.MIN_VALUE;;
		private String groupName = "";
		private String groupDescription = "";
		
		public ProtocolRandomization() {
			super();
		}
		
		public long getProtocolId() {
			return this.protocolId;
		}
		public void setProtocolId(long protocolId) {
			this.protocolId = protocolId;
		}
		public long getSequence() {
			return this.sequence;
		}
		public void setSequence(long sequence) {
			this.sequence = sequence;
		}
		public String getGroupName() {
			return this.groupName;
		}
		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}
		public String getGroupDescription() {
			return this.groupDescription;
		}
		public void setGroupDescription(String groupDescription) {
			this.groupDescription = groupDescription;
		}

		@Override
		public Document toXML() throws TransformationException {
			try	{
				Document document = super.newDocument();
				Element root = super.initXML(document, "protocolrandomization");

				Element protRandomizationIdNode = document.createElement("protRandomizationId");
				protRandomizationIdNode.appendChild(document.createTextNode(String.valueOf(this.getId())));
				root.appendChild(protRandomizationIdNode);

				Element protocolIdNode = document.createElement("protocolId");
				protocolIdNode.appendChild(document.createTextNode(String.valueOf(this.protocolId)));
				root.appendChild(protocolIdNode);
				
				Element sequenceNode = document.createElement("sequence");
				sequenceNode.appendChild(document.createTextNode(String.valueOf(this.sequence)));
				root.appendChild(sequenceNode);
							
				Element groupNameNode = document.createElement("groupName");
				groupNameNode.appendChild(document.createTextNode(this.groupName));
				root.appendChild(groupNameNode);
				
				Element groupDescriptionNode = document.createElement("groupDescription");
				groupDescriptionNode.appendChild(document.createTextNode(this.groupDescription));
				root.appendChild(groupDescriptionNode);

				return document;

			} catch (Exception ex) {
				throw new UnsupportedOperationException("toXML() not supported in ProtocolRandomization.");
			}
		}

}
