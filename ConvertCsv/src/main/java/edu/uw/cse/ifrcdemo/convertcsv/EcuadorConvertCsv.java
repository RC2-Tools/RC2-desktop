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

public class EcuadorConvertCsv extends ConvertCsv {
    private static final String IMPORT_CSV = "C:/ecuadordata/ecuador.csv";
    private static final String CSV_WRITE_PATH = "ecuador_csv";

    private static final String[] INTAKE_COL = new String[]{"provincia","canton","parroquia","nombres","apellidos","rango_edad","genero","nacionalidad","tipo_identificacion","nro_identificacion","telefono","familia_condicion","menores_5","nivel_educacion","ingresos_actuales"};
    private static final String[] OUTPUT_COL = new String[]{"consentimiento","Comentario","apellidos","nombres","correo","estado_civil","nacionalidad","tipo_identificacion","otra_identificacion","nro_identificacion","rango_edad","genero","telefono","whatsapp","provincia","canton","parroquia","nivel_educacion","solo_acompanado","integran_familia","menores_5","familia_condicion","trabajo_antes_emergencia","ingresos_antes_emergencia","situacion_actual","ingresos_actuales","necesidades","equipos_proteccion"};

    private static final String CUSTOM_BEN_FORM_ID = "ecuador_registration";
    private static final String GROUP_ID = "GROUP_ECUADOR";
    private static final String RCID_COLUMN = "rcid";

    void createMappings() {
        columnMatch.put("provincia", new ColumnMatch("provincia", "provincia"));
        columnMatch.put("canton", new ColumnMatch("canton", "canton"));
        columnMatch.put("parroquia", new ColumnMatch("parroquia", "parroquia"));
        columnMatch.put("nombres", new ColumnMatch("nombres", "nombres"));
        columnMatch.put("apellidos", new ColumnMatch("apellidos", "apellidos"));
        columnMatch.put("rango_edad", new ColumnMatch("rango_edad", "rango_edad"));
        columnMatch.put("genero", new ColumnMatch("genero", "genero"));
        columnMatch.put("nacionalidad", new ColumnMatch("nacionalidad", "nacionalidad"));
        columnMatch.put("tipo_identificacion", new ColumnMatch("tipo_identificacion", "tipo_identificacion"));
        columnMatch.put("nro_identificacion", new ColumnMatch("nro_identificacion", "nro_identificacion"));
        columnMatch.put("telefono", new ColumnMatch("telefono", "telefono"));
        columnMatch.put("familia_condicion", new ColumnMatch("familia_condicion", "familia_condicion"));
        columnMatch.put("menores_5", new ColumnMatch("menores_5", "menores_5"));
        columnMatch.put("nivel_educacion", new ColumnMatch("nivel_educacion", "nivel_educacion"));
        columnMatch.put("ingresos_actuales", new ColumnMatch("ingresos_actuales", "ingresos_actuales"));

        choicesVerify.put("provincia", new Choices("provincia", new String[]{"Azuay","Bolivar","Canar","Carchi","Chimborazo","Cotopaxi","El_Oro","Esmeraldas","Galapagos","Guayas","Imbabura","Loja","Los_rios","Manabi","Morona_Santiago","Napo","Orellana","Pastaza","Pichincha","santa_elena","Santo_Domingo","Sucumbios","Tungurahua","Zamora_Chinchipe"}));
        choicesVerify.put("rango_edad", new Choices("rango_edad", new String[]{"menor_18","18_25","26_35","36_45","46_55","56_65","mas_65"}));
        choicesVerify.put("genero", new Choices("genero", new String[]{"masculino","femenino","otro"}));
        choicesVerify.put("nacionalidad", new Choices("nacionalidad", new String[]{"ecuatoriana","venezolana","colombiana"}));
        choicesVerify.put("tipo_identificacion", new Choices("tipo_identificacion", new String[]{"cedula","visa","pasaporte","carta_andina","otro"}));
        choicesVerify.put("familia_condicion", new Choices("familia_condicion", new String[]{"[\"ninguna\"]"}));
        choicesVerify.put("nivel_educacion", new Choices("nivel_educacion", new String[]{"ninguno","primaria","secundaria","universitario"}));
        choicesVerify.put("ingresos_actuales", new Choices("ingresos_actuales", new String[]{"0_50","51_100","101_200","201_300","301_400","mas_400"}));
    }

    public EcuadorConvertCsv() throws IOException {
        super(CUSTOM_BEN_FORM_ID, INTAKE_COL, OUTPUT_COL, GROUP_ID);
        setRcidColunm(RCID_COLUMN);
        convert(IMPORT_CSV, CSV_WRITE_PATH);
    }


    public static void main(String[] args) throws IOException {
        new EcuadorConvertCsv();
    }
}
