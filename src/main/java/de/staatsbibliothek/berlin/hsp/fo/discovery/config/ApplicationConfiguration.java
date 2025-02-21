package de.staatsbibliothek.berlin.hsp.fo.discovery.config;

import de.staatsbibliothek.berlin.hsp.fo.discovery.model.*;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.BaseService;
import de.staatsbibliothek.berlin.hsp.fo.discovery.service.impl.BaseServiceImpl;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.FacetField;
import de.staatsbibliothek.berlin.hsp.fo.discovery.type.HspType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class ApplicationConfiguration {
  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter() {
      @Override
      protected boolean shouldLog(HttpServletRequest request) {
        return this.logger.isInfoEnabled();
      }
      @Override
      protected void beforeRequest(HttpServletRequest request, String message) {
        this.logger.info(message);
      }
      @Override
      protected void afterRequest(HttpServletRequest request, String message) {
        // do nothing, as we do not want to log the after request state
      }
    };
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }


  @Bean
  public BaseServiceImpl<Void> baseEntityService() {
    return new BaseServiceImpl<>(Collections.emptyMap(), Void.class);
  }

  @Bean
  public BaseService<HspCatalog> catalogService() {
    return new BaseServiceImpl<>(Map.of(FacetField.TYPE.getName(), List.of(HspType.HSP_CATALOG.getValue())), HspCatalog.class);
  }

  @Bean
  public BaseService<HspDescription> descriptionService() {
    return new BaseServiceImpl<>(Map.of(FacetField.TYPE.getName(), List.of(HspType.HSP_DESCRIPTION.getValue(), HspType.HSP_DESCRIPTION_RETRO.getValue())), HspDescription.class);
  }

  @Bean
  public BaseServiceImpl<HspDigitized> digitizedService() {
    return new BaseServiceImpl<>(Map.of(FacetField.TYPE.getName(), List.of(HspType.HSP_DIGITIZED.getValue())), HspDigitized.class);
  }

  @Bean
  public BaseServiceImpl<HspBaseEntity> infoService() {
    return new BaseServiceImpl<>(Collections.emptyMap(), HspBaseEntity.class);
  }

  @Bean
  public BaseServiceImpl<HspObject> objectService() {
    return new BaseServiceImpl<>(Map.of(FacetField.TYPE.getName(), List.of(HspType.HSP_OBJECT.getValue())), HspObject.class);
  }
}
