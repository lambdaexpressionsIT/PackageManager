package com.lambda_expressions.package_manager.v1.controllers.utils;

import com.lambda_expressions.package_manager.exceptions.AutoDetectionException;
import com.lambda_expressions.package_manager.exceptions.MalformedURLException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by steccothal
 * on Tuesday 19 January 2021
 * at 9:46 AM
 */
@Component
public class ControllerUtils {

  public long checkIdParameter(String appId) throws MalformedURLException {
    long longId;

    try {
      longId = Long.parseLong(appId);
    } catch (NumberFormatException formatException) {
      throw new MalformedURLException("ID is not a number", "unknown appName", appId);
    }

    return longId;
  }

  public List<Long> convertIdList(List<String> stringIds) {
    return stringIds.stream().filter(this::isParsableToLong)
        .map(parsable->Long.parseLong(parsable))
        .collect(Collectors.toList());
  }

  public String getFileName(MultipartFile file) throws AutoDetectionException {

    String fileName = file.getOriginalFilename();

    if(StringUtils.isEmpty(fileName)){
      throw new AutoDetectionException("No filename for received package", "", "");
    }

    return fileName;
  }

  private boolean isParsableToLong(String stringVal){
    try{
      Long.parseLong(stringVal);
    }catch (NumberFormatException e){
      return false;
    }
    return true;
  }

}
