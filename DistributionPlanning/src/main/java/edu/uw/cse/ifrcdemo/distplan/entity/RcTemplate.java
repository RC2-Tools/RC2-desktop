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

package edu.uw.cse.ifrcdemo.distplan.entity;

import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcVisitProgramTemplate;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import org.apache.wink.json4j.JSONException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.IOException;
import java.nio.file.Path;

@Entity
public class RcTemplate extends ODKEntity {

    @Basic(optional = false)
    @Column(nullable = false)
    private RcTemplateType templateType;

    @Basic(optional = false)
    @Column(nullable = false)
    private String jsonEncodingString;

    @Basic(optional = false)
    @Column(nullable = false)
    private String name;

    public RcTemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(RcTemplateType templateType) {
        this.templateType = templateType;
    }

    public String getJsonEncodingString() {
        return jsonEncodingString;
    }

    public void setJsonEncodingString(String jsonEncodingString) {
        this.jsonEncodingString = jsonEncodingString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void writeEncodinDirectory(Path tmpDirectory) throws IOException, JSONException {
        if (getTemplateType() == RcTemplateType.DISTRIBUTION) {
            RcDistributionTemplate distributionTemplate = new RcDistributionTemplate(getJsonEncodingString());
            distributionTemplate.writeEncodingDirectory(tmpDirectory.toFile());
        } else if (getTemplateType() == RcTemplateType.VISIT_PROGRAM) {
            RcVisitProgramTemplate visitProgramTemplate = new RcVisitProgramTemplate(getJsonEncodingString());
            visitProgramTemplate.writeEncodingDirectory(tmpDirectory.toFile());
        } else {
            throw new IllegalArgumentException("Not a valid template type");
        }

    }
}
