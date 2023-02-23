package edu.uw.cse.ifrcdemo.distplan;

import edu.uw.cse.ifrcdemo.distplan.logic.ResourceInputStreamSupplier;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.csv.FileCsvRepository;
import edu.uw.cse.ifrcdemo.distplan.util.CsvFileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CsvRepoArgConverter extends SimpleArgumentConverter {
  @Override
  protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
    assertNotNull(source);
    assertEquals(CsvRepository.class, targetType);

    return build((String) source);
  }

  public static CsvRepository build(String csvDirPath) {
    Path csvPath = Paths.get(csvDirPath);

    Function<String, Supplier<InputStream>> inputStreamSupplier = name ->
        new ResourceInputStreamSupplier(FileUtil.getPathToCSV(csvPath, name).toString());

    CsvRepository csvRepository = new FileCsvRepository();

    CompletableFuture<?>[] baseAndCustomTables = Stream
        .of(
            CsvIndividual.class,
            CsvBeneficiaryEntity.class,
            CsvVisit.class
        )
        .filter(clazz -> resourceExists(FileUtil.getPathToCsv(csvPath, clazz).toString()))
        .map(clazz -> CsvFileUtil.readBaseTableWithCustomTable(
            inputStreamSupplier,
            clazz,
            row -> row.getCustomTableFormId(),
            csvRepository
        ))
        .toArray(CompletableFuture<?>[]::new);

    CompletableFuture<?>[] baseTablesOnly = Stream
        .of(
            CsvAuthorization.class,
            CsvEntitlement.class,
            CsvDistribution.class
        )
        .filter(clazz -> resourceExists(FileUtil.getPathToCsv(csvPath, clazz).toString()))
        .map(clazz -> CsvFileUtil.readBaseTable(
            inputStreamSupplier.apply(FileUtil.getFileName(clazz)),
            clazz,
            csvRepository
        ))
        .toArray(CompletableFuture<?>[]::new);

    CompletableFuture
        .allOf(
            CompletableFuture.allOf(baseAndCustomTables),
            CompletableFuture.allOf(baseTablesOnly)
        )
        .join();

    return csvRepository;
  }

  private static boolean resourceExists(String name) {
    return CsvRepoArgConverter.class.getClassLoader().getResource(name) != null;
  }
}
