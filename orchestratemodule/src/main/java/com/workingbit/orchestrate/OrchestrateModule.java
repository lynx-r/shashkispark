package com.workingbit.orchestrate;

import com.workingbit.orchestrate.config.ShareProperties;
import com.workingbit.orchestrate.service.OrchestralService;
import com.workingbit.orchestrate.util.RedisUtil;
import com.workingbit.share.common.Config4j;

/**
 * Created by Aleksey Popryadukhin on 06/05/2018.
 */
public class OrchestrateModule {
  public static OrchestralService orchestralService;

  private OrchestrateModule() {
  }

  public static void loadModule() {
    RedisUtil.init();
    ShareProperties shareProperties = Config4j.configurationProvider("shareconfig.yaml").bind("app", ShareProperties.class);
    orchestralService = new OrchestralService(shareProperties);
  }
}
