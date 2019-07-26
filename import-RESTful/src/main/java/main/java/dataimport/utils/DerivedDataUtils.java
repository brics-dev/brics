package main.java.dataimport.utils;

import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.ordermanager.model.DerivedBiosampleConfigurations;
import gov.nih.tbi.ordermanager.model.DerivedBiosampleRepository;

import java.util.ArrayList;
import java.util.List;

import main.java.dataimport.exception.BiosampleRepositoryNotFound;

public class DerivedDataUtils {

	public static List<String> getDerivedDataColumns(DerivedBiosampleConfigurations configurations,
			String repositoryName) throws BiosampleRepositoryNotFound {

		DerivedBiosampleRepository currentRepository =
				getBiosampleRepositoryConfiguration(configurations, repositoryName);
		return new ArrayList<String>(currentRepository.getDataElementColumnMapping().values());
	}

	public static DerivedBiosampleRepository getBiosampleRepositoryConfiguration(
			DerivedBiosampleConfigurations configurations, String repositoryName) throws BiosampleRepositoryNotFound {

		for (DerivedBiosampleRepository repositoryConfiguration : configurations.getDerivedBiosampleRepositories()) {
			if (repositoryName.equalsIgnoreCase(repositoryConfiguration.getName())) {
				return repositoryConfiguration;
			}
		}

		throw new BiosampleRepositoryNotFound("Biosample repository with name, " + repositoryName
				+ " does not exist or is not configured.");
	}

	public static String[] escapeLine(String[] line) {
		for (int i = 0; i < line.length; i++) {
			String currentValue = line[i];
			if (currentValue.contains(ServiceConstants.COMMA)
					&& !(currentValue.startsWith(ServiceConstants.QUOTE) && currentValue
							.endsWith(ServiceConstants.QUOTE))) {
				currentValue = ServiceConstants.QUOTE + currentValue + ServiceConstants.QUOTE;
			}

			line[i] = currentValue;
		}

		return line;
	}
}
