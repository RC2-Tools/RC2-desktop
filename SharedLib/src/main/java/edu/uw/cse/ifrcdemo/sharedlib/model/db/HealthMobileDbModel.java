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

package edu.uw.cse.ifrcdemo.sharedlib.model.db;

import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvHealthService;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvHealthTask;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvHealthServicesForTask;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.BeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Program;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Service;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.ServicesForProgram;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HealthMobileDbModel extends MobileDbModel {
    // This is the definition of the mobile db.
    public static final List<Class<? extends BaseSyncRow>> tableDefinition;

    static {
        List<Class<? extends BaseSyncRow>> list = new LinkedList<>();
        list.add(CsvBeneficiaryEntity.class);
        list.add(CsvIndividual.class);
        list.add(CsvHealthTask.class);
        list.add(CsvHealthService.class);
        list.add(CsvHealthServicesForTask.class);
        tableDefinition = Collections.unmodifiableList(list);
    }

    private static final Map<Class, Class> csvToStubMap;
    static {
        Map<Class, Class> map = new LinkedHashMap<>();
        map.put(CsvBeneficiaryEntity.class, BeneficiaryEntity.class);
        map.put(CsvIndividual.class, Individual.class);
        map.put(CsvHealthTask.class, Program.class);
        map.put(CsvHealthService.class, Service.class);
        map.put(CsvHealthServicesForTask.class, ServicesForProgram.class);
        csvToStubMap = Collections.unmodifiableMap(map);
    }

    private static final Map<Class, String> csvToName;
    static {
        Map<Class, String> map = new LinkedHashMap<>();
        map.put(CsvBeneficiaryEntity.class, MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME);
        map.put(CsvIndividual.class, MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME);
        map.put(CsvHealthTask.class, MobileDbConsts.HealthTableNames.PROGRAMS_TABLE_NAME);
        map.put(CsvHealthService.class, MobileDbConsts.HealthTableNames.SERVICES_TABLE_NAME);
        map.put(CsvHealthServicesForTask.class, MobileDbConsts.HealthTableNames.SERVICES_FOR_PROGRAM_TABLE_NAME);
        csvToName = Collections.unmodifiableMap(map);
    }

    public HealthMobileDbModel(Path basePath, List<Class<? extends BaseSyncRow>> requiredTables) throws IOException, IllegalArgumentException {
        super(tableDefinition, csvToStubMap, csvToName, basePath, requiredTables);
    }
}
