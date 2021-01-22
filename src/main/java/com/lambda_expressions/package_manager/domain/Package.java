package com.lambda_expressions.package_manager.domain;

import lombok.*;

import javax.persistence.*;
import java.util.List;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:37 AM
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Package {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String appname;
  private String filename;
  private int version;
  private String path;
  private boolean valid;
}
