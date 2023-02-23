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

package edu.uw.cse.ifrcdemo.distplan.ui.Validation;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.springframework.util.ObjectUtils.isEmpty;

// Add conditional validator
// https://medium.com/@crb3/spring-boot-conditional-validation-example-2dd23af22539
public class ConditionalValidator implements ConstraintValidator<Conditional, Object> {

  private String selected;
  private String[] required;
  private String message;
  private String[] values;

  @Autowired
  private static Logger logger;

  @Override
  public void initialize(Conditional constraintAnnotation) {
    selected = constraintAnnotation.selected();
    required = constraintAnnotation.required();
    message = constraintAnnotation.message();
    values = constraintAnnotation.values();
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    Boolean valid = true;
    try {
      Object checkedValue = BeanUtils.getProperty(object, selected);
      if (Arrays.asList(values).contains(checkedValue)) {
        for (String propName : required) {
          Object requiredValue = BeanUtils.getProperty(object, propName);
          valid = requiredValue != null && !isEmpty(requiredValue);
          if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(propName)
                .addConstraintViolation();
          }
        }
      }
    } catch (IllegalAccessException e) {
      logger.error("Accessor method is not available for class : {}, exception : {}",
          object.getClass().getName(), e);
      e.printStackTrace();
      return false;
    } catch (NoSuchMethodException e) {
      logger.error("Field or method is not present on class : {}, exception : {}",
          object.getClass().getName(), e);
      e.printStackTrace();
      return false;
    } catch (InvocationTargetException e) {
      logger.error("An exception occurred while accessing class : {}, exception : {}",
          object.getClass().getName(), e);
      e.printStackTrace();
      return false;
    }
    return valid;
  }
}
