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

package edu.uw.cse.ifrcdemo.convertcsv;

import java.io.IOException;
import java.util.stream.IntStream;

public class PeruConvertCsv extends ConvertCsv {

    private static final String IMPORT_CSV = "C:/perudata/Registro__final_20200427.csv";
    private static final String CSV_WRITE_PATH = "peru_csv";

    private static final String[] INTAKE_COL = new String[]{"consentimiento", "Comentario", "nombre", "apellido", "edad", "genero", "nacionalidad", "nacionalidad_otra", "tel", "residencia", "nucleo_familiar", "_0-5", "_6a11", "_12-17", "_18-23", "_24-65", "mas65", "discapacidad", "discapacidad_si", "problema_salud", "problema_salud_si", "medicamento", "medicamento_accesible", "necesidad", "mejoraria_otro", "situacion_laboral", "trabajo", "fuera_casa", "bioseguridad", "diagnostico_positivo", "medida", "otra_necesidad"};
    private static final String[] OUTPUT_COL = new String[]{"Comentario","first_name", "last_name", "bioseguridad", "consentimiento", "diagnostico_positivo", "discapacidad", "discapacidad_si", "edad", "fuera_casa", "gender", "medicamento", "medicamento_accesible", "medida", "mejoraria_otro", "nacionalidad", "nacionalidad_otra", "necesidad", "over65", "nucleo_familiar", "otra_necesidad", "problema_salud", "problema_salud_si", "range0_5", "range12_17", "range18_23", "range24_65", "range6_11", "residencia", "situacion_laboral", "tel", "trabajo"};

    private static final String CUSTOM_BEN_FORM_ID = "peru_registration";
    private static final String GROUP_ID = "GROUP_PERU";
    private static final String ROW_ID_COLUMN = "id";
    private static final String DATE_CREATED_COLUMN = "start";

    void createMappings() {
        columnMatch.put("consentimiento", new ColumnMatch("consentimiento", "consentimiento"));
        columnMatch.put("Comentario", new ColumnMatch("Comentario", "Comentario"));
        columnMatch.put("nombre", new ColumnMatch("nombre", "first_name"));
        columnMatch.put("apellido", new ColumnMatch("apellido", "last_name"));
        columnMatch.put("edad", new ColumnMatch("edad", "edad"));
        columnMatch.put("genero", new ColumnMatch("genero", "gender"));
        columnMatch.put("nacionalidad", new ColumnMatch("nacionalidad", "nacionalidad"));
        columnMatch.put("nacionalidad_otra", new ColumnMatch("nacionalidad_otra", "nacionalidad_otra"));
        columnMatch.put("tel", new ColumnMatch("tel", "tel"));
        columnMatch.put("residencia", new ColumnMatch("residencia", "residencia"));
        columnMatch.put("nucleo_familiar", new ColumnMatch("nucleo_familiar", "nucleo_familiar"));
        columnMatch.put("_0-5", new ColumnMatch("_0-5", "range0_5"));
        columnMatch.put("_6a11", new ColumnMatch("_6a11", "range6_11"));
        columnMatch.put("_12-17", new ColumnMatch("_12-17", "range12_17"));
        columnMatch.put("_18-23", new ColumnMatch("_18-23", "range18_23"));
        columnMatch.put("_24-65", new ColumnMatch("_24-65", "range24_65"));
        columnMatch.put("mas65", new ColumnMatch("mas65", "over65"));
        columnMatch.put("discapacidad", new ColumnMatch("discapacidad", "discapacidad"));
        columnMatch.put("discapacidad_si", new ColumnMatch("discapacidad_si", "discapacidad_si"));
        columnMatch.put("problema_salud", new ColumnMatch("problema_salud", "problema_salud"));
        columnMatch.put("problema_salud_si", new ColumnMatch("problema_salud_si", "problema_salud_si"));
        columnMatch.put("medicamento", new ColumnMatch("medicamento", "medicamento"));
        columnMatch.put("medicamento_accesible", new ColumnMatch("medicamento_accesible", "medicamento_accesible"));
        columnMatch.put("necesidad", new ColumnMatch("necesidad", "necesidad"));
        columnMatch.put("mejoraria_otro", new ColumnMatch("mejoraria_otro", "mejoraria_otro"));
        columnMatch.put("situacion_laboral", new ColumnMatch("situacion_laboral", "situacion_laboral"));
        columnMatch.put("trabajo", new ColumnMatch("trabajo", "trabajo"));
        columnMatch.put("fuera_casa", new ColumnMatch("fuera_casa", "fuera_casa"));
        columnMatch.put("bioseguridad", new ColumnMatch("bioseguridad", "bioseguridad"));
        columnMatch.put("diagnostico_positivo", new ColumnMatch("diagnostico_positivo", "diagnostico_positivo"));
        columnMatch.put("medida", new ColumnMatch("medida", "medida"));
        columnMatch.put("otra_necesidad", new ColumnMatch("otra_necesidad", "otra_necesidad"));
    }

    public PeruConvertCsv() throws IOException {
        super(CUSTOM_BEN_FORM_ID, INTAKE_COL, OUTPUT_COL, GROUP_ID);
        setDateCreatedColumn(DATE_CREATED_COLUMN);
        setRowIdColumn(ROW_ID_COLUMN);
        convertByAssigningRcid(IMPORT_CSV, CSV_WRITE_PATH, IntStream.range(10000, 20000).iterator());
    }

    public static void main(String[] args) throws IOException {
        new PeruConvertCsv();
    }
}
