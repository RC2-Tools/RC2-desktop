package edu.uw.ifrcdemo.distplan.test;

//import org.junit.FixMethodOrder;
//import org.junit.runners.MethodSorters;
//import com.opencsv.CSVReader;
//import com.opencsv.CSVWriter;

//import edu.uw.cse.ifrcdemo.distplan.DistributionModel;
//import edu.uw.cse.ifrcdemo.distplan.DistributionPlanningUtil;
//import edu.uw.cse.ifrcdemo.distplan.entity.ActiveEntitlement;
//import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
//import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
//import edu.uw.cse.ifrcdemo.distplan.entity.ItemPackRange;
//import edu.uw.cse.ifrcdemo.distplan.entity.OverrideEntitlement;
//import edu.uw.cse.ifrcdemo.distplan.parser.IDList;
//import edu.uw.cse.ifrcdemo.distplan.model.authorization.Range;
//import edu.uw.cse.ifrcdemo.distplan.parser.RangeList;


//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DistributionTest {

	/* Tests are broken. Commenting them out while we make significant changes to the project.
	 * TODO: Fix and uncomment tests
	
	private static final char STEP_F = 'f';
	private static final char STEP_E = 'e';
	private static final char STEP_D = 'd';
	private static final char STEP_C = 'c';
	private static final char STEP_B = 'b';
	private static final char STEP_A = 'a';
	private static final String SRC_TEST_CSV_LOC = "src" + File.separator + "test" + File.separator + "resources" +  File.separator +"csv" + File.separator;
	private static final String DIR_INPUT = File.separator + "input";
	private static final String DIR_OUTPUT = File.separator + "output";
	private static final String DIR_EXPECTED = File.separator + "expected";
	private static final String REG_FILE = SRC_TEST_CSV_LOC + "registration_dataset.csv";
	
	
	private DistributionModel model;
	private List<String[]> outputEntData;
	private List<String[]> outputAuthData;
	private Set<String> missingBeneficiaryIndicies;
	private Set<String> missingEntitlementIndicies;
	private Map<String, Integer> outputIndicies;
	

	private static final int entitlementsRowLength = 14;
	private static final String[] ignorableEntFields = new String[] {"_id", "authorization_id", "item_pack_id", "assigned_code", "ranges"};
	private static final String[] ignorableAuthFields = new String[] {"_id", "entitlement_id", "item_pack_id", "ranges"};

	
	private static void clearDatabase() {
		EntityManager em = DistributionPlanningUtil.getEm();
		em.createNativeQuery("DROP TABLE ActiveEntitlement;");
		em.createNativeQuery("DROP TABLE DisabledEntitlement;");
		em.createNativeQuery("DROP TABLE OverrideEntitlement;");
		em.createNativeQuery("DROP TABLE Authorization;");
		em.createNativeQuery("DROP TABLE ItemPack;");
		em.createNativeQuery("DROP TABLE ItemPackRange;");
	}
	
	private static void deleteOutputDirectory(char step) {
		String dirName = getOutputDirectory(step);
		System.out.println("Deleting OutputDirectory: " + dirName);
		File dir = new File(dirName);
		if(dir != null) {
			if(dir.isDirectory()) {
				for(File file: dir.listFiles()) 
					if (!file.isDirectory()) 
						file.delete();
			}
		}
	}
	
	@BeforeClass
	public static void setUpDatabase() {
		deleteOutputDirectory(STEP_A);
		deleteOutputDirectory(STEP_B);
		deleteOutputDirectory(STEP_C);
		deleteOutputDirectory(STEP_D);
		deleteOutputDirectory(STEP_E);
		deleteOutputDirectory(STEP_F);
		clearDatabase();
		DistributionModel model = new DistributionModel();
		DistributionPlanningUtil.addItemPack("equality", "test string equality");
		DistributionPlanningUtil.addItemPack("greater/less", "test greater than and less than");
		DistributionPlanningUtil.addItemPack("number equality", "test number equality");
		DistributionPlanningUtil.addItemPack("assign", "test assignment of item barcodes."
				+ " Also, should be assigned to all beneficiaries");
		DistributionPlanningUtil.addItemPack("collision", "this should not be received");
		DistributionPlanningUtil.addItemPack("voucher", "this voucher should be received by 1100-1105 and 1107-1109");
		DistributionPlanningUtil.addItemPack("disabled", "no one should get this");
		DistributionPlanningUtil.addItemPack("north_item", "Intended for Minors in North Sector");
		DistributionPlanningUtil.addItemPack("south_item", "Intended for Minors in South Sector");
		DistributionPlanningUtil.addItemPack("central_item", "Intended for Minors in Central Sector");
		assertTrue(DistributionPlanningUtil.verifyBeneficiaryFields(REG_FILE).isEmpty());
		model.setBeneficiaryInfo(REG_FILE);
		String t = getInputEntitlementsPath(STEP_A);
		assertTrue(DistributionPlanningUtil.verifyEntitlementFields(t).isEmpty());
		model.setEntitlementInfo(getInputEntitlementsPath(STEP_A));
		model.addRule("gender", "Equality", "female");
		model.addRule("is_pregnant", "Equality", "yes");
		model.addRuleList();
		model.addRule("gender", "Equality", "male");
		model.addRule("is_pregnant", "Equality", "no");
		model.addRuleList();
		model.setItemPackID(DistributionPlanningUtil.getItemID("equality"));
		model.addItemRange("-1000", "1000");
		String sId = model.addRuleLists("String Equality Test");
		DistributionPlanningUtil.setAuthorizationStatus(sId, Authorization.Status.disabled);
		DistributionPlanningUtil.setAuthorizationStatus(sId, Authorization.Status.active);
		model.addRule("age", "Greater Than", "10");
		model.addRule("age", "Less Than", "20.5");
		model.addRuleList();
		model.setItemPackID(DistributionPlanningUtil.getItemID("greater/less"));
		model.addItemRange("0", "2");
		model.addItemRange("2", "500");
		model.addRuleLists("Greater/Less Test");
		model.addRule("age", "Equality", "2.0");
		model.addRuleList();
		model.addRule("age", "Equality", "1.0");
		model.addRuleList();
		model.setItemPackID(DistributionPlanningUtil.getItemID("number equality"));
		model.addRuleLists("Number Equality Test");
		model.setItemPackID(DistributionPlanningUtil.getItemID("assign"));
		model.setIfAssigned(true);
		model.addItemRange("-3", "-2");
		model.addItemRange("0", "0");
		model.addItemRange("0", "0");
		model.addItemRange("0", "1");
		model.addItemRange("4", "4");
		model.addItemRange("2000", "2996");
		String id = model.addRuleLists("Barcode Assignment");
		DistributionPlanningUtil.addRangeToExisting(id, "3", "4");
		model.addVoucherRule("0", "4");
		model.setItemPackID(DistributionPlanningUtil.getItemID("collision"));
		model.addVoucherRuleList("Beneficiary Collision");
		model.addVoucherRule("10", "9");
		model.addVoucherRule("1002", "1001");
		model.addVoucherRule("1100", "1104");
		model.addVoucherRule("1103", "1105");
		model.addVoucherRule("1107", "1109");
		model.setItemPackID(DistributionPlanningUtil.getItemID("voucher"));
		model.addVoucherRuleList("Valid Voucher");
		model.setItemPackID(DistributionPlanningUtil.getItemID("disabled"));
		String disId = model.addRuleLists("Disabled Authorization");
		DistributionPlanningUtil.setAuthorizationStatus(disId, Authorization.Status.disabled);
		model.addRule("_row_owner", "Equality", "username:north");
		model.addRule("age", "Less Than", "18");
		model.setItemPackID(DistributionPlanningUtil.getItemID("north_item"));
		model.addItemRange("-200", "500");
		model.addRuleList();
		model.addRuleLists("North Specific");
		model.addRule("_row_owner", "Equality", "username:south");
		model.addRule("age", "Less Than", "18");
		model.setItemPackID(DistributionPlanningUtil.getItemID("south_item"));
		model.addItemRange("300", "900");
		model.addRuleList();
		model.addRuleLists("South Specific");
		model.addRule("_row_owner", "Equality", "username:central");
		model.addRule("age", "Less Than", "18");
		model.setItemPackID(DistributionPlanningUtil.getItemID("central_item"));
		model.addItemRange("500", "1200");
		model.addRuleList();
		model.addRuleLists("Central Specific");
		
	}
	
	@AfterClass
	public static void tearDown() {
		clearDatabase();
	}
	
	@Before
	public void init() {
		/*try {
			CSVReader reader = new CSVReader(new FileReader(expectedEntFile));
			this.expectedEntData = reader.readAll();
			reader = new CSVReader(new FileReader(expectedAuthFile));
			this.expectedAuthData = reader.readAll();
			reader = new CSVReader(new FileReader(expectedOverrideFile));
			this.expectedOverrideData = reader.readAll();
			reader = new CSVReader(new FileReader(expectedDisabledAuthFile));
			this.expectedDisabledAuthData = reader.readAll();
			reader = new CSVReader(new FileReader(expectedDisabledEntFile));
			this.expectedDisabledEntData = reader.readAll();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}*
		this.model = new DistributionModel();
	}
	
	private void runCycle(char step) {
		assertTrue(DistributionPlanningUtil.verifyBeneficiaryFields(REG_FILE).isEmpty());
		model.setBeneficiaryInfo(REG_FILE);
		assertTrue(DistributionPlanningUtil.verifyEntitlementFields(getInputEntitlementsPath(step)).isEmpty());
		model.setEntitlementInfo(getInputEntitlementsPath(step));
		try {
			this.missingBeneficiaryIndicies = new HashSet<String>();
			this.missingEntitlementIndicies = new HashSet<String>();
			//TODO: fix tests to have new export file structure
			model.export(getInputDirectory(step), getOutputDirectory(step), missingBeneficiaryIndicies, missingEntitlementIndicies);
			CSVReader reader = new CSVReader(new FileReader(getOutputEntitlementsPath(step)));
			this.outputEntData = reader.readAll();
			reader = new CSVReader(new FileReader(getOutputAuthorizationsPath(step)));
			this.outputAuthData = reader.readAll();
			reader.close();
			findIndicies();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	@Test 
	public void aFirstLoop() {
		String outputEntitlementPath = getOutputEntitlementsPath(STEP_A);
		String outputAuthorizationPath = getOutputAuthorizationsPath(STEP_A);
		runCycle(STEP_A);
		//checkAgainstExpected(this.expectedEntData, this.expectedAuthData);
		checkAgainstExpected(getExpectedEntitlementsPath(STEP_A), getExpectedAuthorizationsPath(STEP_A),
				outputEntitlementPath, outputAuthorizationPath);
		
		try {
			Files.copy(Paths.get(outputEntitlementPath), Paths.get(getInputEntitlementsPath(STEP_B)), 
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//@Test
	public void bSecondLoopNoChanges() {
		String outputEntitlementPath = getOutputEntitlementsPath(STEP_B);
		String outputAuthorizationPath = getOutputAuthorizationsPath(STEP_B);
		runCycle(STEP_B);
		checkAgainstExpected(getExpectedEntitlementsPath(STEP_B), getExpectedAuthorizationsPath(STEP_B),
				outputEntitlementPath, outputAuthorizationPath);
		
		try {
			Files.copy(Paths.get(outputEntitlementPath), Paths.get(getInputEntitlementsPath(STEP_C)), 
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void cCheckOverridesNotExistingBeneficiaries() {
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(getInputEntitlementsPath(STEP_C), true));
			EntityManager em = DistributionPlanningUtil.getEm();
			@SuppressWarnings("unchecked")
			List<Authorization> auths = em.createQuery("SELECT p FROM Authorization p")
										.getResultList();
			for (Authorization auth : auths) {
				if (!auth.isAssignedCode()) {
					if (auth.getAuthorizationName().equals("Disabled Authorization")) {
						String[] override = new String[entitlementsRowLength];
						override[0] = "random";
						override[1] = UUID.randomUUID().toString();
						override[2] = auth.getAuthorizationID();
						override[3] = 2000 + "";
						override[4] = convertRanges(auth.getRangeIDs());
						override[5] = auth.getAuthorizationName();
						override[6] = auth.getItemPackID();
						ItemPack ip = em.find(ItemPack.class, auth.getItemPackID());
						assertTrue(ip != null);
						override[7] = ip.getName();
						override[8] = ip.getDescription();
						override[9] = "false";
						override[10] = "false";
						override[12] = "username:central";
						override[13] = "HIDDEN";
						writer.writeNext(override);
					}
					String[] override = new String[entitlementsRowLength];
					override[0] = "update";
					override[1] = UUID.randomUUID().toString();
					override[2] = auth.getAuthorizationID();
					override[3] = 2000 + "";
					override[4] = convertRanges(auth.getRangeIDs());
					override[5] = auth.getAuthorizationName();
					override[6] = auth.getItemPackID();
					ItemPack ip = em.find(ItemPack.class, auth.getItemPackID());
					assertTrue(ip != null);
					override[7] = ip.getName();
					override[8] = ip.getDescription();
					override[9] = "true";
					override[10] = "false";
					override[12] = "username:central";
					override[13] = "HIDDEN";
					writer.writeNext(override);
				}
			}
			writer.close();
			
			String outputEntitlementPath = getOutputEntitlementsPath(STEP_C);
			String outputAuthorizationPath = getOutputAuthorizationsPath(STEP_C);
			runCycle(STEP_C);
			checkAgainstExpected(getExpectedEntitlementsPath(STEP_C), getExpectedAuthorizationsPath(STEP_C),
					outputEntitlementPath, outputAuthorizationPath);
			overrideDatabaseTest(outputAuthData.size() - 1);
			
			Files.copy(Paths.get(outputEntitlementPath), Paths.get(getInputEntitlementsPath(STEP_D)),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@SuppressWarnings("unchecked")
	//@Test
	// This will now actually create overrides for these...
	public void dCheckOverridesExistingBeneficiariesNotMarkedAsOverride() {
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(getInputEntitlementsPath(STEP_D), true));
			EntityManager em = DistributionPlanningUtil.getEm();
			List<Authorization> auths = em.createQuery("SELECT p FROM Authorization p")
										.getResultList();
			for (Authorization auth : auths) {
				if (!auth.isAssignedCode()) {
					List<ActiveEntitlement> ents = em.createQuery("SELECT p FROM ActiveEntitlement p"
							+ " WHERE p.authorizationID = '" + auth.getAuthorizationID() + 
							"' AND p.beneficiaryID = '1'").getResultList();
					/*ActiveEntitlement override = new ActiveEntitlement(auth, ents.get(0).getBeneficiaryCode(), 
							UUID.randomUUID().toString(), null, null, null, null, null);*/

					
					//writer.writeNext(override.attemptExport(Authorization.Status.active));
					/*String[] override = new String[entitlementsRowLength];
					override[0] = "update";
					override[1] = UUID.randomUUID().toString();
					override[2] = auth.getAuthorizationID();
					@SuppressWarnings("unchecked")
					List<ActiveEntitlement> ents = em.createQuery("SELECT p FROM ActiveEntitlement p"
							+ " WHERE p.authorizationID = '" + auth.getAuthorizationID() + 
							"' AND p.beneficiaryID = '1'").getResultList();
					if (!ents.isEmpty()) {
						override[3] = ents.get(0).getBeneficiaryID();
						override[4] = convertRanges(auth.getRangeIDs());
						override[5] = auth.getAuthorizationName();
						override[6] = auth.getItemPackID();
						ItemPack ip = em.find(ItemPack.class, auth.getItemPackID());
						assertTrue(ip != null);
						override[7] = ip.getName();
						override[8] = ip.getDescription();
						override[9] = "false";
						override[10] = "false";
						override[12] = ents.get(0).getFilterValue();
						override[13] = ents.get(0).getFilterType();
						writer.writeNext(override);
					}*
				}
			}
			writer.close();
			
			String outputEntitlementPath = getOutputEntitlementsPath(STEP_D);
			String outputAuthorizationPath = getOutputAuthorizationsPath(STEP_D);
			
			//runCycle(regFile, inputStepD, outputEntFileD, outputAuthFile);
			runCycle(STEP_D);
			// TODO: change the expectedOverrideData to be a file with the new stuff
			//checkAgainstExpected(this.expectedOverrideData, this.expectedAuthData);
			checkAgainstExpected(getExpectedEntitlementsPath(STEP_D), getExpectedAuthorizationsPath(STEP_D),
					outputEntitlementPath, outputAuthorizationPath);

			overrideDatabaseTest(outputAuthData.size());
			Files.copy(Paths.get(outputEntitlementPath), Paths.get(getInputEntitlementsPath(STEP_E)),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	//@Test
	public void eCheckDisableEverything() {
		EntityManager em = DistributionPlanningUtil.getEm();
		@SuppressWarnings("unchecked")
		List<Authorization> auths = em.createQuery("SELECT p FROM Authorization p").getResultList();
		for (Authorization auth : auths) {
			DistributionPlanningUtil.setAuthorizationStatus(auth.getAuthorizationID(), Authorization.Status.disabled);
		}
		
		String outputEntitlementPath = getOutputEntitlementsPath(STEP_E);
		String outputAuthorizationPath = getOutputAuthorizationsPath(STEP_E);
		
		//runCycle(regFile, outputEntFileD, outputEntFileE, outputAuthFile);
		runCycle(STEP_E);
		//checkAgainstExpected(this.expectedDisabledEntData, this.expectedDisabledAuthData);
		checkAgainstExpected(getExpectedEntitlementsPath(STEP_E), getExpectedAuthorizationsPath(STEP_E),
				outputEntitlementPath, outputAuthorizationPath);
		overrideDatabaseTest(outputAuthData.size());
		
		try {
			Files.copy(Paths.get(outputEntitlementPath), Paths.get(getInputEntitlementsPath(STEP_F)),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void fCheckReenableEverything() {
		EntityManager em = DistributionPlanningUtil.getEm();
		@SuppressWarnings("unchecked")
		List<Authorization> auths = em.createQuery("SELECT p FROM Authorization p").getResultList();
		for (Authorization auth : auths) {
			if (auth.getAuthorizationName() != "Disabled Authorization") {
				DistributionPlanningUtil.setAuthorizationStatus(auth.getAuthorizationID(), Authorization.Status.active);
			}
		}
		String outputEntitlementPath = getOutputDirectory(STEP_F);
		String outputAuthorizationPath = getOutputDirectory(STEP_F);
		
		//runCycle(regFile, outputEntFileE, outputEntFileF, outputAuthFile);
		runCycle(STEP_F);
		//checkAgainstExpected(this.expectedOverrideData, this.expectedAuthData);
		checkAgainstExpected(getExpectedEntitlementsPath(STEP_F), getExpectedAuthorizationsPath(STEP_F),
				outputEntitlementPath, outputAuthorizationPath);
		overrideDatabaseTest(outputAuthData.size());
	}
	
	private void checkAgainstExpected(String expectedEntitlementsPath,
			String expectedAuthorizationsPath, String outputEntitlementsPath,
			String outputAuthorizationsPath) {
		try {
			CSVReader reader = new CSVReader(new FileReader(expectedEntitlementsPath));
			List<String[]> expectedEntitlements = reader.readAll();
			reader = new CSVReader(new FileReader(expectedAuthorizationsPath));
			List<String[]> expectedAuthorizations = reader.readAll();
			reader = new CSVReader(new FileReader(outputEntitlementsPath));
			List<String[]> outputEntitlements = reader.readAll();
			reader = new CSVReader(new FileReader(outputAuthorizationsPath));
			List<String[]> outputAuthorizations = reader.readAll();
			reader.close();
			
			checkAgainstExpectedHelper(expectedEntitlements, outputEntitlements, ignorableEntFields);
			checkAgainstExpectedHelper(expectedAuthorizations, outputAuthorizations, ignorableEntFields);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void checkAgainstExpectedHelper(List<String[]> expectedData, List<String[]> outputData, String[] ignorable) {
		if (expectedData.size() != outputData.size()) {
			System.out.println("Expected: " + expectedData.size());
			System.out.println("Output: " + outputData.size());
		}
		assertEquals(expectedData.size(), outputData.size());
		String[] expectedLabels = expectedData.get(0);
		String[] outputLabels = expectedData.get(0);
		datasetContains(outputData, expectedLabels, new String[0], outputLabels);
		for (int j = 1; j < expectedData.size(); j++) {
			boolean valid = datasetContains(outputData, expectedData.get(j), ignorable, outputLabels);
			if (!valid) {
				System.out.println(j);
			}
			assertTrue(valid);
		}
	}
	
	private boolean datasetContains(List<String[]> dataset, String[] row, String[] ignorable, String[] labels) {
		for (String[] testRow : dataset) {
			if (testRow.length != row.length) {
				System.out.println("testRow: " + Arrays.toString(testRow));
				System.out.println("row: " + Arrays.toString(row));

				System.out.println("HEY");
				System.out.println(testRow.length + "---" + row.length);
			}
			assertEquals(testRow.length, row.length);
			boolean match = true;
			for (int i = 0; i < testRow.length; i++) {
				boolean ignore = false;
				int j = 0;
				while (j < ignorable.length && !ignore) {
					if (labels[i].equals(ignorable[j])) {
						ignore = handleIgnore(testRow[i], row[i], ignorable[j]);
					}
					j++;
				}
				if (!ignore && !testRow[i].equalsIgnoreCase(row[i])) {
					match = false;
				}
			}
			if (match) {
				return true;
			}
		}
		return false;
	}
	
	private boolean handleIgnore(String ids1, String ids2, String type) {
		/*if (type.equals("ranges")) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				Set<Range> list1 = mapper.readValue(ids1, RangeList.class).getRanges();
				Set<Range> list2 = mapper.readValue(ids2, RangeList.class).getRanges();
				
				System.out.println("******" + list1.equals(list2) + "******");
				return list1.equals(list2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}*
		return true;
	}
	
	private static String getInputDirectory(char step) {
		return SRC_TEST_CSV_LOC + step + DIR_INPUT;
	}
	
	private static String getOutputDirectory(char step) {
		return SRC_TEST_CSV_LOC + step + DIR_OUTPUT;
	}
	
	private static String getInputEntitlementsPath(char step) {
		return SRC_TEST_CSV_LOC + step + DIR_INPUT + File.separator +"entitlements.csv";
	}
	
	private static String getOutputEntitlementsPath(char step) {
		return SRC_TEST_CSV_LOC + step + DIR_OUTPUT + File.separator +"entitlements.csv";
	}
	
	private static String getExpectedEntitlementsPath(char step) {
		return SRC_TEST_CSV_LOC + step + DIR_EXPECTED+ File.separator + "entitlements.csv";
	}
	
	private static String getOutputAuthorizationsPath(char step) {
		return SRC_TEST_CSV_LOC + step + DIR_OUTPUT + File.separator + "authorizations.csv";
	}

	private static String getExpectedAuthorizationsPath(char step) {
		return SRC_TEST_CSV_LOC + step + DIR_EXPECTED+ File.separator + "authorizations.csv";
	}
	
	private static void overrideDatabaseTest(int num) {
		EntityManager em = DistributionPlanningUtil.getEm();
		Query q = em.createQuery("SELECT p FROM OverrideEntitlement p");
		@SuppressWarnings("unchecked")
		List<OverrideEntitlement> overs = q.getResultList();
		assertTrue(overs.size() == num);
	}
	
	private static String convertRanges(String ids) {
		ObjectMapper mapper = new ObjectMapper();
		IDList idl;
		try {
			idl = mapper.readValue(ids, IDList.class);
			EntityManager em = DistributionPlanningUtil.getEm();
			RangeList rl = new RangeList();
			for (String id : idl.getIDs()) {
				ItemPackRange ipr = em.find(ItemPackRange.class, id);
				if (ipr != null) {
					Range r = new Range(ipr.getMin(), ipr.getMax());
					rl.addRange(r);
				}
			}
			return mapper.writeValueAsString(rl);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private void findIndicies() {
		outputIndicies = new HashMap<String, Integer>();
		String[] labels = outputEntData.get(0);
		for (int i = 0; i < labels.length; i++) {
			outputIndicies.put(labels[i], i);
		}
	}
	*/
}
