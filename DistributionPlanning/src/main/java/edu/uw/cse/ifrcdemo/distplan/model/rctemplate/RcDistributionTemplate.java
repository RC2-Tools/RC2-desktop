/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.model.rctemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.authorization.Range;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RcDistributionTemplate implements Cloneable{

    public static final String FILENAME = "DistTemplate.json";

    private static final String ITEM_ROW_ID = "itemRowId";
    private static final String ITEM_NAME = "itemName";
    private static final String ITEM_DESCRIPTION = "itemDescription";

    private static final String AUTH_ROW_ID = "authRowId";
    private static final String AUTH_DATE_CREATED = "authDateCreated";
    private static final String AUTH_TYPE = "authType";
    private static final String AUTH_ASSIGN_ITEM_PACK_CODE = "authAssignItemPackCode";
    private static final String AUTH_FOR_INDIVIDUAL = "authForIndividual";
    private static final String AUTH_ITEM_PACK = "authItemPack";
    private static final String AUTH_EXTRA_FIELD_ENTITLEMENTS = "authExtraFieldEntitlements";
    private static final String AUTH_ITEM_PACK_RANGES = "authItemPackRanges";
    private static final String AUTH_VOUCHER_RANGES = "authVoucherRanges";
    private static final String AUTH_BENEFICIARY_RANGES = "authBeneficiaryRanges";
    private static final String AUTH_CRITERIA_RULES = "authCriteriaRules";
    private static final String AUTH_CUSTOM_DELIVERY_FORM = "authCustomDeliveryForm";

    private static final String DISTRIBUTION_TEMPLATE_NAME = "distributionTemplateName";
    private static final String DISTRIBTION_KEY = "distribution";
    private static final String DISTRIBUTION_ROW_ID = "distributionRowId";
    private static final String DISTRIBUTION_DATE_CREATED = "distributionDateCreated";
    private static final String DISTRIBUTION_SUMMARY_FORM = "distributionSummaryForm";
    private static final String DISTRIBUTION_AUTHS = "distributionAuths";

    private Distribution distribution;

    private String templateName;

    public RcDistributionTemplate(File file) throws IOException, JSONException {
        this(new String(Files.readAllBytes(Paths.get(file.getPath()))));
    }

    public RcDistributionTemplate(String jsonStr) throws IOException, JSONException {
        JSONObject json = new JSONObject(jsonStr);

        this.templateName = json.getString(DISTRIBUTION_TEMPLATE_NAME);
        this.distribution = createDistributionFromJSON(json.getJSONObject(DISTRIBTION_KEY));

    }

    public RcDistributionTemplate(String name, Distribution importDistribution) {
        this.templateName = name;
        this.distribution = new Distribution();

        distribution.setDateCreated(Instant.now());
        distribution.setDescription(importDistribution.getDescription());

        // add authorizations
        List<Authorization> authorizations = new ArrayList<>();
        for (Authorization importAuth : importDistribution.getAuthorizations()) {
            Authorization auth = processAuthorization(importAuth);
            authorizations.add(auth);
        }
        distribution.setAuthorizations(authorizations);

        // TODO: make sure form is included in zip
        distribution.setSummaryForm(importDistribution.getSummaryForm());
    }

    public static RcDistributionTemplate ConvertToRcDistributionTemplate(RcTemplate template) throws IOException, JSONException {
        if(template.getTemplateType() != RcTemplateType.DISTRIBUTION) {
            throw new IllegalArgumentException();
        }
        return new RcDistributionTemplate(template.getJsonEncodingString());
    }

    public RcTemplate getTemplate() throws IOException, JSONException {
        RcTemplate template = new RcTemplate();
        template.setTemplateType(RcTemplateType.DISTRIBUTION);
        template.setJsonEncodingString(getJsonRcTemplateString());
        template.setName(templateName);
        return template;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public String getTemplateName() {
        return templateName;
    }

    private Authorization processAuthorization(Authorization importAuthorization) {
        Authorization auth = new Authorization();
        auth.setDateCreated(Instant.now());
        auth.setType(importAuthorization.getType());
        auth.setAssignItemPackCode(importAuthorization.isAssignItemPackCode());
        auth.setForIndividual(importAuthorization.getForIndividual());
        auth.setItemPack(processItem(importAuthorization.getItemPack()));
        auth.setItemPackRanges(importAuthorization.getItemPackRanges());
        auth.setVoucherRanges(importAuthorization.getVoucherRanges());
        auth.setExtraFieldEntitlements(importAuthorization.getExtraFieldEntitlements());
        auth.setBeneficiaryRanges(importAuthorization.getBeneficiaryRanges());
        auth.setCustomDeliveryForm(importAuthorization.getCustomDeliveryForm());
        auth.setRules(importAuthorization.getRules());

        return auth;
    }

    public void writeEncodingDirectory(File file) throws IOException, JSONException {
        file.mkdir();

        RcTemplateUtil.outputJsonFile(new File(file.getAbsolutePath(), FILENAME), getJsonRcTemplateString());

        // copy field summary form
        RcTemplateUtil.copyFormForTemplateIfNotNull(file, distribution.getSummaryForm());

        // copy custom delivery form & form used for criteria
        for(Authorization auth : distribution.getAuthorizations()) {
            RcTemplateUtil.copyFormForTemplateIfNotNull(file, auth.getCustomDeliveryForm());
            for(List<AuthorizationCriterion> criteriaQualification : auth.getRules()) {
                for(AuthorizationCriterion criteria : criteriaQualification) {
                    CriterionField criterionField = criteria.getField();
                    RcTemplateUtil.copyFormForTemplateIfNotNull(file, criterionField.getTableId());
                }
            }
        }
    }




    public static RcDistributionTemplate readEncodedDirectory(File file) throws IOException, JSONException {
        File jsonFile = new File(file.getAbsolutePath(), FILENAME);
        if(!jsonFile.exists()) {
            throw new FileNotFoundException("Missing Distribution Template JSON file");
        }

        RcDistributionTemplate template = new RcDistributionTemplate(jsonFile);

        // TODO: need to load forms
        return template;
    }

    private String getJsonRcTemplateString() throws JSONException, IOException {
        JSONObject distributionTemplate = new JSONObject();
        distributionTemplate.put(DISTRIBUTION_TEMPLATE_NAME, templateName);
        distributionTemplate.put(DISTRIBTION_KEY, createJSONfromDistribution(distribution));
        return distributionTemplate.toString();
    }


    private final static ItemPack processItem(ItemPack importItem) {
        ItemPack item = new ItemPack();
        item.setRowId(importItem.getRowId());
        item.setName(importItem.getName());
        item.setDescription(importItem.getDescription());
        return item;
    }


    final static ItemPack createItemFromJSON(JSONObject json) {
        ItemPack item = new ItemPack();
        if (json == null)
            return item;

        // if not null parse
        try {
            item.setRowId(json.getString(ITEM_ROW_ID));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            item.setName(json.getString(ITEM_NAME));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            item.setDescription(json.getString(ITEM_DESCRIPTION));
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        return item;
    }

    final static JSONObject createJSONFromItem(ItemPack item) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ITEM_ROW_ID, item.getRowId());
        json.put(ITEM_NAME, item.getName());
        json.put(ITEM_DESCRIPTION, item.getDescription());
        return json;
    }

    final static JSONObject createJSONfromAuthorization(Authorization auth) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        json.put(AUTH_ROW_ID, auth.getRowId());

        Instant dateCreated = auth.getDateCreated();
        if (dateCreated != null) {
            json.put(AUTH_DATE_CREATED, Long.valueOf(dateCreated.getEpochSecond()));
        }

        AuthorizationType type = auth.getType();
        if (type != null) {
            json.put(AUTH_TYPE, type.name());
        }
        json.put(AUTH_ASSIGN_ITEM_PACK_CODE, auth.isAssignItemPackCode());
        json.put(AUTH_FOR_INDIVIDUAL, auth.getForIndividual());

        ItemPack item = auth.getItemPack();
        if (item != null) {
            json.put(AUTH_ITEM_PACK, createJSONFromItem(item));
        }

        ExtraFieldEntitlements extraFieldEntitlements = auth.getExtraFieldEntitlements();
        if (extraFieldEntitlements != null) {
            json.put(AUTH_EXTRA_FIELD_ENTITLEMENTS, extraFieldEntitlements.name());
        }

        String itemRanges = new ObjectMapper().writerFor(new TypeReference<List<Range>>() {
        }).writeValueAsString(auth.getItemPackRanges());
        json.put(AUTH_ITEM_PACK_RANGES, itemRanges);

        String voucherRanges = new ObjectMapper().writerFor(new TypeReference<List<Range>>() {
        }).writeValueAsString(auth.getVoucherRanges());
        json.put(AUTH_VOUCHER_RANGES, voucherRanges);

        String beneficiaryRanges = new ObjectMapper().writerFor(new TypeReference<List<Range>>() {
        }).writeValueAsString(auth.getBeneficiaryRanges());
        json.put(AUTH_BENEFICIARY_RANGES, beneficiaryRanges);

        String rulesStr = new ObjectMapper().writerFor(new TypeReference<List<List<AuthorizationCriterion>>>() {
        }).writeValueAsString(auth.getRules());
        json.put(AUTH_CRITERIA_RULES, rulesStr);

        json.put(AUTH_CUSTOM_DELIVERY_FORM, new ObjectMapper().writeValueAsString(auth.getCustomDeliveryForm()));

        return json;
    }

    final static Authorization createAuthorizationFromJSON(JSONObject json) throws IOException {
        Authorization auth = new Authorization();

        if (json == null)
            return auth;

        // if not null parse

        try {
            auth.setRowId(json.getString(AUTH_ROW_ID));
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        try {
            auth.setDateCreated(Instant.ofEpochSecond(json.getLong(AUTH_DATE_CREATED)));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            String authTypeStr = json.getString(AUTH_TYPE);
            if(authTypeStr != null) {
                auth.setType(AuthorizationType.valueOf(authTypeStr));
            }
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            auth.setAssignItemPackCode(json.getBoolean(AUTH_ASSIGN_ITEM_PACK_CODE));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            auth.setForIndividual(json.getBoolean(AUTH_FOR_INDIVIDUAL));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            auth.setItemPack(createItemFromJSON(json.getJSONObject(AUTH_ITEM_PACK)));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            String itemRangeStr = json.getString(AUTH_ITEM_PACK_RANGES);
            List<Range> itemRanges = new ObjectMapper().readerFor(new TypeReference<List<Range>>() {
            }).readValue(itemRangeStr);
            auth.setItemPackRanges(itemRanges);
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            String voucherRangeStr = json.getString(AUTH_VOUCHER_RANGES);
            List<Range> voucherRanges = new ObjectMapper().readerFor(new TypeReference<List<Range>>() {
            }).readValue(voucherRangeStr);
            auth.setVoucherRanges(voucherRanges);
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        try {
            String authExtraFieldEntitlement = json.getString(AUTH_EXTRA_FIELD_ENTITLEMENTS);
            if(authExtraFieldEntitlement != null) {
                auth.setExtraFieldEntitlements(ExtraFieldEntitlements.valueOf(authExtraFieldEntitlement));
            }
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        try {
            String beneficiaryRangeStr = json.getString(AUTH_BENEFICIARY_RANGES);
            List<Range> beneficiaryRanges = new ObjectMapper().readerFor(new TypeReference<List<Range>>() {
            }).readValue(beneficiaryRangeStr);
            auth.setBeneficiaryRanges(beneficiaryRanges);
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            String rulesStr = json.getString(AUTH_CRITERIA_RULES);
            List<List<AuthorizationCriterion>> rules = new ObjectMapper().readerFor(new TypeReference<List<List<AuthorizationCriterion>>>() {
            }).readValue(rulesStr);
            auth.setRules(rules);
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        try {
            auth.setCustomDeliveryForm(new ObjectMapper().readValue(json.getString(AUTH_CUSTOM_DELIVERY_FORM), XlsxForm.class));
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        return auth;
    }

    final static JSONObject createJSONfromDistribution(Distribution distribution) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        json.put(DISTRIBUTION_ROW_ID, distribution.getRowId());
        List<Authorization> auths = distribution.getAuthorizations();
        if (auths != null && !auths.isEmpty()) {
            JSONArray authArray = new JSONArray();
            for (Authorization auth : auths) {
                JSONObject authJSON = createJSONfromAuthorization(auth);
                authArray.add(authJSON);
            }
            json.put(DISTRIBUTION_AUTHS, authArray);
        }

        Instant dateCreated = distribution.getDateCreated();
        if (dateCreated != null) {
            json.put(DISTRIBUTION_DATE_CREATED, Long.valueOf(dateCreated.getEpochSecond()));
        }

        json.put(DISTRIBUTION_SUMMARY_FORM, new ObjectMapper().writeValueAsString(distribution.getSummaryForm()));
        return json;
    }

    final static Distribution createDistributionFromJSON(JSONObject json) throws IOException {
        Distribution distribution = new Distribution();

        if (json == null) {
            return distribution;
        }

        // if not null parse

        try {
            distribution.setRowId(json.getString(DISTRIBUTION_ROW_ID));
        } catch (JSONException e) {
            // do nothing as leave value null
        }


        try {
            JSONArray authArray = json.getJSONArray(DISTRIBUTION_AUTHS);
            if (authArray != null && !authArray.isEmpty()) {
                List<Authorization> authList = new ArrayList<>();
                Iterator<JSONObject> authItr = authArray.iterator();
                while (authItr.hasNext()) {
                    JSONObject authJson = authItr.next();
                    authList.add(createAuthorizationFromJSON(authJson));
                }
                distribution.setAuthorizations(authList);
            }
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        try {
            distribution.setDateCreated(Instant.ofEpochSecond(json.getLong(DISTRIBUTION_DATE_CREATED)));
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        try {
            distribution.setSummaryForm(new ObjectMapper().readValue(json.getString(DISTRIBUTION_SUMMARY_FORM), XlsxForm.class));
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        return distribution;
    }

    @Override
    public RcDistributionTemplate clone(){
        try{
            String jsonStr = this.getJsonRcTemplateString();
            RcDistributionTemplate template = new RcDistributionTemplate(jsonStr);
            return template;
        } catch (Exception e) {
            throw new AssertionError("Problem with Distribution Clone");
        }
    }
}
