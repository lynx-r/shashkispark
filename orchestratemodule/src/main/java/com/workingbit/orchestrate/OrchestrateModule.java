package com.workingbit.orchestrate;

import com.workingbit.orchestrate.config.ModuleProperties;
import com.workingbit.orchestrate.service.OrchestralService;
import com.workingbit.orchestrate.util.RedisUtil;
import com.workingbit.share.common.Config4j;

/**
 * Created by Aleksey Popryadukhin on 06/05/2018.
 */
public class OrchestrateModule {
  public static OrchestralService orchestralService;
  public static ModuleProperties moduleProperties;

  private OrchestrateModule() {
  }

  public static void loadModule() {
    RedisUtil.init();
    moduleProperties = Config4j.configurationProvider("moduleconfig.yaml").bind("app", ModuleProperties.class);
    orchestralService = new OrchestralService(moduleProperties);
  }
}
