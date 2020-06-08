package gov.nih.tbi.api.query.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.tbi.service.model.MetaDataCache;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class CacheController {

	@Autowired
	MetaDataCache cache;

	@RequestMapping(value = "/cache/clear", method = RequestMethod.DELETE)
	ResponseEntity<Void> clearCache() {
		cache.clearCache();
		return ResponseEntity.ok().build();
	}
}
