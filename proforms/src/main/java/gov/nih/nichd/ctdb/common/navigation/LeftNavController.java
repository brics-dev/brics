package gov.nih.nichd.ctdb.common.navigation;

import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.rs;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Represents the left navigation links container for IBIS.  Contains a list of
 * MainNavLink elements that, themselves, contain lists of sublinks.
 * 
 * @author jpark1
 *
 */
public class LeftNavController extends BaseAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8624743769221599967L;
	private ArrayList<SubNavLink> links;
	private HttpServletRequest request;
	private int highlightedLink = -1;
	private int[] disabledLinks;
	
	public static final int LEFTNAV_HOME = 1;
	public static final int LEFTNAV_PICKSTUDY = 2;
	public static final int LEFTNAV_SUBJECTS_MANAGE = 3;
	public static final int LEFTNAV_SUBJECTS_ADD = 4;
	public static final int LEFTNAV_SUBJECTS_VISITS = 5;
	public static final int LEFTNAV_SUBJECTS_QUERIES = 6;
	public static final int LEFTNAV_SUBJECTS_ATTACHMENT = 7;
	public static final int LEFTNAV_COLLECT = 8;
	public static final int LEFTNAV_COLLECT_COLLECTIONS = 9;
	public static final int LEFTNAV_COLLECT_DISCREPANCIES = 10;
	public static final int LEFTNAV_FORM_HOME = 11;
	public static final int LEFTNAV_FORM_FORMS = 12;
	public static final int LEFTNAV_FORM_CREATE = 13;
	public static final int LEFTNAV_FORM_GROUP = 14;
	public static final int LEFTNAV_STUDY_HOME = 15;
	public static final int LEFTNAV_STUDY_DETAILS = 16;
	public static final int LEFTNAV_STUDY_ROLES = 17;
	public static final int LEFTNAV_STUDY_INTERVAL = 18;
	public static final int LEFTNAV_STUDY_DOCUMENTS = 19;
	public static final int LEFTNAV_STUDY_CONTACTS = 20;
	public static final int LEFTNAV_QUERY_DISCREPANCY = 21;
	public static final int LEFTNAV_QUERY_FORMSTATUS = 23;
	public static final int LEFTNAV_QUERY_COMPLETED = 24;
	public static final int LEFTNAV_SUBJECTS_MYSUBJECTS = 25;
	public static final int LEFTNAV_COLLECT_COLLECT = 26;
	public static final int LEFTNAV_QUERY_QUERY = 27;
	public static final int LEFTNAV_QUERY_STUDY = 28;
	public static final int LEFTNAV_ADMIN_HOME = 29;
	public static final int LEFTNAV_ADMIN_USER = 30;
	public static final int LEFTNAV_ADMIN_ROLES = 31;
	public static final int LEFTNAV_ADMIN_URLS = 32;
	public static final int LEFTNAV_ADMIN_REGS = 33;
	public static final int LEFTNAV_STUDY_EBINDER = 34;
	public static final int LEFTNAV_FORM_COPY = 35;
	public static final int LEFTNAV_STUDY_CREATEINTERVAL = 36;
	public static final int LEFTNAV_QUERY_FORMS_REQ_LOCK = 37;
	public static final int LEFTNAV_QUERY_PERFORMANCE_OVERVIEW = 38;
	public static final int LEFTNAV_QUERY_COMPLETED_VISITS = 39;
	public static final int LEFTNAV_FORM_IMPORT = 40;
	public static final int LEFTNAV_STATS_ARMS_RANDOMIZE = 41;
	public static final int LEFTNAV_QUERY_SUBMISSION_SUMMARY = 42;
	public static final int LEFTNAV_SUBJECTS_HELP = 43;
	public static final int LEFTNAV_COLLECT_HELP = 44;
	public static final int LEFTNAV_FORM_HELP = 45;
	public static final int LEFTNAV_STUDY_HELP = 46;
	public static final int LEFTNAV_SUBJECT_MATRIX_DASHBORAD = 47;
	public static final int LEFTNAV_QUERY_GUIDS_WITHOUT_COLLECTIONS = 48;
	public static final int LEFTNAV_ADMIN_FORM_SUBMISSION = 49;
	public static final int LEFTNAV_PSREFORMS_CONFIGURE = 50;
	public static final int LEFTNAV_SCHEDULE = 51;
	public static final int LEFTNAV_STUDY_ORDER_INTERVAL = 52;
	public static final int LEFTNAV_STUDY_CLOSE_OUT = 53;
	
	public LeftNavController(HttpServletRequest request, User user) throws Exception {
		links = new ArrayList<SubNavLink>();
		setRequest(request);
		buildTree(request, user);
	}
	
	public LeftNavController(ArrayList<SubNavLink> linkList, HttpServletRequest request, User user) throws Exception {
		links = linkList;
		setRequest(request);
		buildTree(request, user);
	}

	public ArrayList<SubNavLink> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<SubNavLink> links) {
		this.links = links;
	}
	
	public int getHighlightedLink() {
		return highlightedLink;
	}

	public void setHighlightedLink(int highlightedLink) {
		this.highlightedLink = highlightedLink;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public int[] getDisabledLinks() {
		return disabledLinks;
	}

	public void setDisabledLinks(int[] disabledLinks) {
		this.disabledLinks = disabledLinks;
	}

	/**
	 * Gets a flattened list of all links in the tree under this one.
	 * 
	 * @return ArrayList of all sub links under this menu
	 */
	public ArrayList<SubNavLink> allSubLinks() {
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[links.size()]; 
		arrLinks = links.toArray(arrLinks);
		ArrayList<SubNavLink> output = new ArrayList<SubNavLink>();
		for(SubNavLink subLink : arrLinks) {
			output.add(subLink);
			output.addAll(subLink.getAllSubLinks());
		}
		return output;
	}
	
	public void addLink(SubNavLink link) 
			throws Exception {
		addLink(null, link);
	}
	
	public void addLink(String parentLinkText, SubNavLink link) 
			throws Exception {
		if (parentLinkText == null) {
			// check for duplicate
			SubNavLink existsLink = findLink(link.getLinkText(), link.getUrl());
			if (existsLink == null) {
				links.add(link);
			}
		}
		else {
			ArrayList<SubNavLink> subLinks = allSubLinks();
			for (SubNavLink subLink : subLinks) {
				if (subLink.getLinkText().equals(parentLinkText)) {
					if (findLink(link.getLinkText(), link.getUrl()) == null) {
						subLink.addLink(link);
					}
				}
			}
		}
	}
	
	/**
	 * Searches the link list for the link with the given link text.
	 * Will return NULL if it finds more than one!
	 * 
	 * @param linkText the display text of the link
	 * @return SubNavLink link upon finding a match or null if not found or multiple
	 */
	public SubNavLink findLink(String linkText) {
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[links.size()]; 
		arrLinks = links.toArray(arrLinks);
		SubNavLink matchLink = null;
		for (SubNavLink link : arrLinks) {
			if (link.getLinkText().equals(linkText)) {
				if (matchLink == null) {
					matchLink = link;
				}
				else {
					return null;
				}
			}
		}
		return matchLink;
	}
	
	public SubNavLink findLink(String linkText, String linkUrl) {
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[links.size()]; 
		arrLinks = links.toArray(arrLinks);
		for (SubNavLink link : arrLinks) {
			if (link.getLinkText().equals(linkText) && link.getUrl().equals(linkUrl)) {
				return link;
			}
		}
		return null;
	}
	
	public SubNavLink findLink(SubNavLink testLink) {
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[links.size()]; 
		arrLinks = links.toArray(arrLinks); 
		for (SubNavLink link : arrLinks) {
			if (link.equals(testLink)) {
				return link;
			}
		}
		return null;
	}
	
	public SubNavLink findLinkInAll(String linkText, String linkUrl) {
		SubNavLink testLink = findLink(linkText, linkUrl);
		if (testLink != null) {
			return testLink;
		}
		for (SubNavLink link : allSubLinks()) {
			if (link.getLinkText().equals(linkText) && link.getUrl().equals(linkUrl)) {
				return link;
			}
		}
		return null;
	}
	
	public SubNavLink findLinkInAll(SubNavLink link) {
		SubNavLink testLink = findLink(link);
		if (testLink != null) {
			return testLink;
		}
		for (SubNavLink linkLoop : allSubLinks()) {
			if (linkLoop.equals(link)) {
				return linkLoop;
			}
		}
		return null;
	}
	
	public void updateLink(SubNavLink original, SubNavLink newLink) {
		SubNavLink existsLink = findLinkInAll(original.getLinkText(), original.getUrl());
		if (existsLink != null) {
			existsLink.setLinkText(newLink.getLinkText());
			existsLink.setUrl(newLink.getUrl());
			existsLink.setPermissions(newLink.getPermissions());
		}
	}
	
	public void clear() {
		links.clear();
	}
	
	private void buildTree(HttpServletRequest request, User user) throws Exception {
		SubNavLink headerLink = null;
		Protocol study = (Protocol) request.getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		long studyId = (study == null) ? -1 : study.getId();
		Locale l = request.getLocale();

		String appNameNonPii = rs.getValue("application.name.nonPii", l);
		String appNamePii = rs.getValue("application.name.pii", l);
		String allowPii = SysPropUtil.getProperty("guid_with_non_pii");
		String appName = (allowPii.equals("0")) ? appNamePii : appNameNonPii;
		Boolean enableVisitTypeScheduler = Boolean.valueOf(SysPropUtil.getProperty("enable.visittype.scheduler"));
		Boolean enableScheduleReport = Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"));

		boolean isPDBP = SysPropUtil.getProperty("template.global.appName").equals("pdbp");
		headerLink = new SubNavLink(rs.getValue("nav.Home", l, appName), "pickStudy.action?id=0",
				LeftNavController.LEFTNAV_HOME);
		addLink(null, headerLink);
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("protocol.list.title.display", l),
				"pickStudy.action?id=0", LeftNavController.LEFTNAV_PICKSTUDY));

		headerLink = new SubNavLink(rs.getValue("nav.ManageSubjects", l), "patient/patientHome.action",
				LeftNavController.LEFTNAV_SUBJECTS_MANAGE, new String[] {"viewpatients"});
		addLink(null, headerLink);
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("subject.table.title.display", l),
				"patient/patientHome.action", LeftNavController.LEFTNAV_SUBJECTS_MYSUBJECTS));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("patient.add.title.display", l), "patient/showAddPatient.action",
						LeftNavController.LEFTNAV_SUBJECTS_ADD, new String[] {"addeditpatients"}));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("patient.scheduleVisit.title", l), "patient/patientVisitHome.action",
						LeftNavController.LEFTNAV_SUBJECTS_VISITS, new String[] {"addeditschedulevisits"}));

		headerLink = new SubNavLink(rs.getValue("nav.CollectData", l), "response/dataCollectingLandingSearch.action",
				LeftNavController.LEFTNAV_COLLECT);
		addLink(null, headerLink);
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("response.collect.title.display", l),
				"response/dataCollectingLandingSearch.action", LeftNavController.LEFTNAV_COLLECT_COLLECT));
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("response.collect.myCollections.title.display", l),
				"response/collectDataPreviousHome.action", LeftNavController.LEFTNAV_COLLECT_COLLECTIONS));

		headerLink = new SubNavLink(rs.getValue("nav.ManageStudy", l),
				"protocol/showStudy.action?studyId=" + Long.toString(studyId), LeftNavController.LEFTNAV_STUDY_HOME);
		addLink(null, headerLink);
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("study.info.title.display", l),
						"/protocol/showStudy.action?studyId=" + Long.toString(studyId),
						LeftNavController.LEFTNAV_STUDY_DETAILS, new String[] {"viewprotocols"}));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("study.roles.title.display", l), "/protocol/protocolUser.action",
						LeftNavController.LEFTNAV_STUDY_ROLES, new String[] {"assignuserstoprotocol"}));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("protocol.visitType.create.title.display", l),
						"/protocol/editVisitType.action", LeftNavController.LEFTNAV_STUDY_CREATEINTERVAL,
						new String[] {"addeditvisittypes"}));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("protocol.visitType.manage.title.display", l),
						"/protocol/visitTypeHome.action", LeftNavController.LEFTNAV_STUDY_INTERVAL,
						new String[] {"viewvisittypes"}));

		if (enableVisitTypeScheduler) {
			addLink(headerLink.getLinkText(),
					new SubNavLink(rs.getValue("protocol.visitType.order.title.display", l),
							"/protocol/orderVisitType.action", LeftNavController.LEFTNAV_STUDY_ORDER_INTERVAL,
							new String[] {"ordervisittypes"}));
		}

		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("protocol.psr.eforms.configure.title.display", l),
						"/protocol/configurePSReFormsHome.action", LeftNavController.LEFTNAV_PSREFORMS_CONFIGURE,
						new String[] {"viewPSR"}));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("ebinder.title.display", l), "/protocol/studyEbinder.action",
						LeftNavController.LEFTNAV_STUDY_EBINDER, new String[] {"viewstudyebinder"}));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("protocol.study.closeout", l), "/protocol/protocolCloseout.action",
						LeftNavController.LEFTNAV_STUDY_CLOSE_OUT, new String[] {"protocolcloseout"}));

		headerLink = new SubNavLink(rs.getValue("nav.ReportAndQuery", l), "response/studyReport.action",
				LeftNavController.LEFTNAV_QUERY_QUERY);
		addLink(null, headerLink);
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.studyReport", l),
				"response/studyReport.action", LeftNavController.LEFTNAV_QUERY_STUDY));

		// Apply these two links only on the PDBP version of the site.
		if (isPDBP) {
			addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.CompletedVisitsReport", l),
					"response/completedVisits.action", LeftNavController.LEFTNAV_QUERY_COMPLETED_VISITS));
			addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.PerformanceOverviewReport", l),
					"response/performanceOverview.action", LeftNavController.LEFTNAV_QUERY_PERFORMANCE_OVERVIEW));

		}
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.guidsWithoutCollectionsReport", l),
				"response/guidsWithoutCollections.action", LeftNavController.LEFTNAV_QUERY_GUIDS_WITHOUT_COLLECTIONS));
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.FormsRequiringLockReport", l),
				"response/formsRequiringLock.action", LeftNavController.LEFTNAV_QUERY_FORMS_REQ_LOCK));
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.CompletedFormsReport", l),
				"response/completedForms.action", LeftNavController.LEFTNAV_QUERY_COMPLETED));
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.submissionSummary.title.display", l),
				"response/submissionSummary.action", LeftNavController.LEFTNAV_QUERY_SUBMISSION_SUMMARY));
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.form.status.id.guid.mrn", l),
				"response/subjectMatrixDashboard.action", LeftNavController.LEFTNAV_SUBJECT_MATRIX_DASHBORAD));

		String[] schedullerPrivArr = {"scheduller", "pischeduller"};
		if (enableScheduleReport && user != null && user.hasAnyPrivilege(schedullerPrivArr)) {
			addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("report.schedule", l),
					"response/scheduleHome.action", LeftNavController.LEFTNAV_SCHEDULE));
		}

		headerLink = new SubNavLink(rs.getValue("nav.SiteAdmin", l), "admin/userHome.action",
				LeftNavController.LEFTNAV_ADMIN_HOME, new String[] {"sysadmin"});
		addLink(null, headerLink);
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("admin.users.display", l), "admin/userHome.action",
				LeftNavController.LEFTNAV_ADMIN_USER, new String[] {"sysadmin"}));
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("admin.roles.display", l),
				"admin/rolesAdmin.action", LeftNavController.LEFTNAV_ADMIN_ROLES, new String[] {"sysadmin"}));
		addLink(headerLink.getLinkText(), new SubNavLink(rs.getValue("admin.links.display", l),
				"admin/siteLinkAdmin.action", LeftNavController.LEFTNAV_ADMIN_URLS, new String[] {"sysadmin"}));
		addLink(headerLink.getLinkText(),
				new SubNavLink(rs.getValue("admin.form.submit", l), "admin/formSubmissionAdmin.action",
						LeftNavController.LEFTNAV_ADMIN_FORM_SUBMISSION, new String[] {"sysadmin"}));
	}
	
	/**
	 * Determines if the link or one of the links below it is highlighted
	 * 
	 * @param link the link to check
	 * @return boolean true if this link or one below it is highlighted; otherwise false
	 */
	public boolean isHighlighted(SubNavLink link) {
		if (this.highlightedLink == -1) {
			return false;
		}
		if (link.getNickname() == this.highlightedLink) {
			return true;
		}
		for (SubNavLink sublink : link.getAllSubLinks()) {
			if (sublink.getNickname() == this.highlightedLink) {
				return true;
			}
		}
		return false;
	}
	
	public void save(HttpServletRequest request) {
		request.setAttribute(CtdbConstants.NAVIGATION_LEFTNAV_KEY, this);
	}
	public void save() {
		request.setAttribute(CtdbConstants.NAVIGATION_LEFTNAV_KEY, this);
	}
}
