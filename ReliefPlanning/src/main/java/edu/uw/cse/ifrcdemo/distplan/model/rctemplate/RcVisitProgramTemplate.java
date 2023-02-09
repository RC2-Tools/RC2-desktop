/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.model.rctemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

public class RcVisitProgramTemplate {

    public static final String FILENAME = "VisitTemplate.json";

    private static final String VISIT_ROW_ID = "visitProgramRowId";
    private static final String VISIT_DATE_CREATED = "visitProgramDateCreated";
    private static final String VISIT_CUSTOM_FORM = "visitProgramCustomForm";
    private static final String VISIT_IS_FOR_MEMBER = "visitProgramIsForMember";
    private static final String VISIT_RULES = "visitProgramRules";
    private static final String VISIT_BENEFICIARY_RANGES = "visitProgramBeneficiaryRanges";
    private static final String VISIT_TEMPLATE_NAME = "visitTemplateName";
    private static final String VISIT_TEMPLATE_VISIT_PROGRAM = "visitTemplateVisitProgram";

    private String templateName;
    private VisitProgram visitProgram;

    public RcVisitProgramTemplate(File file) throws IOException, JSONException {
        this(new String(Files.readAllBytes(Paths.get(file.getPath()))));
     }

    public RcVisitProgramTemplate(String jsonStr) throws IOException, JSONException {
        JSONObject json = new JSONObject(jsonStr);

        this.templateName = json.getString(VISIT_TEMPLATE_NAME);
        this.visitProgram = createVisitProgramFromJSON(json.getJSONObject(VISIT_TEMPLATE_VISIT_PROGRAM));
    }

    public RcVisitProgramTemplate(String name, VisitProgram importVisitProgram) {
        this.templateName = name;
        this.visitProgram = new VisitProgram();

        visitProgram.setCustomVisitForm(importVisitProgram.getCustomVisitForm());
        visitProgram.setDateCreated(Instant.now());
        visitProgram.setForMember(importVisitProgram.isForMember());
        visitProgram.setRules(importVisitProgram.getRules());
        visitProgram.setBeneficiaryRanges(importVisitProgram.getBeneficiaryRanges());
    }

    public static RcVisitProgramTemplate ConvertToRcVisitProgramTemplate(RcTemplate template) throws IOException, JSONException {
        if(template.getTemplateType() != RcTemplateType.VISIT_PROGRAM) {
            throw new IllegalArgumentException();
        }
        return new RcVisitProgramTemplate(template.getJsonEncodingString());
    }

    public RcTemplate getTemplate() throws IOException, JSONException {
        RcTemplate template = new RcTemplate();
        template.setTemplateType(RcTemplateType.VISIT_PROGRAM);
        template.setJsonEncodingString(getJsonRcTemplateString());
        template.setName(templateName);
        return template;
    }

    public VisitProgram getVisitProgram() {
        return visitProgram;
    }

    public String getTemplateName() { return templateName; }

    private String getJsonRcTemplateString() throws JSONException, IOException {
        JSONObject visitTemplate = new JSONObject();
        visitTemplate.put(VISIT_TEMPLATE_NAME, templateName);
        visitTemplate.put(VISIT_TEMPLATE_VISIT_PROGRAM, createJSONFromVisitProgram(visitProgram));
        return visitTemplate.toString();
    }

    public void writeEncodingDirectory(File file) throws IOException, JSONException  {
        file.mkdir();

        RcTemplateUtil.outputJsonFile(new File(file.getAbsolutePath(), FILENAME), getJsonRcTemplateString());
        // copy visit form
        RcTemplateUtil.copyFormForTemplateIfNotNull(file, visitProgram.getCustomVisitForm());

    }

    final static JSONObject createJSONFromVisitProgram(VisitProgram visitProgram) throws JSONException, JsonProcessingException {
        JSONObject json = new JSONObject();
        json.put(VISIT_ROW_ID, visitProgram.getRowId());
        json.put(VISIT_CUSTOM_FORM, new ObjectMapper().writeValueAsString(visitProgram.getCustomVisitForm()));

        Instant dateCreated = visitProgram.getDateCreated();
        if (dateCreated != null) {
            json.put(VISIT_DATE_CREATED, Long.valueOf(dateCreated.getEpochSecond()));
        }

        json.put(VISIT_IS_FOR_MEMBER, visitProgram.isForMember());

        ObjectMapper objectMapper = new ObjectMapper();

        String rulesStr = objectMapper.writerFor(new TypeReference<List<List<AuthorizationCriterion>>>() {
        }).writeValueAsString(visitProgram.getRules());
        json.put(VISIT_RULES, rulesStr);

        List<Range> beneficiaryRanges = visitProgram.getBeneficiaryRanges();
        String beneficiaryRangeStr = objectMapper
            .writerFor(new TypeReference<List<Range>>() {})
            .writeValueAsString(beneficiaryRanges);
        json.put(VISIT_BENEFICIARY_RANGES, beneficiaryRangeStr);

        return json;
    }

    final static VisitProgram createVisitProgramFromJSON(JSONObject json) throws IOException {
        VisitProgram visitProgram = new VisitProgram();

        if (json == null) {
            return visitProgram;
        }

        // if not null parse


        try {
            visitProgram.setRowId(json.getString(VISIT_ROW_ID));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            visitProgram.setCustomVisitForm(new ObjectMapper().readValue(json.getString(VISIT_CUSTOM_FORM), XlsxForm.class));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            visitProgram.setDateCreated(Instant.ofEpochSecond(json.getLong(VISIT_DATE_CREATED)));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            visitProgram.setForMember(json.getBoolean(VISIT_IS_FOR_MEMBER));
        } catch (JSONException e) {
            // do nothing as leave value null
        }
        try {
            String rulesStr = json.getString(VISIT_RULES);
            List<List<AuthorizationCriterion>> rules = new ObjectMapper().readerFor(new TypeReference<List<List<AuthorizationCriterion>>>() {
            }).readValue(rulesStr);
            visitProgram.setRules(rules);
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        try {
            String beneficiaryRangesStr = json.getString(VISIT_BENEFICIARY_RANGES);
            List<Range> ranges = new ObjectMapper()
                .readerFor(new TypeReference<List<Range>>() {})
                .readValue(beneficiaryRangesStr);

            visitProgram.setBeneficiaryRanges(ranges);
        } catch (JSONException e) {
            // do nothing as leave value null
        }

        return visitProgram;
    }

    @Override
    public RcVisitProgramTemplate clone(){
        try{
            String jsonStr = this.getJsonRcTemplateString();
            RcVisitProgramTemplate template = new RcVisitProgramTemplate(jsonStr);
            return template;
        } catch (Exception e) {
            throw new AssertionError("Problem with Visit Program template Clone");
        }
    }
}
