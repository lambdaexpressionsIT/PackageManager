package com.lambda_expressions.package_manager.repositories;

import com.lambda_expressions.package_manager.domain.Package;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:37 AM
 */
@Repository
public interface PackageRepository extends CrudRepository<Package, Long> {
  @Nullable
  List<Package> findByAppnameIgnoreCase(String appname);

  @Nullable
  Package findByAppnameIgnoreCaseAndVersion(String appname, int version);
}
