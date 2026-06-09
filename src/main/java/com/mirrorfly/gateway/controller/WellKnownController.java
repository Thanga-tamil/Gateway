package com.mirrorfly.gateway.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.config.ApplicationProperties;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WellKnownController {

	private static final String SHA256_FINGERPRINT =
			"D6:F3:A7:4E:47:95:33:59:C9:87:CF:45:A6:FA:19:B6:70:82:8F:C7:60:8B:F2:53:9A:D4:7A:B0:A4:2B:C6:03";

	private final ApplicationProperties applicationProperties;

	@GetMapping(value = "/.well-known/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, Object>> getAssetLinks() {
		Map<String, Object> target = new LinkedHashMap<>();
		target.put("namespace", "android_app");
		target.put("package_name", applicationProperties.getAndroidPackageName());
		target.put("sha256_cert_fingerprints", List.of(SHA256_FINGERPRINT));

		Map<String, Object> relationExtensions = new LinkedHashMap<>();
		relationExtensions.put("delegate_permission/common.handle_all_urls", Map.of(
				"dynamic_app_link_components", List.of(Map.of("/", "/invite"))));

		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("relation", List.of("delegate_permission/common.handle_all_urls"));
		entry.put("target", target);
		entry.put("relation_extensions", relationExtensions);

		return List.of(entry);
	}

	@GetMapping(value = "/.well-known/apple-app-site-association", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getAppleAppSiteAssociation() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("appID", applicationProperties.getIosAppId());
		details.put("paths", List.of("/invite"));

		Map<String, Object> applinks = new LinkedHashMap<>();
		applinks.put("apps", List.of());
		applinks.put("details", List.of(details));

		return Map.of("applinks", applinks);
	}

}
