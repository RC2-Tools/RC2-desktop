package edu.uw.cse.ifrcdemo.distplan;

import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionField;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CriterionFieldArgConverter extends SimpleArgumentConverter {
  @Override
  protected CriterionField convert(Object source, Class<?> targetType) throws ArgumentConversionException {
    assertEquals(CriterionField.class, targetType);

    if (source == null) {
      return null;
    }

    String[] split = ((String) source).split("//", 2);
    assertEquals(2, split.length);

    return new CriterionField(split[0], split[1]);
  }
}
