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

package edu.uw.cse.ifrcdemo.distplan.sync;

import edu.uw.cse.ifrcdemo.distplan.data.DataRepos;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.FileConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.OdkPathUtil;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CopyForms {

    private final DataRepos dataRepos;

    public CopyForms(DataRepos repos) {
        this.dataRepos = repos;
    }

    public void copyForms(Path outputPath) throws IOException {
        List<XlsxForm> distributionForms = dataRepos.getDistributionRepository().getAllForms();
        List<XlsxForm> authorizationForms = dataRepos.getAuthorizationRepository().getAllForms();
        List<XlsxForm> visitProgForms = dataRepos.getVisitProgramRepository().getAllForms();

        Set<XlsxForm> allForms = new HashSet<>();
        allForms.addAll(distributionForms);
        allForms.addAll(authorizationForms);
        allForms.addAll(visitProgForms);

        for (XlsxForm form : allForms) {
            copyForm(form, outputPath);
        }
    }

    public void copyForm(XlsxForm form, Path outputPath) throws IOException {
        Path xlsxFormStoragePath = InternalFileStoreUtil.getXlsxFormStoragePath();
        Path srcTablePath = OdkPathUtil.getTablePath(xlsxFormStoragePath, form.getTableId());
        Path srcFormPath = OdkPathUtil.getFormPath(srcTablePath, form.getFormId());

        Path targetTablePath = OdkPathUtil.getTablePath(outputPath, form.getTableId());
        Path targetFormPath = OdkPathUtil.getFormPath(targetTablePath, form.getFormId());

        if (Files.notExists(targetTablePath)) {
            // Table level files only need to be copied once per table

            Files.createDirectories(targetTablePath);

            for (String file : FileConsts.TABLE_LEVEL_FILES) {
                Files.copy(srcTablePath.resolve(file), targetTablePath.resolve(file));
            }
        }

        Files.createDirectories(targetFormPath);
        FileUtils.copyDirectory(srcFormPath.toFile(), targetFormPath.toFile());
    }
}
