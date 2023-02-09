## RC2 Relief Tool Report Module 

### Report Template Reference 
The template file (`src/main/resources/pdf-template/deliveries_report.mustache`) is a [Mustache template](https://mustache.github.io/mustache.5.html). This module combines CSVs in the data directory and 
the template to produce reports. 

#### Basic Data Access

All the CSVs in the data directory are available as variables to the templates. You can access 
the files by their table ID. 

For example, to iterate over the `custom_distribution_reports` table,
```
{{#custom_distribution_reports}}

{{/custom_distribution_reports}}
```

Metadata columns are available are variables under each table. 

For example, to print the Row ID of a row in `custom_distribution_reports`,
```
{{#custom_distribution_reports}}
    {{rowId}}
{{/custom_distribution_reports}}
```

Other columns are available under the `columns` variable. 

For example, to print the report version of a row in `custom_distribution_reports`,
```
{{#custom_distribution_reports}}
    {{columns.report_version}}
{{/custom_distribution_reports}}
```

**Note:** For easier access, all of the columns in the base tables are merged to their corresponding custom table. If they contain columns with the same name, the custom table takes precedence. 

#### Attachments 

Row attachments are not available directly from the table variable but through the `_wrapped` variable 
in the top level context. The `_wrapped` variable is a wrapped version of the table variables mentioned 
above but with additional Mustache lambdas added. 

Currently, the only function that `_wrapped` adds is the `attachment` function. It takes a column name 
and returns a data uri for the attachment stored under that column name. 

For example, to retrieve an attachment stored under the `receive_signature` column in the `ctp_delivery` table, 
```
{{#_wrapped.ctp_delivery}}
    {{#attachment}}receive_signature{{/attachment}}
{{/_wrapped.ctp_delivery}}
```

#### Other variables

The report module also exposes some information outside of the data directory. 

 - `_metadata`  
    `_metadata.date` The date the report was generated  
    `_metadata.time` The time the report was generated 