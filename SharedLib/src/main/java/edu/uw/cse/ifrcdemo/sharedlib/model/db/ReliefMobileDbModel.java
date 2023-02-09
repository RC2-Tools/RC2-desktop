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
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorizationReport;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDelivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Authorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.AuthorizationReport;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.BeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Delivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Entitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReliefMobileDbModel extends MobileDbModel {

    // This is the definition of the mobile db. There are 6 tables, each with its own row class that extends
    // BaseSyncRow. This list is used to construct the data table map.
    public static final List<Class<? extends BaseSyncRow>> tableDefinition;

    static {
        List<Class<? extends BaseSyncRow>> list = new LinkedList<>();
        list.add(CsvAuthorization.class);
        list.add(CsvEntitlement.class);
        list.add(CsvBeneficiaryEntity.class);
        list.add(CsvIndividual.class);
        list.add(CsvDelivery.class);
        list.add(CsvAuthorizationReport.class);

        tableDefinition = Collections.unmodifiableList(list);
    }

    private static final Map<Class, Class> csvToStubMap;
    static {
        Map<Class, Class> map = new LinkedHashMap<>();
        map.put(CsvAuthorization.class, Authorization.class);
        map.put(CsvEntitlement.class, Entitlement.class);
        map.put(CsvBeneficiaryEntity.class, BeneficiaryEntity.class);
        map.put(CsvIndividual.class, Individual.class);
        map.put(CsvDelivery.class, Delivery.class);
        map.put(CsvAuthorizationReport.class, AuthorizationReport.class);

        csvToStubMap = Collections.unmodifiableMap(map);
    }

    private static final Map<Class, String> csvToName;
    static {
        Map<Class, String> map = new LinkedHashMap<>();
        map.put(CsvAuthorization.class, MobileDbConsts.TableNames.AUTHORIZATION_TABLE_NAME);
        map.put(CsvEntitlement.class, MobileDbConsts.TableNames.ENTITLEMENT_TABLE_NAME);
        map.put(CsvBeneficiaryEntity.class, MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME);
        map.put(CsvIndividual.class, MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME);
        map.put(CsvDelivery.class, MobileDbConsts.TableNames.DELIVERY_TABLE_NAME);
        map.put(CsvAuthorizationReport.class, MobileDbConsts.TableNames.AUTHORIZATION_REPORT_TABLE_NAME);

        csvToName = Collections.unmodifiableMap(map);
    }

    public ReliefMobileDbModel(Path basePath, List<Class<? extends BaseSyncRow>> requiredTables) throws IOException, IllegalArgumentException {
        super(tableDefinition, csvToStubMap, csvToName, basePath, requiredTables);
    }
}
