package gov.nih.tbi.dictionary.validation.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.WebstartRestProvider;
import gov.nih.tbi.dictionary.model.Translations;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.dictionary.ws.validation.DictionaryAccessor;

public class FileParser { // extends SwingWorker<DataSubmission, Integer>{

	private static Logger logger = Logger.getLogger(FileParser.class);

	File directory;
	WebstartRestProvider ddtClient;
	ExecutorService executorService;
	HashSet<String> structNames;
	HashMap<FileNode, BufferedReader> leafNodes;
	DataSubmission submission;
	SwingWorker worker;
	int progressMax = 0;

	int N_CPU = Runtime.getRuntime().availableProcessors() - 1;
	float U_CPU = (float) 0.75;

	public FileParser(WebstartRestProvider ddtClient, File file) {

		directory = file;
		this.ddtClient = ddtClient;
		executorService = Executors.newFixedThreadPool(getThreadCount());
		structNames = new HashSet<String>();
		leafNodes = new HashMap<FileNode, BufferedReader>();

	}

	public FileParser(WebstartRestProvider ddtClient, DataSubmission submission) {

		this.submission = submission;
		this.ddtClient = ddtClient;
		executorService = Executors.newFixedThreadPool(getThreadCount());
		structNames = new HashSet<String>();
		leafNodes = new HashMap<FileNode, BufferedReader>();
		// TODO Auto-generated constructor stub
	}

	private int getThreadCount() {

		int count = Math.round(N_CPU * U_CPU);
		if (count <= 0) {
			return 1;
		} else {
			return count;
		}
	}

	public void setWorker(SwingWorker worker) {

		this.worker = worker;
	}

	/**
	 * This function takes a list of FileNodes uses them to rebuild a section of the submission
	 * 
	 * @return
	 */
	public DataSubmission buildPartialSubmission(List<FileNode> fileNodes) {

		structNames.clear();
		leafNodes.clear();

		// Build a set from the list of files
		Set<FileNode> filesToReload = new LinkedHashSet<FileNode>();
		for (FileNode f : fileNodes) {
			buildFilesToReloadSet(f, filesToReload);
		}

		// This for loops: open the files for the selected file nodes, create a buffered reader, and
		// read the struct name/version from the file.
		for (FileNode f : filesToReload) {
			// Existence check
			String absPath = f.getConicalPath();
			BufferedReader fileReader;
			File file;
			try {
				file = new File(absPath);
				fileReader = new BufferedReader(new FileReader(file));
				leafNodes.put(f, fileReader);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.error("Could not find path " + f.getConicalPath() + ". This file will not be reloaded");
				continue;
			}

			// Reset File type
			if (file.length() == 0) {
				f.setType(FileType.UNKNOWN);
			} else if (absPath.toLowerCase().endsWith(".txt")) {
				parseDelimFile(file, (FileNode) f.getParent(), absPath, f.getType(), fileReader, "[\t]");
			} else if (absPath.toLowerCase().endsWith(".csv")) {
				parseDelimFile(file, (FileNode) f.getParent(), absPath, f.getType(), fileReader, "[,]");
			} else {
				f.setType(FileType.UNKNOWN);
			}

			// New size and CRC
			// Add CRC and File Size to NODE
			f.setFileSize(file.length());

			FileReader fr;
			try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");

				fr = new FileReader(file);
				int bytesRead = 0;
				while (fr.ready() && bytesRead < ModelConstants.MD5_HASH_SIZE) {
					int input = fr.read();
					md5.update((byte) input);
					bytesRead++;
				}

				f.setCrcHash("" + new BigInteger(1, md5.digest()).toString(16));

			} catch (FileNotFoundException e) {
				logger.debug("failed to find the file ( " + f.getConicalPath() + " ) during CRC check.");
			} catch (IOException e) {
				logger.debug("failed to read from the file ( " + f.getConicalPath() + " ) during CRC check");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Populate the dictionary using the structure names
		String[] names = new String[structNames.size()];
		List<StructuralFormStructure> dictionary =
				WebstartRestProvider.getDataDictionary(ddtClient, structNames.toArray(names));
		Collection<Callable<BufferedReader>> tasks = new ArrayList<Callable<BufferedReader>>();

		// Add new dictionary items to the dictionary list
		for (StructuralFormStructure newDS : dictionary) {
			for (StructuralFormStructure oldDS : submission.getDictionary()) {
				if (oldDS.getShortName().equals(newDS.getShortName())) {
					submission.getDictionary().remove(oldDS);
					break;
				}
			}
			submission.getDictionary().add(newDS);
		}

		// All of the files we want to reload are iterated over.
		// Check existence, structure, version, and add the data to the tasks list
		for (FileNode f : filesToReload) {
			if (f.getType().equals(FileType.UNKNOWN)) {
				continue;
			}

			String name = f.getStructureDisplay();
			StructuralFormStructure structure = DictionaryAccessor.getDataStructureByName(dictionary, name);

			if (structure != null) {
				tasks.add(new TableBuilder(f, leafNodes.get(f), structure, submission.getFileData(), worker));

				submission.getStructureData().putIfAbsent(structure.getShortName(), new Vector<FileNode>());
			} else {
				f.setStructureName(null);
				f.setType(FileType.UNKNOWN);
			}

		}

		// Run all the table building tasks
		try {
			for (Future<BufferedReader> future : executorService.invokeAll(tasks)) {
				future.get().close();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// XXX: This may be able to close or it might break things
			for (Callable<BufferedReader> o : tasks) {
				TableBuilder tb = (TableBuilder) o;

				try {
					tb.getReader().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return submission;
	}

	/**
	 * A recursive helper. All the children from f are added to set.
	 * 
	 * @param node
	 * @param set
	 */
	private void buildFilesToReloadSet(FileNode node, Set<FileNode> set) {

		if (node.isDirectory()) {
			for (FileNode f : node.getChildren()) {
				buildFilesToReloadSet(f, set);
			}
		} else {
			set.add(node);
		}
	}

	public DataSubmission buildSubmission() {

		structNames.clear();
		leafNodes.clear();

		// This function will leave you with all the csv/tab files as FileNodes in leafNodes and all the structs used in
		// structNames.
		// Root will also be the FileNode for the top directory.
		FileNode root = createFileTree(directory, null);

		String[] names = new String[structNames.size()];

		List<StructuralFormStructure> dictionary =
				WebstartRestProvider.getDataDictionary(ddtClient, structNames.toArray(names));

		ConcurrentHashMap<FileNode, DataStructureTable> dataMap = new ConcurrentHashMap<FileNode, DataStructureTable>();
		ConcurrentHashMap<String, Vector<FileNode>> structMap = new ConcurrentHashMap<String, Vector<FileNode>>();
		Collection<Callable<BufferedReader>> tasks = new ArrayList<Callable<BufferedReader>>();

		for (FileNode node : leafNodes.keySet()) {

			if (FileType.TRANSLATION_RULE.equals(node.getType())) {
				dataMap.put(node, new DataStructureTable());
			} else {
				String name = node.getStructureDisplay();
				StructuralFormStructure structure = DictionaryAccessor.getDataStructureByName(dictionary, name); // dictionary.getDataStructureByName(name);

				if (structure != null) {
					tasks.add(new TableBuilder(node, leafNodes.get(node), structure, dataMap, worker));

					structMap.putIfAbsent(structure.getShortName(), new Vector<FileNode>());
					structMap.get(structure.getShortName()).add(node);
					// set file status in file node.
					node.setFsStatus(structure.getStatus().getType());
				} else {
					node.setStructureName(null);
					if (node.getType() == FileType.CSV) {
						node.setType(FileType.UNKNOWN);
					}
				}
			}
		}

		// Before launching the tasks, change the progress bar to a loading one.
		worker.firePropertyChange("max", 0, progressMax);

		try {
			for (Future<BufferedReader> f : executorService.invokeAll(tasks)) {
				f.get().close();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// XXX: This may be able to close or it might break things
			for (Callable<BufferedReader> o : tasks) {
				TableBuilder tb = (TableBuilder) o;

				try {
					tb.getReader().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return new DataSubmission(root, dataMap, structMap, dictionary);
	}

	/**
	 * Uses JAXB to unmarshall the translation rule.xml into FormTranslation object.
	 * 
	 * @return Unmarshalled form translation.
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static Translations buildTranslations(String path) throws JAXBException, IOException {

		FileInputStream in;

		try {
			in = new FileInputStream(new File(path));
		} catch (FileNotFoundException e) {
			// can continue without a translation rule.xml
			return null;
		}

		JAXBContext jc = JAXBContext.newInstance(Translations.class);
		Unmarshaller um = jc.createUnmarshaller();
		Translations translationRule = (Translations) um.unmarshal(in);
		in.close();
		return translationRule;
	}

	// Populates structNames and leafNodes
	private FileNode createFileTree(File file, FileNode parent) {

		FileNode node;
		// If this file is a directory, then create a DIR type file node and recursively run through all its children
		if (file.isDirectory()) {
			node = new FileNode(file.getAbsolutePath(), file.getName(), FileType.DIR, parent);
			for (File child : file.listFiles()) {
				if (!child.isHidden()) {
					node.insert(createFileTree(child, node));
				}
			}
		}
		// This file is not a directory
		else {

			String absPath = file.getAbsolutePath();
			BufferedReader fileReader;
			try {

				// We want to count lines but we also don't want to count an entire file if its not a data file.
				if (absPath.toLowerCase().endsWith(".txt") || absPath.toLowerCase().endsWith(".csv")
						|| absPath.toLowerCase().endsWith(".xml")) {
					fileReader = new BufferedReader(new FileReader(file));
					String firstLine = fileReader.readLine();
					if (firstLine != null && firstLine.matches("[A-Za-z0-9_]{1,30}[,\\t][0-9]{1,2}[,\\t]*")) {
						int readChar = 0;
						while ((readChar = fileReader.read()) != -1) {
							if (readChar == '\n')
								progressMax++;
						}
						fileReader.close();
					}
				}
				// /////////////////////////////////////

				fileReader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null; // Do not create nodes for missing files
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			// check to see if the file is empty
			if (file.length() == 0) {
				node = new FileNode(absPath, file.getName(), FileType.UNKNOWN, parent);
				// node.exclude();
			}
			// TODO: Add support for XML file parsing
			// Parse this file
			else if (absPath.toLowerCase().endsWith(".txt")) {
				// node = parseDelimFile(file, parent, absPath, FileType.TAB, fileReader, "[\t]");
				node = parseDelimFile(file, parent, absPath, FileType.UNKNOWN, fileReader, "[\t]");
			} else if (absPath.toLowerCase().endsWith(".csv")) {
				node = parseDelimFile(file, parent, absPath, FileType.CSV, fileReader, "[,]");
			} else if (absPath.toLowerCase().endsWith(".xml")) {

				try { // try to unmarshall the xml into translation rule to see if it is indeed in the proper
						// format
					buildTranslations(absPath);
					node = new FileNode(absPath, file.getName(), FileType.TRANSLATION_RULE, parent);
					leafNodes.put(node, fileReader);
				} catch (Exception e) { // TODO: when XML is supported, add the parsing for it here
					node = new FileNode(absPath, file.getName(), FileType.UNKNOWN, parent);
					// node.exclude();
				}
			} else {
				node = new FileNode(absPath, file.getName(), FileType.UNKNOWN, parent);
				/*
				 * if(!node.isImageType()) node.exclude();
				 */
			}

			// If this file is recognized as a datafile, submissionticket, or outputlog (by title), then exclude it
			String name = file.getName();
			if (name.matches("dataFile\\-[0-9]{13}\\.xml") || name.matches("submissionTicket\\-[0-9]{13}\\.xml")
					|| name.matches("output_log_[0-9]{13}\\.txt")) {
				node.exclude();
			}

			// Add CRC and File Size to NODE
			node.setFileSize(file.length());

			FileReader fr;
			try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");

				fr = new FileReader(file);
				int bytesRead = 0;
				while (fr.ready() && bytesRead < ModelConstants.MD5_HASH_SIZE) {
					int input = fr.read();
					md5.update((byte) input);
					bytesRead++;
				}

				node.setCrcHash("" + new BigInteger(1, md5.digest()).toString(16));

			} catch (FileNotFoundException e) {
				logger.debug("failed to find the file ( " + node.getConicalPath() + " ) during CRC check.");
			} catch (IOException e) {
				logger.debug("failed to read from the file ( " + node.getConicalPath() + " ) during CRC check");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return node;
	}

	private FileNode parseDelimFile(File file, FileNode parent, String absPath, FileType type,
			BufferedReader fileReader, String delim) {

		FileNode node;
		node = new FileNode(absPath, file.getName(), type, parent);

		if (delim != null) {
			try {
				String[] line = new String[0];
				while (line.length == 0) {
					line = fileReader.readLine().split(delim); // throws a exception if you get to the end
				}
				String name = line[0].trim();

				// name = name.toLowerCase();
				structNames.add(name);
				node.setStructureName(name);
				leafNodes.put(node, fileReader);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return node;
	}

}
